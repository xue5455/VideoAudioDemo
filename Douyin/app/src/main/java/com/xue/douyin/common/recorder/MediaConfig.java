package com.xue.douyin.common.recorder;


import com.xue.douyin.common.C;
import com.xue.douyin.common.recorder.audio.AudioConfig;
import com.xue.douyin.common.recorder.video.VideoConfig;

import static com.xue.douyin.common.C.MODE_EXTRA_FAST;
import static com.xue.douyin.common.C.MODE_EXTRA_SLOW;
import static com.xue.douyin.common.C.MODE_FAST;
import static com.xue.douyin.common.C.MODE_NORMAL;
import static com.xue.douyin.common.C.MODE_SLOW;


/**
 * Created by 薛贤俊 on 2018/3/21.
 */

public class MediaConfig {



    private String filePath;

    private VideoConfig videoConfig;

    private AudioConfig audioConfig;

    private long maxDuration;

    private boolean supportAudio;

    private @C.SpeedMode
    int mSpeedMode;

    public MediaConfig(String filePath) {
        this.filePath = filePath;
    }

    public void setSpeedMode(@C.SpeedMode int speedMode) {
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

    public static float getSpeedFactor(@C.SpeedMode int speedMode) {
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
