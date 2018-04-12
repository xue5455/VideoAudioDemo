package com.xue.douyin.common.recorder;

import com.xue.douyin.common.C;

/**
 * Created by 薛贤俊 on 2018/4/11.
 */

public class ClipInfo {

    private String fileName;

    private long duration;

    private int type;

    public ClipInfo(String fileName, long duration,@C.DataType int type) {
        this.fileName = fileName;
        this.duration = duration;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public long getDuration() {
        return duration;
    }

    public @C.DataType
    int getType() {
        return type;
    }

}
