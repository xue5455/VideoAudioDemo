package Jni;

import android.support.annotation.Keep;

import com.netease.ffmpeg.VideoCmdCallback;
import com.xue.douyin.common.util.LogUtil;

@Keep
public class FFmpegCmd {

    public static Object sLock = new Object();

    /**
     * 加载所有相关链接库
     */
    static {
        System.loadLibrary("avutil");
        System.loadLibrary("avcodec");
        System.loadLibrary("swresample");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("avfilter");
        System.loadLibrary("avdevice");
        System.loadLibrary("ffmpeg");
    }


    /**
     * 调用底层执行
     *
     * @param argc
     * @param argv
     * @return
     */
    @Keep
    public static native int exec(int argc, String[] argv);

    @Keep
    public static native void exit();

    @Keep
    public static void onExecuted(int ret) {
        synchronized (sLock) {
            sLock.notify();
        }
    }

    @Keep
    public static void onProgress(float progress) {
        LogUtil.d("progress is " + progress);
    }


    /**
     * 执行ffmoeg命令
     *
     * @param cmds
     */
    @Keep
    public static void exec(String[] cmds, VideoCmdCallback callback) {
        int ret = exec(cmds.length, cmds);
        synchronized (sLock) {
            try {
                sLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (callback != null) {
            callback.onCommandFinish(ret == 0);
        }
    }
}
