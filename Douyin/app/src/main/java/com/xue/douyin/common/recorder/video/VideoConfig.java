package com.xue.douyin.common.recorder.video;

import android.opengl.EGLContext;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class VideoConfig {

    private EGLContext mGLContext;

    private int mVideoWidth;

    private int mVideoHeight;

    private int mBitRate;

    private String fileName;

    private float factor;

    private long maxDuration;

    public VideoConfig(String fileName, EGLContext context, int width, int height, int bitRate) {
        this.mGLContext = context;
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        this.mBitRate = bitRate;
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    public EGLContext getGLContext() {
        return mGLContext;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }
}
