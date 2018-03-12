package com.xue.douyin.douyin.common.codec;

import com.xue.douyin.douyin.common.preview.CameraFilter;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class VideoFrameData {
    private CameraFilter mFilter;
    private float[] mMatrix;
    private long mTimeStamp;
    private int mTextureId;

    public VideoFrameData(CameraFilter filter, float[] matrix, long timestamp, int textureId) {
        this.mFilter = filter;
        this.mMatrix = matrix;
        this.mTimeStamp = timestamp;
        this.mTextureId = textureId;
    }

    public CameraFilter getFilter() {
        return mFilter;
    }

    public float[] getMatrix() {
        return mMatrix;
    }

    public long getTimestamp() {
        return mTimeStamp;
    }

    public int getTextureId() {
        return mTextureId;
    }
}
