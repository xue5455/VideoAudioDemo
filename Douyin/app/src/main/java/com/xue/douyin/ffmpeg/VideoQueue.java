package com.xue.douyin.ffmpeg;

import android.os.Handler;
import android.os.HandlerThread;

import Jni.FFmpegCmd;

/**
 * Created by 薛贤俊 on 2018/3/14.
 */

public class VideoQueue {
    private Handler mHandler = new Handler();

    public VideoQueue() {
        HandlerThread thread = new HandlerThread("ffmpeg");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    public void release() {
        if (mHandler == null) {
            return;
        }
        mHandler.getLooper().quitSafely();
        mHandler = null;
    }

    public void execCommand(final String[] cmd, final VideoCmdCallback callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                FFmpegCmd.exec(cmd, callback);
            }
        });
    }

    public void execCommand(final String[] cmd) {
        execCommand(cmd, null);
    }
}
