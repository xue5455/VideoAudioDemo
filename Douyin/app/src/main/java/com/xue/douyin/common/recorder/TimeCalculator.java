package com.xue.douyin.common.recorder;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/3/24.
 */

public interface TimeCalculator {
    long getDuration();

    void addSample(int sizeInBytes);

    long getTimeStampUs();
}
