package com.xue.douyin.common.codec;

/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class MediaData {

    /**
     * 文件地址
     */
    private String mFilePath;

    /**
     * 是否是视频
     */
    private boolean mVideo;

    /**
     * 播放速度
     */
    private float mSpeed;

    public MediaData(String path, boolean video, float speed) {
        this.mFilePath = path;
        this.mVideo = video;
        this.mSpeed = speed;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public boolean isVideo() {
        return mVideo;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float speed){
        this.mSpeed = speed;
    }
}
