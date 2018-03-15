package com.xue.douyin.common.codec.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import com.xue.douyin.common.codec.OnRecordProgressListener;
import com.xue.douyin.common.codec.OnRecordFinishListener;
import com.xue.douyin.common.util.LogUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class VideoRecorder {

    private static final int MSG_START = 1;

    private static final int MSG_STOP = 2;

    private static final int MSG_ON_NEW_FRAME = 3;

    /**
     * H.264 Advanced Video Coding
     */
    private static final String MIME_TYPE = "video/avc";

    /**
     * fps 30
     */
    private static final int FRAME_RATE = 30;

    private static final int I_FRAME_INTERVAL = 10;

    private Surface mInputSurface;

    private MediaCodec.BufferInfo mBufferInfo;

    private OffScreenWrapper mOffScreen;

    private EGLContext mGLContext;

    private MediaCodec mCodec;

    private H mHandler;

    private MediaMuxer mMuxer;

    private boolean mStarted = false;

    private int mTrack = -1;

    private TimeModifier mTimer;

    private OnRecordProgressListener mProgressListener;

    private OnRecordFinishListener mFinishListener;

    private VideoConfig mConfig;

    public VideoRecorder() {
        HandlerThread thread = new HandlerThread("");
        thread.start();
        mHandler = new H(thread.getLooper(), this);
    }

    private void initCodec(VideoConfig config) throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, config.getVideoWidth(),
                config.getVideoHeight());
        //设置参数
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, config.getBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        mCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        //FLAG_ENCODE表示这个是一个编码器
        mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //新建一个输入源Surface
        mInputSurface = mCodec.createInputSurface();
        mCodec.start();
        mGLContext = config.getGLContext();
        mConfig = config;
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

    private void releaseCodec() {
        if (mCodec == null) {
            return;
        }
        mCodec.stop();
        mCodec.release();
        mCodec = null;
    }

    /**
     * 停止Muxer，录制完成
     */
    private void releaseMuxer() {
        if (mMuxer == null) {
            return;
        }
        try {
            mMuxer.stop();
            mMuxer.release();
        } catch (Throwable e) {
            LogUtil.e(e);
        }
        mMuxer = null;
    }


    public void start(VideoConfig config) {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_START, config));
    }

    public void stop() {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP));
    }

    public void frameAvailable(VideoFrameData object) {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_ON_NEW_FRAME, object));
    }

    public void quit() {
        mHandler.removeMessages(MSG_ON_NEW_FRAME);
        mHandler.getLooper().quitSafely();
        mHandler = null;
    }

    private void onStart(VideoConfig config) {
        if (mStarted) {
            return;
        }
        try {
            initCodec(config);
            prepareMuxer(config.getOuputFile());
            mStarted = true;
        } catch (IOException e) {
            LogUtil.e(e);
        }
        mOffScreen = new OffScreenWrapper(mGLContext, mInputSurface);
        mTimer = new TimeModifier(config.getSpeed());
        mTimer.setMaxLength(config.getMaxLength());
    }

    private void onStop() {
        if (!mStarted) {
            return;
        }
        mStarted = false;
        drain(true);
        releaseCodec();
        releaseMuxer();
        if (mFinishListener != null) {
            mFinishListener.onRecordFinish(mConfig.getOuputFile(), true, mConfig.getSpeed());
        }
    }

    private void onFrameAvailable(VideoFrameData object) {
        if (!mStarted) {
            return;
        }
        if (mOffScreen == null) {
            return;
        }
        mOffScreen.draw(object.getFilter(), object.getMatrix(), object.getTextureId(), object.getTimestamp());
        drain(false);
    }

    private void drain(boolean endOfStream) {
        if (endOfStream) {
            mCodec.signalEndOfInputStream();
        }
        ByteBuffer[] encoderOutputBuffers = mCodec.getOutputBuffers();
        while (true) {
            int encoderStatus = mCodec.dequeueOutputBuffer(mBufferInfo, 10000);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //没有数据
                if (!endOfStream) {
                    break;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mCodec.getOutputFormat();
                mTrack = mMuxer.addTrack(newFormat);
                mMuxer.start();
            } else if (encoderStatus < 0) {
                LogUtil.e("unexpected result from encoder.dequeueOutputBuffer: " +
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
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    mTimer.processData(mBufferInfo);
                    mMuxer.writeSampleData(mTrack, encodedData, mBufferInfo);
                    if (mProgressListener != null) {
                        mProgressListener.onRecordProgress(mTimer.getDuration());
                    }
                }
                mCodec.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }

        }
    }

    public void setOnProgressListener(OnRecordProgressListener listener) {
        this.mProgressListener = listener;
    }

    public void setOnFinishListener(OnRecordFinishListener listener) {
        this.mFinishListener = listener;
    }

    public long getDuration() {
        return mTimer.getDuration();
    }


    private static class H extends Handler {
        WeakReference<VideoRecorder> ref;

        public H(Looper looper, VideoRecorder ref) {
            super(looper);
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            VideoRecorder ref = this.ref.get();
            if (ref == null) {
                return;
            }
            switch (what) {
                case MSG_START:
                    ref.onStart((VideoConfig) msg.obj);
                    break;
                case MSG_STOP:
                    ref.onStop();
                    break;
                case MSG_ON_NEW_FRAME:
                    ref.onFrameAvailable((VideoFrameData) msg.obj);
                    break;
            }
        }
    }

}
