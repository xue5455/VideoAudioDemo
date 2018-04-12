package com.xue.douyin.common.recorder.tst;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.HandlerThread;

import com.xue.douyin.common.util.LogUtil;

import java.io.IOException;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public class MicAudioSource implements AudioSource {

    private static final int SAMPLE_PER_FRAME = 1024;

    private AudioRecord mAudioRecord;

    private boolean mStarted = false;

    private int mBufferSize;

    private AudioEncoder mEncoder;

    private Handler mHandler;

    @Override
    public void start() {
        if (mStarted) {
            return;
        }
        try {
            mEncoder.start();
        } catch (IOException e) {
            LogUtil.e(e);
            return;
        }
        prepareAudio();
        mStarted = true;
        HandlerThread audioThread = new HandlerThread(getClass().getSimpleName());
        audioThread.start();
        mHandler = new Handler(audioThread.getLooper());
        loop();
    }

    @Override
    public void stop() {
        if (!mStarted) {
            return;
        }
        mAudioRecord.stop();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mStarted = false;
                mEncoder.stop();
            }
        });
    }

    @Override
    public void setEncoder(AudioEncoder encoder) {
        mEncoder = encoder;
    }

    private void prepareAudio() {
        if (mAudioRecord != null) {
            return;
        }

        mBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, CHANNEL,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                CHANNEL, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);
        mAudioRecord.startRecording();
    }

    private void loop() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mStarted) {
                    mHandler.getLooper().quitSafely();
                    return;
                }
                drawData();
                loop();
            }
        });
    }

    private void drawData() {
        byte[] data = new byte[SAMPLE_PER_FRAME];
        int readBytes = mAudioRecord.read(data, 0, SAMPLE_PER_FRAME);
        if (readBytes <= 0) {
            return;
        }
        mEncoder.encode(data);
    }
}
