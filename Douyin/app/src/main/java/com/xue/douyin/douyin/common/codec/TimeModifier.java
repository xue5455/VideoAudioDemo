package com.xue.douyin.douyin.common.codec;

/**
 * Created by 薛贤俊 on 2018/3/10.
 */

public class TimeModifier {
    /**
     * 倍数，正常是1，大于1为加速录制，小于1为减速录制
     */
    protected double mSpeed;
    /**
     * 上一帧数据写入到文件中的时间戳
     */
    protected long mLastTimestamp;
    /**
     * 上一帧数据修正后的时间戳
     */
    protected long mLastModifiedTimestamp;
    /**
     * 上一帧数据修正前的时间戳
     */
    protected long mLastRealTimestamp;

    /**
     * 暂停之后停留的时间
     */
    protected long mPausedInterval;

    protected long mPausedTimestamp;

    protected long mContentLength;

    public TimeModifier(double speed) {
        this.mSpeed = speed;
    }

    /**
     * 处理输入数据，处理偏移
     *
     * @param data
     */
    void processData(MovieMuxCore.MuxerData data) {
        long lastModifiedTime = mLastModifiedTimestamp;
        mLastRealTimestamp = data.info.presentationTimeUs;
        updatePausedInterval(mLastRealTimestamp);
        mLastModifiedTimestamp = mLastRealTimestamp - mPausedInterval;
        if (mLastTimestamp == 0) {
            mLastTimestamp = mLastModifiedTimestamp;
        }
        //每帧之间的间隔是当前修正的时间 - 上一帧修正的时间
        long frameInterval = lastModifiedTime == 0 ? 0 : (mLastModifiedTimestamp - lastModifiedTime);
        data.info.presentationTimeUs = (long) (mLastTimestamp + frameInterval / mSpeed);
        mLastTimestamp = data.info.presentationTimeUs;
        mContentLength += frameInterval / mSpeed;
    }

    long getCurrentLength() {
        return mContentLength;
    }

    void onPause() {
        mPausedTimestamp = mLastRealTimestamp;
    }

    protected void updatePausedInterval(long realTimestamp) {
        if (mPausedTimestamp != 0) {
            mPausedInterval = realTimestamp - mPausedTimestamp;
            mPausedTimestamp = 0;
        }
    }


}
