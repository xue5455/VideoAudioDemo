package com.xue.douyin.common.recorder.tst;

import android.media.AudioFormat;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public interface AudioSource {
    int SAMPLE_RATE = 44100;

    int CHANNEL = AudioFormat.CHANNEL_IN_MONO;

    int BIT_RATE = 64000;

    int CHANNEL_COUNT = 1;

    void start();

    void stop();

    void setEncoder(AudioEncoder encoder);
}
