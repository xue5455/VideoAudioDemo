package com.xue.douyin.common.recorder;

import com.xue.douyin.common.C;

import java.io.IOException;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public interface Recorder<T> {



    @C.DataType
    int getDataType();

    void setOnRecordFinishListener(OnRecordFinishListener listener);

    void configure(T configuration);

    void start();

    void stop();

    void prepareCodec() throws IOException;

    void shutdown();

    boolean isStarted();
}
