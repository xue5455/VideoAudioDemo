package com.xue.douyin.common.recorder.video;

import android.media.MediaCodec;

import com.xue.douyin.common.recorder.TimeCalculator;

/**
 * Created by 薛贤俊 on 2018/3/23.
 */

public class VideoCalculator implements TimeCalculator {

    private long mLastTime;

    private float mFactor;

    private long mDuration;

    private long mInterval;

    private long mTimeStamp;

    public VideoCalculator(float factor, long startTime) {
        this.mFactor = factor;
        //30fps
        mInterval = 1000000L / 30;
        mTimeStamp = 0;
    }

    @Override
    public void addSample(int sizeInBytes) {
        long interval = mInterval*2;
        mDuration += interval;
        mTimeStamp += interval;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public long getTimeStampUs() {
        return mTimeStamp;
    }
}
