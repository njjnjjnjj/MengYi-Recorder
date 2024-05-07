package site.nimou.recorder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import site.nimou.recorder.config.DetectorConfig;
import site.nimou.recorder.config.RecorderConfig;
import site.nimou.recorder.detector.Detector;
import site.nimou.recorder.recorder.Recorder;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class RecorderApplication {

    private static final Logger logger = LoggerFactory.getLogger(RecorderApplication.class);

    @Resource
    private DetectorConfig detectorConfigBean;
    private static DetectorConfig detectorConfig;
    @Resource
    private RecorderConfig recorderConfigBean;
    private static RecorderConfig recorderConfig;

    private static final Lock detectorAndRecorderLock = new ReentrantLock();
    private static final Condition detectorAndRecorderCondition = detectorAndRecorderLock.newCondition();


    @PostConstruct
    public void init() {
        detectorConfig = this.detectorConfigBean;
        recorderConfig = this.recorderConfigBean;
        savePid();
    }

    private static void savePid() {
        // 获取进程pid，保存至程序运行目录的 pid.txt 文件中
        String name = ManagementFactory.getRuntimeMXBean().getName();
        logger.debug("name:{}", name);
        String pid = name.split("@")[0];
        logger.debug("pid:{}", pid);
        try {
            FileUtils.writeStringToFile(new File("./pid.txt"), pid, "UTF-8");
        } catch (IOException e) {
            logger.error("pid保存失败", e);
        }
    }

    public static void main(String[] args) {
        logger.info("程序启动");
        SpringApplication.run(RecorderApplication.class, args);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        logger.debug("构造检测器与录音器");
        Recorder recorder = new Recorder(recorderConfig);
        recorder.setLock(detectorAndRecorderLock);
        recorder.setCondition(detectorAndRecorderCondition);
        Detector detector = new Detector(detectorConfig);
        detector.setLock(detectorAndRecorderLock);
        detector.setCondition(detectorAndRecorderCondition);
        recorder.setRecorderCallBack(filePath -> {
            // 录音完成后，重新开始声音检测
            logger.info("录音完成，文件路径：{}", filePath);
            detectorAndRecorderCondition.signal();
            detector.restartDetect();
        });
        detector.setDetectorCallBack(() -> {
            // 检测到声音后，开启录音线程
            logger.info("检测到声音");
            // 如果recorder线程未启动则启动，否则唤醒
            if (!recorder.isAlive()) {
                recorder.start();
            } else {
                detectorAndRecorderCondition.signal();
            }
        });
        logger.debug("检测器与录音器构造完成");
        detector.start();
        try {
            // 阻塞主线程
            logger.debug("阻塞主线程，保持程序运行...");
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
