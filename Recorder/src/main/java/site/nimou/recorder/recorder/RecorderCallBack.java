package site.nimou.recorder.recorder;

/**
 * @author Ni Jiajun njj1108@outlook.com
 * @since 1.0.0 2024-04-24
 **/
@FunctionalInterface
public interface RecorderCallBack {

    /**
     * 录音完成
     */
    void onRecordComplete(String filePath);

}
