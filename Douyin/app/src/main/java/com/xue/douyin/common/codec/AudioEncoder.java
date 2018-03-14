package com.xue.douyin.common.codec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import com.xue.douyin.common.util.LogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class AudioEncoder extends BaseEncoder<AudioConfig, Object> {

    private static final String MIME_TYPE = "audio/mp4a-latm";

    private static final int SAMPLE_RATE = 44100;

    private static final int BIT_RATE = 64000;

    public static final int SAMPLES_PER_FRAME = 1024;    // AAC, bytes/frame/channel

    public static final int FRAMES_PER_BUFFER = 1;    // AAC, frame/buffer/sec

    private AudioRecord mRecorder;

    private MediaCodec.BufferInfo mBufferInfo;

    public AudioEncoder(MovieMuxCore core, AudioConfig config) throws IOException {
        super(core, config);
    }

    @Override
    protected void initCodec(AudioConfig config) throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, 1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        mCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mCodec.start();
        initRecorder();
    }

    private void initRecorder() {
        int minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int bufferSize = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
        if (bufferSize < minBufferSize)
            bufferSize = ((minBufferSize / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;
        mRecorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }

    @Override
    protected void release() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mCodec != null) {
            mCodec.stop();
            mCodec.release();
            mCodec = null;
        }
    }

    @Override
    protected void onStart() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.startRecording();
        batch();
    }

    @Override
    protected void onStop() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.stop();
        stopBatch();
        drain(true);
    }


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            drawAudioData();
            batch();
        }
    };

    private void stopBatch() {
        if (mHandler == null) {
            return;
        }
        mHandler.removeCallbacks(mRunnable);
    }

    private void batch() {
        if (mHandler == null || !mStarted) {
            return;
        }
        mHandler.post(mRunnable);
    }

    private void drawAudioData() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
        int readBytes = mRecorder.read(buffer, SAMPLES_PER_FRAME);
        if (readBytes > 0) {
            // set audio data to encoder
            encode(buffer, readBytes, getPTSUs());
            drain(false);
        }
    }

    private void drain(boolean endOfStream) {
        ByteBuffer[] encoderOutputBuffers = mCodec.getOutputBuffers();
        while (true) {
            int encoderStatus = mCodec.dequeueOutputBuffer(mBufferInfo, TIME_OUT);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat format = mCodec.getOutputFormat();
                mMuxCore.setAudioReady(format);
            } else if (encoderStatus < 0) {
                LogUtil.e(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    encodedData.position(mBufferInfo.offset);
                    mMuxCore.writeAudio(encodedData, mBufferInfo);
                    // write encoded data to muxer(need to adjust presentationTimeUs.
                }
                mCodec.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }
    }

    protected void encode(ByteBuffer buffer, int length, long presentationTimeUs) {
        final ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        while (true) {
            final int inputBufferIndex = mCodec.dequeueInputBuffer(TIME_OUT);
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

}
