package com.xue.douyin.common.codec;

/**
 * Created by 薛贤俊 on 2018/3/10.
 */

public class TimeModifier {
    /**
     * 倍数，正常是1，小于1为加速录制，大于1为减速录制
     */
    protected double mSpeed;
    /**
     * 上一帧数据写入到文件中的时间戳
     */
    protected long mLastTimestamp;

    protected long mContentLength;

    protected long mMaxLength;

    public TimeModifier(double speed) {
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
    void processData(MovieMuxCore.MuxerData data) {
        if (mLastTimestamp == 0) {
            mLastTimestamp = data.info.presentationTimeUs;
        }
        long duration = data.info.presentationTimeUs - mLastTimestamp;
        mContentLength += (long) (duration * mSpeed);
        mLastTimestamp = data.info.presentationTimeUs;
    }

    boolean isReachMaxLength() {
        return mContentLength >= mMaxLength;
    }

    long getDuration() {
        return mContentLength;
    }


}
