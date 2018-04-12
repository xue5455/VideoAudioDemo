package com.xue.douyin.common.recorder.tst;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.IntDef;

import com.xue.douyin.common.recorder.MediaConfig;
import com.xue.douyin.common.recorder.OnRecordFinishListener;
import com.xue.douyin.common.recorder.OnRecordProgressListener;
import com.xue.douyin.common.recorder.Recorder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public interface Consumer {

    int VIDEO = 1;

    int AUDIO = 2;

    @IntDef({VIDEO, AUDIO})
    @Retention(RetentionPolicy.SOURCE)
    @interface DataType {

    }

    void consume(@DataType int type, ByteBuffer sample, MediaCodec.BufferInfo info);

    void setProducerReady(@DataType int type, MediaFormat format);

    void producerStopped(@DataType int type);

    void start(MediaConfig config);

    void stop();

    void addProducer(Recorder producer);

    void setProgressListener(OnRecordProgressListener listener);

    void setFinishListener(OnRecordFinishListener listener);

    void stopFeeding();

}
