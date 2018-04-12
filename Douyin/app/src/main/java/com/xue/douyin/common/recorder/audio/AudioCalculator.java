package com.xue.douyin.common.recorder.audio;

import android.media.MediaCodec;

import com.xue.douyin.common.recorder.TimeCalculator;
import com.xue.douyin.common.util.LogUtil;

import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/3/24.
 */

public class AudioCalculator implements TimeCalculator {

    private long mDuration;

    private long mInterval;

    private long mTimeStamp;

    public AudioCalculator(AudioConfig config, long startTime) {
        mInterval = 1024 * 1000000L / 44100;
        mTimeStamp = 0;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public void addSample(int sizeInBytes) {
        long interval = (long) (mInterval * sizeInBytes / 1024f);
        LogUtil.d("xue","addSample " + interval);
        mDuration += mInterval;
        mTimeStamp += mInterval;
    }

    @Override
    public long getTimeStampUs() {
        return mTimeStamp;
    }
}
