package com.xue.douyin.common.recorder;

import android.media.*;
import android.media.MediaRecorder;
import android.os.Handler;

import com.netease.soundtouch.SoundTouch;
import com.xue.douyin.common.C;
import com.xue.douyin.common.recorder.audio.AudioConfig;
import com.xue.douyin.common.util.FileUtils;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.ThreadUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.xue.douyin.common.C.BUFFER_TIME_OUT;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public class AudioRecorder implements Recorder<AudioConfig>, Runnable {

    private AudioRecord recorder;

    private Handler recordHandler;

    private Handler encodeHandler;

    private MediaCodec encoder;

    private MediaCodec.BufferInfo bufferInfo;

    private AudioConfig configuration;

    private SoundTouch soundTouch;

    private boolean started = false;

    private BufferedOutputStream fileStream;

    private OnRecordFinishListener finishListener;

    private long sampleDuration;

    private long duration;

    @Override
    public int getDataType() {
        return C.AUDIO;
    }

    @Override
    public void setOnRecordFinishListener(OnRecordFinishListener listener) {
        finishListener = listener;
    }

    @Override
    public void configure(AudioConfig configuration) {
        this.configuration = configuration;
        int bufferSize = AudioRecord.getMinBufferSize(
                configuration.getSampleRate(), C.AudioParams.CHANNEL,
                C.AudioParams.BITS_PER_SAMPLE);
        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC, configuration.getSampleRate(),
                C.AudioParams.CHANNEL, C.AudioParams.BITS_PER_SAMPLE, bufferSize);
        recordHandler = ThreadUtil.newHandlerThread("record");
        encodeHandler = ThreadUtil.newHandlerThread("encode");
        soundTouch = new SoundTouch();
        soundTouch.setPitchSemiTones(1.0f);
        soundTouch.setTempo(MediaConfig.getSpeedFactor(configuration.getSpeedMode()));
        sampleDuration = configuration.getSamplePerFrame() * C.SECOND_IN_US /
                configuration.getSampleRate();
        duration = 0;

    }

    private void loop() {
        recordHandler.post(this);
    }

    @Override
    public void start() {
        recorder.startRecording();
        encoder.start();
        loop();
        started = true;
    }

    @Override
    public void stop() {
        if (recordHandler == null) {
            return;
        }
        started = false;
        recordHandler.post(new Runnable() {
            @Override
            public void run() {
                onStop();
            }
        });
    }

    private void onStop() {
        if (recorder == null) {
            return;
        }
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    @Override
    public void prepareCodec() throws IOException {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(C.AudioParams.MIME_TYPE,
                C.AudioParams.SAMPLE_RATE, C.AudioParams.CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, C.AudioParams.CHANNEL);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, C.AudioParams.BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, C.AudioParams.CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 4);
        encoder = MediaCodec.createEncoderByType(C.AudioParams.MIME_TYPE);
        encoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        bufferInfo = new MediaCodec.BufferInfo();
        fileStream = new BufferedOutputStream(new FileOutputStream(configuration.getFileName()));
    }

    @Override
    public void shutdown() {
        if (encodeHandler != null) {
            encodeHandler.getLooper().quitSafely();
            encodeHandler = null;
        }
        if (recordHandler != null) {
            recordHandler.getLooper().quitSafely();
            recordHandler = null;
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }


    @Override
    public void run() {
        //为空，说明已经停止了
        if (recorder == null) {
            //通知编码线程退出
            stopEncode();
            recordHandler.getLooper().quitSafely();
            recordHandler = null;
            return;
        }
        byte[] buffer = new byte[configuration.getSamplePerFrame()];
        int bytes = recorder.read(buffer, 0, buffer.length);
        if (bytes > 0) {
            encode(buffer, bytes);
        }
        loop();
    }


    private void stopEncode() {
        encodeHandler.post(new Runnable() {
            @Override
            public void run() {
                if (encoder == null) {
                    return;
                }
                encoder.stop();
                encoder.release();
                encoder = null;
                FileUtils.closeSafely(fileStream);
                fileStream = null;
                soundTouch.close();
                soundTouch = null;
                if (finishListener != null) {
                    finishListener.onRecordFinish(new ClipInfo(configuration.getFileName(),
                            duration, getDataType()));
                }
                encodeHandler.getLooper().quitSafely();
                encodeHandler = null;
            }
        });
    }

    private void encode(final byte[] data, final int length) {

        encodeHandler.post(new Runnable() {
            @Override
            public void run() {
                if (soundTouch != null) {
                    soundTouch.putBytes(data);
                    while (true) {
                        byte[] modified = new byte[4096];
                        int count = soundTouch.getBytes(modified);
                        if (count > 0) {
                            onEncode(modified, count * 2);
                            drain();
                        } else {
                            break;
                        }
                    }
                } else {
                    onEncode(data, length);
                    drain();
                }
            }
        });
    }

    private long getTimeUs() {
        return System.nanoTime() / 1000L;
    }

    private void onEncode(byte[] data, int length) {
        final ByteBuffer[] inputBuffers = encoder.getInputBuffers();
        while (true) {
            final int inputBufferIndex = encoder.dequeueInputBuffer(BUFFER_TIME_OUT);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.position(0);
                if (data != null) {
                    inputBuffer.put(data, 0, length);
                }
                if (length <= 0) {
                    encoder.queueInputBuffer(inputBufferIndex, 0, 0,
                            getTimeUs(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    encoder.queueInputBuffer(inputBufferIndex, 0, length,
                            getTimeUs(), 0);
                }
                break;
            }
        }
    }

    private void drain() {
        bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
        int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, C.BUFFER_TIME_OUT);
        while (encoderStatus >= 0) {
            ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
            int outSize = bufferInfo.size;
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            byte[] data = new byte[outSize + 7];
            addADTSHeader(data, outSize + 7);
            encodedData.get(data, 7, outSize);
            try {
                fileStream.write(data, 0, data.length);
                fileStream.flush();
                duration += sampleDuration;
            } catch (IOException e) {
                LogUtil.e(e);
            }
            if (duration >= configuration.getMaxDuration()) {
                stop();
            }
            encoder.releaseOutputBuffer(encoderStatus, false);
            encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, C.BUFFER_TIME_OUT);
        }
    }

    private void addADTSHeader(byte[] packet, int length) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 1; // CPE
        // fill in A D T S data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (length >> 11));
        packet[4] = (byte) ((length & 0x7FF) >> 3);
        packet[5] = (byte) (((length & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

}
