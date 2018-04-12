package com.xue.douyin.common.recorder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.view.Surface;

import com.xue.douyin.common.C;
import com.xue.douyin.common.recorder.tst.Consumer;
import com.xue.douyin.common.recorder.video.OffScreenWrapper;
import com.xue.douyin.common.recorder.video.VideoConfig;
import com.xue.douyin.common.recorder.video.VideoFrameData;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.ThreadUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.xue.douyin.common.recorder.MediaConfig.MODE_NORMAL;

/**
 * Created by 薛贤俊 on 2018/4/11.
 */

public class VideoRecorder implements Recorder<VideoConfig> {

    private Handler handler;

    private Surface inputSurface;

    private OffScreenWrapper offScreen;

    private EGLContext glContext;

    private MediaCodec encoder;

    private VideoConfig configuration;

    private boolean started = false;

    private MediaCodec.BufferInfo bufferInfo;

    private MediaMuxer muxer;

    private OnRecordFinishListener listener;

    private OnRecordProgressListener progressListener;

    private int track;

    private float factor;


    private long duration;

    private long startTimeStamp;

    @Override
    public int getDataType() {
        return Consumer.VIDEO;
    }

    @Override
    public void setOnRecordFinishListener(OnRecordFinishListener listener) {
        this.listener = listener;
    }


    public void setOnProgressListener(OnRecordProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public void configure(VideoConfig configuration) {
        this.configuration = configuration;
        handler = ThreadUtil.newHandlerThread("video");
        factor = configuration.getFactor();
        duration = 0;
        startTimeStamp = 0;
    }

    @Override
    public void start() {
        if (started) {
            throw new RuntimeException("VideoRecorder is already started");
        }
        if (encoder == null) {
            return;
        }

        started = true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStart();
            }
        });
    }

    private void onStart() {
        offScreen = new OffScreenWrapper(glContext, inputSurface);
        encoder.start();
    }

    @Override
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStop();
            }
        });
    }

    private void onStop() {
        LogUtil.d("video stop");
        drain(true);
        encoder.stop();
        encoder.release();
        encoder = null;
        muxer.stop();
        muxer.release();
        muxer = null;
        if (listener != null) {
            listener.onRecordFinish(new ClipInfo(configuration.getFileName(), duration, MODE_NORMAL, getDataType()));
        }
        offScreen = null;
        inputSurface = null;
        glContext = null;
        configuration = null;
        handler.getLooper().quitSafely();
        handler = null;
    }


    @Override
    public void prepareCodec() throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat(C.VideoParams.MIME_TYPE,
                configuration.getVideoWidth(),
                configuration.getVideoHeight());
        //设置参数
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, C.VideoParams.BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, C.VideoParams.SAMPLE_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, C.VideoParams.I_FRAME_INTERVAL);
        encoder = MediaCodec.createEncoderByType(C.VideoParams.MIME_TYPE);
        //FLAG_ENCODE表示这个是一个编码器
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = encoder.createInputSurface();
        glContext = configuration.getGLContext();
        bufferInfo = new MediaCodec.BufferInfo();
        muxer = new MediaMuxer(configuration.getFileName(),
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    @Override
    public void shutdown() {
        if (handler != null) {
            handler.getLooper().quitSafely();
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    public void frameAvailable(final VideoFrameData data) {
        if (!started) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFrameAvailable(data);
            }
        });
    }

    private void onFrameAvailable(VideoFrameData data) {
        if (offScreen == null) {
            return;
        }
        offScreen.draw(data.getFilter(), data.getMatrix(), data.getTextureId(), data.getTimestamp());
        drain(false);
    }

    private void drain(boolean endOfStream) {
        if (endOfStream) {
            encoder.signalEndOfInputStream();
        }
        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
        while (true) {
            int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, C.BUFFER_TIME_OUT);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //没有数据
                if (!endOfStream) {
                    break;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = encoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = encoder.getOutputFormat();
                track = muxer.addTrack(newFormat);
                muxer.start();
            } else if (encoderStatus < 0) {
                LogUtil.e("unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    adaptTimeUs(bufferInfo);
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    muxer.writeSampleData(track, encodedData, bufferInfo);
                    if (progressListener != null) {
                        progressListener.onRecordProgress(duration);
                    }
                    if (duration >= configuration.getMaxDuration()) {
                        stop();
                    }
                }
                encoder.releaseOutputBuffer(encoderStatus, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }

        }
    }


    private void adaptTimeUs(MediaCodec.BufferInfo info) {
        info.presentationTimeUs = (long) (info.presentationTimeUs / factor);
        if (startTimeStamp == 0) {
            startTimeStamp = info.presentationTimeUs;
        } else {
            duration = info.presentationTimeUs - startTimeStamp;
        }
    }
}
