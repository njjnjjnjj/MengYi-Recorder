package site.nimou.recorder.detector;

/**
 * 声音检测器回调函数
 *
 * @author Ni Jiajun njj1108@outlook.com
 * @since 1.0.0 2024-04-24
 **/
@FunctionalInterface
public interface DetectorCallBack {

    /**
     * 检测到声音的回调
     */
    void onSoundDetected();

}
