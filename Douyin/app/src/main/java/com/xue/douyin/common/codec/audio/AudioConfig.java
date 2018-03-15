package com.xue.douyin.common.codec.audio;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class AudioConfig {

    private String mOutputFile;

    private float mSpeed;

    public AudioConfig(String fileName, float speed) {
        mOutputFile = fileName;
        mSpeed = speed;
    }

    public String getFileName() {
        return mOutputFile;
    }

    public float getSpeed() {
        return mSpeed;
    }
}
