package com.xue.douyin.common.recorder;

import android.media.MediaCodec;

import com.xue.douyin.common.recorder.tst.Consumer;
import java.nio.ByteBuffer;
import static com.xue.douyin.common.recorder.MediaConfig.MODE_EXTRA_FAST;
import static com.xue.douyin.common.recorder.MediaConfig.MODE_EXTRA_SLOW;
import static com.xue.douyin.common.recorder.MediaConfig.MODE_FAST;
import static com.xue.douyin.common.recorder.MediaConfig.MODE_NORMAL;
import static com.xue.douyin.common.recorder.MediaConfig.MODE_SLOW;

/**
 * Created by 薛贤俊 on 2018/4/11.
 */

public class AudioSampler {

    private @MediaConfig.SpeedMode
    int speedMode;

    private Consumer consumer;

    public AudioSampler(int speedMode, Consumer consumer) {
        this.speedMode = speedMode;
        this.consumer = consumer;
    }


    public void addSample(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        switch (speedMode) {
            case MODE_EXTRA_SLOW:
                consumer.consume(Consumer.AUDIO, buffer, info);
            case MODE_SLOW:
                consumer.consume(Consumer.AUDIO, buffer, info);
            case MODE_NORMAL:
            case MODE_FAST:
            case MODE_EXTRA_FAST:
                consumer.consume(Consumer.AUDIO, buffer, info);
        }
    }
}
