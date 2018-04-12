package com.xue.douyin.common.player;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by 薛贤俊 on 2018/4/7.
 */

public abstract class BaseRenderer implements Renderer {
    protected String mFile;
    protected Handler mHandler;
    private final int mTrackType;
    private long mCurrentPositionUs;
    private int mState;

    public BaseRenderer(int trackType) {
        this.mTrackType = trackType;
    }

    public void start() {
        mState = STATE_STARTED;
        onStart();
    }

    protected abstract void onStart();


    public void stop() {
        mState = STATE_STOPPED;
        onStop();
    }

    protected abstract void onStop();

    @Override
    public void resetPosition(long positionUs) {
        mCurrentPositionUs = positionUs;
        onResetPosition();
    }

    protected abstract void onResetPosition();
}
