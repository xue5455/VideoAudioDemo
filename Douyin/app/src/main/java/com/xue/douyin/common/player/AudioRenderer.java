package com.xue.douyin.common.player;


import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.xue.douyin.common.C;
import com.xue.douyin.common.util.VideoUtil;

import java.io.IOException;

/**
 * Created by 薛贤俊 on 2018/4/3.
 */

public class AudioRenderer {





    private AudioTrack mAudioTrack;
    private MediaFormat mFormat;
    private MediaExtractor mExtractor;
    private MediaCodec mCodec;

    public AudioRenderer(String url) throws IOException {
        prepareExtractor(url);
        prepareCodec(mFormat);
    }

    private void prepareExtractor(String url) throws IOException {
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(url);
        int audioTrack = VideoUtil.selectAudioTrack(mExtractor);
        if (audioTrack == -1) {
            return;
        }
        mExtractor.selectTrack(audioTrack);
        mFormat = mExtractor.getTrackFormat(audioTrack);
    }

    private void prepareCodec(MediaFormat format) throws IOException {
//        mCodec = MediaCodec.createDecoderByType(C.AUDIO_MIME_TYPE);
        mCodec.configure(format, null, null, 0);
    }
}
