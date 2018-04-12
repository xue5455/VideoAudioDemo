package com.xue.douyin.common.recorder;

import com.xue.douyin.common.C;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public class AudioClock implements MediaClock {

    private long sampleDuration;

    private long timeUs = 0;

    private long maxDuration;

    private long startTime;

    public AudioClock(long startTime, int sampleRate, int samplePerFrame) {
        sampleDuration = samplePerFrame * C.SECOND_IN_US / sampleRate;
        this.startTime = startTime;
        timeUs = startTime - sampleDuration;
    }

    @Override
    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public void addSample(long presentationUs) {
        timeUs += (sampleDuration);
    }

    @Override
    public long getTimeUs() {
        return timeUs;
    }

    @Override
    public boolean reachMaxCounts() {
        return getDuration() >= maxDuration;
    }

    @Override
    public long getDuration() {
        return timeUs - startTime;
    }

}
