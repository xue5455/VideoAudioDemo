package com.xue.douyin.common.recorder;

import com.xue.douyin.common.recorder.tst.Consumer;

/**
 * Created by 薛贤俊 on 2018/4/11.
 */

public class ClipInfo {

    private String fileName;

    private @MediaConfig.SpeedMode
    int speedMode;

    private long duration;

    private int type;

    public ClipInfo(String fileName, long duration, int speedMode, @Consumer.DataType int type) {
        this.fileName = fileName;
        this.speedMode = speedMode;
        this.duration = duration;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public int getSpeedMode() {
        return speedMode;
    }

    public long getDuration() {
        return duration;
    }

    public @Consumer.DataType
    int getType() {
        return type;
    }

}
