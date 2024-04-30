package site.nimou.recorder.detector;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.nimou.recorder.config.DetectorConfig;
import site.nimou.recorder.util.RecorderUtil;

import javax.sound.sampled.TargetDataLine;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 声音检测器
 *
 * <p>
 * 当声音大于阈值后，将调用传入的回调函数
 * </p>
 *
 * @author Ni Jiajun njj1108@outlook.com
 * @since 1.0.0 2024-04-24
 **/
public class Detector extends Thread {

    public static final String THREAD_NAME_PREFIX = "Detector-";

    // 采样率
    private final int sampleRate;
    // 阈值分贝
    private final double thresholdDB;
    // 缓冲区大小
    private final int bufferSize;
    // 分贝值队列长度
    private final int dbQueueSize;
    // 回调函数
    @Setter
    private DetectorCallBack detectorCallBack;
    // 检测标识符
    @Setter
    private boolean detectFlag = true;

    // 分贝值队列
    Queue<Double> dbQueue = new LinkedList<>();

    @Setter
    private Lock lock;
    @Setter
    private Condition condition;

    private final Logger logger = LoggerFactory.getLogger(Detector.class);

    // 分贝值队列，仅保存近期5个分贝值
    boolean recording = false;
    byte[] buffer;
    TargetDataLine line;


    public void init() {
        logger.debug("初始化检测器");
        buffer = new byte[bufferSize];
        line = RecorderUtil.getTargetDataLine(sampleRate);
    }

    public void restartDetect() {
        logger.info("重新开始检测...");
        init();
    }

    public Detector(DetectorConfig detectorConfig) {
        this.sampleRate = detectorConfig.getSampleRate();
        this.thresholdDB = detectorConfig.getThresholdDB();
        this.bufferSize = detectorConfig.getBufferSize();
        this.dbQueueSize = detectorConfig.getDbQueueSize();
        this.setName(THREAD_NAME_PREFIX + this.getId());
    }

    @Override
    public void run() {
        init();
        startDetect();
    }

    /**
     * 开始声音检测
     */
    public void startDetect() {
        try {
            lock.lock();
            logger.debug("开始检测音量...");
            while (true) {
                line.read(buffer, 0, buffer.length);
                double db = calculateDB(buffer);
                addDBToQueue(db);
                double averageDB = calculateAverageDB();
                logger.debug("当前音量：{}", db);
                if (!recording && (db - averageDB) > thresholdDB) {
                    // 停止监听
                    detectFlag = false;
                    // 关闭资源
                    line.stop();
                    line.close();
                    // 调用回调函数
                    detectorCallBack.onSoundDetected();
                    condition.await();
                }
                // 等待500毫秒
                Thread.sleep(500);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 计算分贝值
     */
    private double calculateDB(byte[] buffer) {
        long sum = 0;
        for (int i = 0; i < buffer.length; i += 2) {
            short sample = (short) ((buffer[i + 1] << 8) | buffer[i]);
            sum += sample * sample;
        }
        double rms = Math.sqrt((double) sum / buffer.length);
        return 20 * Math.log10(rms);
    }

    /**
     * 将分贝值添加至队列中
     */
    private void addDBToQueue(double db) {
        dbQueue.add(db);
        if (dbQueue.size() > dbQueueSize) {
            dbQueue.poll();
        }
    }

    /**
     * 计算分贝平均值
     */
    private double calculateAverageDB() {
        double sum = 0;
        for (Double db : dbQueue) {
            sum += db;
        }
        double v = sum / dbQueue.size();
        logger.debug("平均音量：{}", v);
        return v;
    }

}
