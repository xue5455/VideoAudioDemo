package com.xue.douyin.common.recorder.tst;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;

import com.netease.soundtouch.SoundTouch;
import com.xue.douyin.common.recorder.OnRecordFinishListener;
import com.xue.douyin.common.util.FileUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.xue.douyin.common.recorder.MediaConfig.MODE_NORMAL;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public class AudioEncoder {

    private String mFileName;

    private Handler mHandler;

    private boolean mStarted = false;

    private MediaCodec mCodec;

    private BufferedOutputStream mStream;

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private static final String MIME_TYPE = "audio/mp4a-latm";

    private OnRecordFinishListener mFinishListener;

    public AudioEncoder(String fileName, OnRecordFinishListener listener) {
        this.mFileName = fileName;
        mFinishListener = listener;
    }


    private void prepare() throws IOException {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, AudioSource.SAMPLE_RATE, 1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioSource.CHANNEL);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AudioSource.BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, AudioSource.CHANNEL_COUNT);
        mCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mCodec.start();
    }

    public void start() throws IOException {
        if (mStarted) {
            return;
        }
        FileUtils.createFile(mFileName);
        HandlerThread thread = new HandlerThread(getClass().getSimpleName());
        thread.start();
        mHandler = new Handler(thread.getLooper());
        mStream = new BufferedOutputStream(new FileOutputStream(mFileName));
        prepare();
        mStarted = true;
    }

    public void stop() {
        if (!mStarted) {
            return;
        }
        mStarted = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCodec.stop();
                mCodec.release();
                mCodec = null;
                if (mFinishListener != null) {
//                    mFinishListener.onRecordFinish(mFileName, 0, MODE_NORMAL);
                }
                mHandler.getLooper().quitSafely();
            }
        });
    }

    private long getTimeUs() {
        return System.nanoTime() / 1000L;
    }


    public void encode(final byte[] data) {
        if (!mStarted) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                encode(data, data.length);
                drain();
            }
        });
    }

    private void drain() {
        ByteBuffer[] encoderOutputBuffers = mCodec.getOutputBuffers();
        int encoderStatus = mCodec.dequeueOutputBuffer(mBufferInfo, 10000);
        while (encoderStatus >= 0) {
            int outSize = mBufferInfo.size;
            ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
            byte[] data = new byte[outSize + 7];
            addADTStoPacket(data, outSize + 7);
            encodedData.get(data, 7, mBufferInfo.size);
            try {
                mStream.write(data, 0, data.length);
                mStream.flush();
            } catch (IOException e) {

            }
            mCodec.releaseOutputBuffer(encoderStatus, false);
            encoderStatus = mCodec.dequeueOutputBuffer(mBufferInfo, 10000);
        }
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 1; // CPE
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }


    private void encode(byte[] data, int length) {
        final ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        while (true) {
            final int inputBufferIndex = mCodec.dequeueInputBuffer(10000);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (data != null) {
                    inputBuffer.put(data, 0, length);
                }
                if (length <= 0) {
                    mCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            getTimeUs(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            getTimeUs(), 0);
                }
                break;
            }
        }
    }

}
