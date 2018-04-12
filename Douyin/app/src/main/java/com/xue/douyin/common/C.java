package com.xue.douyin.common;

import android.media.AudioFormat;

import com.xue.douyin.common.util.StorageUtil;

import java.io.File;

/**
 * Created by 薛贤俊 on 2018/4/7.
 */

public class C {

    private C() {

    }

    public static final int SECOND_IN_US = 1000000;

    public static final int BUFFER_TIME_OUT = 10000;

    public static final class AudioParams {
        public static final int SAMPLE_PER_FRAME = 1024;
        public static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;

        public static final int CHANNEL_COUNT = 1;

        public static final int BITS_PER_SAMPLE = AudioFormat.ENCODING_PCM_16BIT;

        public static final int BIT_RATE = 64000;

        public static final int SAMPLE_RATE = 44100;

        public static final String MIME_TYPE = "audio/mp4a-latm";
    }


    public static final class VideoParams {

        public static final String MIME_TYPE = "video/avc";

        public static final int SAMPLE_RATE = 30;

        public static final int I_FRAME_INTERVAL = 10;

        public static final int BIT_RATE = 1000000;
    }


    public static final String AUDIO_TEMP_FILE_NAME = StorageUtil.getExternalStoragePath() +
            File.separator + "tmp12" + ".aac";

    public static final String VIDEO_TEMP_FILE_NAME = StorageUtil.getExternalStoragePath() +
            File.separator + "tmp1" + ".mp4";
}
