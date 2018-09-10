package com.xue.douyin.common;

import android.media.AudioFormat;
import android.support.annotation.IntDef;

import com.xue.douyin.common.util.StorageUtil;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

        public static final int I_FRAME_INTERVAL = 0;
        /**
         * 16*1000 bps：可视电话质量
         * 128-384 * 1000 bps：视频会议系统质量
         * 1.25 * 1000000 bps：VCD质量（使用MPEG1压缩）
         * 5 * 1000000 bps：DVD质量（使用MPEG2压缩）
         * 8-15 * 1000000 bps：高清晰度电视（HDTV） 质量（使用H.264压缩）
         * 29.4  * 1000000 bps：HD DVD质量
         * 40 * 1000000 bps：蓝光光碟质量（使用MPEG2、H.264或VC-1压缩）
         */
        public static final int BIT_RATE = 15 * 1000000;
    }


    public static final String AUDIO_TEMP_FILE_NAME = StorageUtil.getExternalStoragePath() +
            File.separator + "tmp" + ".aac";

    public static final String VIDEO_TEMP_FILE_NAME = StorageUtil.getExternalStoragePath() +
            File.separator + "tmp" + ".mp4";

    public static final int MODE_EXTRA_SLOW = 1;

    public static final int MODE_SLOW = 2;

    public static final int MODE_NORMAL = 3;

    public static final int MODE_FAST = 4;

    public static final int MODE_EXTRA_FAST = 5;

    @IntDef({MODE_EXTRA_SLOW, MODE_SLOW, MODE_NORMAL, MODE_FAST, MODE_EXTRA_FAST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SpeedMode {

    }
    public static final int VIDEO = 1;

    public static final int AUDIO = 2;

    @IntDef({VIDEO, AUDIO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DataType {

    }

}
