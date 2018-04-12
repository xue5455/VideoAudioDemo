package com.xue.douyin.common.recorder;

/**
 * Created by 薛贤俊 on 2018/4/11.
 */

public class TestClock {
    private long lastTimeStamp;

    private long timeStamp;

    public void addSample(long time) {
        if (lastTimeStamp == 0) {
            lastTimeStamp = time;
            timeStamp = 0;
        } else {
            long interval = time - lastTimeStamp;
            lastTimeStamp = time;
            timeStamp += interval;
        }

    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
