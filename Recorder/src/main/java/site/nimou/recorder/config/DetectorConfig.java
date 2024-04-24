package site.nimou.recorder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 检测器配置
 *
 * @author Ni Jiajun njj1108@outlook.com
 * @since 1.0.0 2024-04-24
 **/
@Data
@Component
@ConfigurationProperties(prefix = "recorder.detector")
public class DetectorConfig {
    // 采样率
    private int sampleRate;
    // 阈值分贝
    private double thresholdDB;
    // 缓冲区大小
    private int bufferSize;
    // 分贝值队列长度
    private int dbQueueSize;
}
