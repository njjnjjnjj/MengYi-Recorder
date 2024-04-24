package site.nimou.recorder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
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

import java.util.concurrent.CountDownLatch;

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

    @PostConstruct
    public void init() {
        detectorConfig = this.detectorConfigBean;
        recorderConfig = this.recorderConfigBean;
    }

    public static void main(String[] args) {
        SpringApplication.run(RecorderApplication.class, args);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Recorder recorder = new Recorder(recorderConfig);
        Detector detector = new Detector(detectorConfig);

        recorder.setRecorderCallBack(filePath -> {
            // 录音完成后，重新开始声音检测
            logger.debug("录音完成，文件路径：{}", filePath);
        });
        detector.setDetectorCallBack(() -> {
            // 检测到声音后，开启录音线程
            logger.debug("检测到声音，开始录音...");
            recorder.start();
        });
        detector.start();

        // 阻塞主线程
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
