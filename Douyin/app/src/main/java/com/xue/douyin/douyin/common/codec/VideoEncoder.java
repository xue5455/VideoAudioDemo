package com.xue.douyin.douyin.common.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.view.Surface;

import com.xue.douyin.douyin.common.util.LogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class VideoEncoder extends BaseEncoder<VideoConfig, VideoFrameData> {

    /**
     * H.264 Advanced Video Coding
     */
    private static final String MIME_TYPE = "video/avc";

    /**
     * fps 30
     */
    private static final int FRAME_RATE = 25;

    private static final int I_FRAME_INTERVAL = 10;

    private Surface mInputSurface;

    private MediaCodec.BufferInfo mBufferInfo;

    private OffScreenWrapper mOffScreen;

    private EGLContext mGLContext;

    public VideoEncoder(MovieMuxCore core, VideoConfig config) throws IOException {
        super(core, config);
    }

    @Override
    protected void initCodec(VideoConfig config) throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, config.getVideoWidth(),
                config.getVideoHeight());
        //设置参数
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, (int)(0.25f * FRAME_RATE * config.getVideoWidth() * config.getVideoHeight()));
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        mCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        //FLAG_ENCODE表示这个是一个编码器
        mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //新建一个输入源Surface
        mInputSurface = mCodec.createInputSurface();
        mCodec.start();
        mGLContext = config.getGLContext();
    }

    @Override
    protected void release() {
        if (mCodec == null) {
            return;
        }
        mCodec.stop();
        mCodec.release();
        mCodec = null;
    }

    @Override
    protected void onStart() {
        mOffScreen = new OffScreenWrapper(mGLContext, mInputSurface);
        drain(false);
    }

    @Override
    protected void onStop() {
        drain(true);
    }

    @Override
    protected void onPause() {

    }

    @Override
    protected void onResume() {

    }

    private long prevOutputPTSUs = 0;

    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;

        return result;
    }

    @Override
    protected void onFrameAvailable(VideoFrameData object) {
        mOffScreen.draw(object.getFilter(), object.getMatrix(), object.getTextureId(), object.getTimestamp());
        drain(false);
    }

    private static final int TIME_OUT = 10000;

    private void drain(boolean endOfStream) {
        if (endOfStream) {
            mCodec.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mCodec.getOutputBuffers();
        while (true) {
            int encoderStatus = mCodec.dequeueOutputBuffer(mBufferInfo, TIME_OUT);
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
                mMuxCore.setVideoReady(newFormat);
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
//                    mBufferInfo.presentationTimeUs = getPTSUs();
                    mMuxCore.writeVideo(encodedData, mBufferInfo);
                }
                mCodec.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }

        }
    }
}
