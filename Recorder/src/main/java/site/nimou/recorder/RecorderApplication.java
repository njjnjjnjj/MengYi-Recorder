package site.nimou.recorder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import site.nimou.recorder.config.DetectorConfig;
import site.nimou.recorder.detector.Detector;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class RecorderApplication {

    private static final Logger logger = LoggerFactory.getLogger(RecorderApplication.class);

    @Resource
    private DetectorConfig detectorConfigBean;

    private static DetectorConfig detectorConfig;

    @PostConstruct
    public void init() {
        detectorConfig = this.detectorConfigBean;
    }

    public static void main(String[] args) {
        SpringApplication.run(RecorderApplication.class, args);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Detector detector = new Detector(detectorConfig, () -> {
            logger.debug("检测到声音...");
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
