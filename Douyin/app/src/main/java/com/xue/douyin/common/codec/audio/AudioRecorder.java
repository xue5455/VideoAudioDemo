package com.xue.douyin.common.codec.audio;

import android.media.*;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.xue.douyin.common.codec.OnRecordFinishListener;
import com.xue.douyin.common.util.LogUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class AudioRecorder implements Runnable {
    private static final int MSG_START = 1;

    private static final int MSG_STOP = 2;

    private static final int SAMPLE_RATE = 44100;

    private static final int BIT_RATE = 64000;

    private static final int SAMPLE_PER_FRAME = 1024;

    private int mBufferSize;

    private AudioRecord mRecorder;

    private MediaCodec.BufferInfo mBufferInfo;

    private BufferedOutputStream mBufferStream;

    private boolean mStarted;

    private MediaCodec mCodec;

    private H mHandler;

    private long mDuration;

    private OnRecordFinishListener mFinishListener;

    private AudioConfig mConfig;

    private MediaMuxer mMuxer;

    private int mAudioTrack;

    public AudioRecorder() {
        HandlerThread thread = new HandlerThread("AudioRecorder");
        thread.start();
        mHandler = new H(thread.getLooper(), this);
    }

    public void setOnFinishListener(OnRecordFinishListener listener) {
        this.mFinishListener = listener;
    }

    private void prepare(AudioConfig config) throws Exception {
        if (mRecorder != null) {
            return;
        }
        MediaFormat audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", SAMPLE_RATE, 1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        mCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        mCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        mBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mRecorder = new AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);

        mConfig = config;
        prepareMuxer(config.getFileName());
    }

    private void prepareMuxer(String fileName) {
        if (mMuxer != null) {
            return;
        }
        try {
            mMuxer = new MediaMuxer(fileName,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            LogUtil.e(e);
            if (mMuxer == null) {
                throw new RuntimeException(e);
            }
        }
    }


    private void drawAudioData() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(SAMPLE_PER_FRAME);
        int readBytes = mRecorder.read(buffer, SAMPLE_PER_FRAME);
        if (readBytes > 0) {
            // set audio data to encoder
            encode(buffer, readBytes, System.nanoTime() / 1000L);
            drain();
        }
    }



    private void drain() {
        mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] encoderOutputBuffers = mCodec.getOutputBuffers();
        int encoderStatus = mCodec.dequeueOutputBuffer(mBufferInfo, 10000);
        while (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED || encoderStatus >= 0) {
            if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mCodec.getOutputFormat();
                mAudioTrack = mMuxer.addTrack(newFormat);
                mMuxer.start();
            } else if (encoderStatus >= 0) {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                encodedData.position(mBufferInfo.offset);
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                mMuxer.writeSampleData(mAudioTrack, encodedData, mBufferInfo);
                mCodec.releaseOutputBuffer(encoderStatus, false);
            }
            encoderStatus = mCodec.dequeueOutputBuffer(mBufferInfo, 10000);
        }
    }

    protected void encode(ByteBuffer buffer, int length, long presentationTimeUs) {
        final ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        while (true) {
            final int inputBufferIndex = mCodec.dequeueInputBuffer(10000);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }
                if (length <= 0) {
                    mCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            presentationTimeUs, 0);
                }
                break;
            }
        }
    }


    public void start(AudioConfig config) {
        if (mStarted) {
            return;
        }
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_START, config));
    }

    public void stop() {
        if (!mStarted) {
            return;
        }
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STOP),500);
    }

    public void quit() {
        mHandler.getLooper().quitSafely();
        mHandler = null;
    }


    private void batch() {
        if (mHandler == null || !mStarted) {
            return;
        }
        mHandler.post(this);
    }

    private void onStart(AudioConfig config) {
        try {
            prepare(config);
            mCodec.start();
            mRecorder.startRecording();
            mStarted = true;
            batch();
        } catch (Exception e) {
            LogUtil.e(e);
        }
    }

    private void onStop() {
        mStarted = false;
        mHandler.removeCallbacks(this);
        mCodec.stop();
        mCodec.release();
        mRecorder.stop();
        mRecorder.release();
        mMuxer.stop();
        mMuxer.release();
        mMuxer = null;
        mCodec = null;
        mRecorder = null;
        if (mFinishListener != null) {
            mFinishListener.onRecordFinish(mConfig.getFileName(), false, mConfig.getSpeed());
        }
    }

    private void onFrameAvailable() {
        drawAudioData();
    }

    @Override
    public void run() {
        onFrameAvailable();
        batch();
    }

    public long getDuration() {
        return mDuration;
    }

    private static class H extends Handler {
        WeakReference<AudioRecorder> ref;

        public H(Looper looper, AudioRecorder ref) {
            super(looper);
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            AudioRecorder ref = this.ref.get();
            if (ref == null) {
                return;
            }
            switch (what) {
                case MSG_START:
                    ref.onStart((AudioConfig) msg.obj);
                    break;
                case MSG_STOP:
                    ref.onStop();
                    break;
            }
        }
    }
}
