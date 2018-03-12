package com.xue.douyin.douyin.common.codec;

import android.opengl.EGLContext;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class VideoConfig {
    private EGLContext mGLContext;

    private int mVideoWidth;

    private int mVideoHeight;

    private int mBitRate;

    public VideoConfig(EGLContext context, int width, int height, int bitRate) {
        this.mGLContext = context;
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        this.mBitRate = bitRate;
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
}
