package com.xue.douyin.common.codec.video;

import android.media.MediaCodec;

/**
 * Created by 薛贤俊 on 2018/3/10.
 */

public class TimeModifier {
    /**
     * 倍数，正常是1。大于1为加速录制，小于1为减速录制
     */
    private float mSpeed;
    /**
     * 上一帧数据写入到文件中的时间戳
     */
    private long mLastTimestamp;

    private long mContentLength;

    private long mMaxLength;

    public TimeModifier(float speed) {
        this.mSpeed = speed;
    }

    public void setMaxLength(long maxLength) {
        mMaxLength = maxLength;
    }

    /**
     * 处理输入数据，处理偏移
     *
     * @param data
     */
    public void processData(MediaCodec.BufferInfo data) {
        data.presentationTimeUs = (long) (data.presentationTimeUs / mSpeed);
        if (mLastTimestamp == 0) {
            mLastTimestamp = data.presentationTimeUs;
        }
        long duration = data.presentationTimeUs - mLastTimestamp;
        mContentLength += duration;
        mLastTimestamp = data.presentationTimeUs;
    }

    public boolean isReachMaxLength() {
        return mContentLength >= mMaxLength;
    }

    public long getDuration() {
        return mContentLength;
    }


}
