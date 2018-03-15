package com.xue.douyin.common.codec.video;

import android.opengl.EGLContext;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class VideoConfig {

    private String mOutputFile;

    private EGLContext mGLContext;

    private int mVideoWidth;

    private int mVideoHeight;

    private int mBitRate;

    private long mMaxLength;

    private float mSpeed;

    public VideoConfig(EGLContext context, int width, int height, int bitRate, String file, long maxLength,float speed) {
        this.mGLContext = context;
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        this.mBitRate = bitRate;
        this.mOutputFile = file;
        this.mMaxLength = maxLength;
        this.mSpeed = speed;
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

    public String getOuputFile() {
        return mOutputFile;
    }

    public long getMaxLength() {
        return mMaxLength;
    }

    public float getSpeed(){
        return mSpeed;
    }
}
