package com.xue.douyin.common.recorder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class MediaData implements Parcelable {

    /**
     * 文件地址
     */
    private String mFilePath;

    /**
     * 播放速度
     */
    private int mSpeed;

    private long mDuration;

    public MediaData(String path, long duration,int speed) {
        this.mFilePath = path;
        this.mSpeed = speed;
        this.mDuration = duration;
    }
    public String getFilePath() {
        return mFilePath;
    }
    public int getSpeed() {
        return mSpeed;
    }

    public void setDuration(long duration){
        this.mDuration = duration;
    }

    public long getDuration(){
        return mDuration;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFilePath);
        dest.writeInt(this.mSpeed);
        dest.writeLong(this.mDuration);
    }

    protected MediaData(Parcel in) {
        this.mFilePath = in.readString();
        this.mSpeed = in.readInt();
        this.mDuration = in.readLong();
    }

    public static final Parcelable.Creator<MediaData> CREATOR = new Parcelable.Creator<MediaData>() {
        @Override
        public MediaData createFromParcel(Parcel source) {
            return new MediaData(source);
        }

        @Override
        public MediaData[] newArray(int size) {
            return new MediaData[size];
        }
    };
}
