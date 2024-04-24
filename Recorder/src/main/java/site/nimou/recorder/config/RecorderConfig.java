package site.nimou.recorder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Ni Jiajun njj1108@outlook.com
 * @since 1.0.0 2024-04-24
 **/
@Data
@Component
@ConfigurationProperties(prefix = "recorder")
public class RecorderConfig {
    // 录制时长
    private int recordTime;
    // 录制文件路径
    private String recordFilePath;
}
