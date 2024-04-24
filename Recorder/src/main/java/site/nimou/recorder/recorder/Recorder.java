package site.nimou.recorder.recorder;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.nimou.recorder.config.RecorderConfig;
import site.nimou.recorder.util.RecorderUtil;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 录音器
 *
 * @author Ni Jiajun njj1108@outlook.com
 * @since 1.0.0 2024-04-24
 **/
public class Recorder extends Thread {

    // 录制时长
    private final int recordTime;
    // 录制文件路径
    private final String recordFilePath;
    @Setter
    private RecorderCallBack recorderCallBack;

    private final Logger logger = LoggerFactory.getLogger(Recorder.class);


    public Recorder(RecorderConfig recorderConfig) {
        this.recordTime = recorderConfig.getRecordTime();
        this.recordFilePath = recorderConfig.getRecordFilePath();
    }

    @Override
    public void run() {
        startRecord();
    }

    private void startRecord() {
        try {
            logger.debug("开始录音...");
            File audioFile = new File(recordFilePath + File.separator + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".wav");
            TargetDataLine line = RecorderUtil.getTargetDataLine(44100);
            // 录制指定时间
            AudioInputStream ais = new AudioInputStream(line);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    line.stop();
                    line.close();
                }
            }, recordTime * 1000L);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);
            recorderCallBack.onRecordComplete(audioFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
