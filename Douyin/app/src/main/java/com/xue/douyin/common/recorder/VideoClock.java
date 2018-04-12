package com.xue.douyin.common.recorder;


import com.xue.douyin.common.util.LogUtil;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public class VideoClock implements MediaClock {

    private long lastTimeStamp;

    private long timeStamp;

    private long maxDuration;

    private float factor;

    private long startTime;

    private long duration;

    public VideoClock(float factor, long startTime) {
        this.factor = factor;
        this.startTime = startTime;
    }

    @Override
    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public void addSample(long presentationUs) {
        if (lastTimeStamp == 0) {
            lastTimeStamp = presentationUs;
        } else {
            long interval = presentationUs - lastTimeStamp;
            lastTimeStamp = presentationUs;
            duration += interval / factor;
        }

        timeStamp = presentationUs;
    }

    @Override
    public long getTimeUs() {
        return timeStamp;
    }

    @Override
    public boolean reachMaxCounts() {
        return getDuration() >= maxDuration;
    }

    @Override
    public long getDuration() {
        return duration;
    }
}
