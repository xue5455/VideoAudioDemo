package com.xue.douyin.common.recorder;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public interface MediaClock {
    void setMaxDuration(long maxDuration);

    void addSample(long presentationUs);

    long getTimeUs();

    boolean reachMaxCounts();

    long getDuration();
}
