package com.xue.douyin.common.player;

/**
 * Created by 薛贤俊 on 2018/4/7.
 */

public interface Renderer {
    int STATE_STARTED = 1;

    int STATE_STOPPED = 2;

    int getTrackType();

    boolean isStarted();

    void start();

    void resetPosition(long positionUs);

    void render(long positionUs);

    void stop();

    long getCurrentPositionUs();


}
