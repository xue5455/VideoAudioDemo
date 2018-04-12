package com.xue.douyin.common.recorder;

import android.support.annotation.IntDef;

import com.xue.douyin.common.recorder.audio.AudioConfig;
import com.xue.douyin.common.recorder.video.VideoConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 薛贤俊 on 2018/3/21.
 */

public class MediaConfig {

    public static final int MODE_EXTRA_SLOW = 1;

    public static final int MODE_SLOW = 2;

    public static final int MODE_NORMAL = 3;

    public static final int MODE_FAST = 4;

    public static final int MODE_EXTRA_FAST = 5;

    @IntDef({MODE_EXTRA_SLOW, MODE_SLOW, MODE_NORMAL, MODE_FAST, MODE_EXTRA_FAST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SpeedMode {

    }

    private String filePath;

    private VideoConfig videoConfig;

    private AudioConfig audioConfig;

    private long maxDuration;

    private boolean supportAudio;

    private @SpeedMode
    int mSpeedMode;

    public MediaConfig(String filePath) {
        this.filePath = filePath;
    }

    public void setSpeedMode(@SpeedMode int speedMode) {
        this.mSpeedMode = speedMode;
    }

    public void setSupportAudio(boolean supportAudio) {
        this.supportAudio = supportAudio;
    }

    public void configure(AudioConfig audio, VideoConfig video) {
        this.audioConfig = audio;
        this.videoConfig = video;
    }

    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getSpeedMode() {
        return mSpeedMode;
    }

    public VideoConfig getVideoConfig() {
        return videoConfig;
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    public boolean supportAudio() {
        return supportAudio;
    }

    public float getSpeedFactor() {
        return getSpeedFactor(mSpeedMode);
    }

    public static float getSpeedFactor(@SpeedMode int speedMode) {
        switch (speedMode) {
            case MODE_EXTRA_SLOW:
                return 1f / 3;
            case MODE_SLOW:
                return 0.5f;
            case MODE_NORMAL:
                return 1f;
            case MODE_FAST:
                return 2f;
            case MODE_EXTRA_FAST:
                return 3f;
        }
        return 1;
    }
}
