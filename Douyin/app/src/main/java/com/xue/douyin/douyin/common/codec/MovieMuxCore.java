package com.xue.douyin.douyin.common.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.xue.douyin.douyin.common.util.LogUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/8.
 */

public class MovieMuxCore {

    private static final String TAG = "MovieMuxCore";
    /**
     * 可能存在音频被禁用的情况，此时不需要声音
     */
    private boolean mSupportAudio;

    private static final int MSG_RESUME = 1;

    private static final int MSG_PAUSE = 2;

    private static final int MSG_END = 3;

    private static final int MSG_VIDEO_READY = 4;

    private static final int MSG_AUDIO_READY = 5;

    private static final int MSG_NEW_VIDEO_DATA = 6;

    private static final int MSG_NEW_AUDIO_DATA = 7;

    private boolean mAudioReady;

    private boolean mVideoReady;

    private MovieMuxHandler mMuxHandler;
    /**
     * 视轨
     */
    private int mVideoTrack = -1;
    /**
     * 音轨
     */
    private int mAudioTrack = 1;

    private MediaMuxer mMuxer;

    private boolean mMuxerStarted = false;

    private String mOutputFile;


    private List<MuxerData> mVideoData = new ArrayList<>();
    private List<MuxerData> mAudioData = new ArrayList<>();
    /**
     * 最大视频长度,纳秒
     */
    private long mMaxLength = 0;


    private TimeModifier mVideoModifier;

    private TimeModifier mAudioModifier;

    private OnVideoMaxLengthListener mMaxListener;


    public MovieMuxCore(boolean audioSupport, String path, double speed) {
        this.mSupportAudio = audioSupport;
        mOutputFile = path;
        mVideoModifier = new TimeModifier(speed);
        mAudioModifier = new TimeModifier(speed);
    }

    public void setMaxLength(int seconds, OnVideoMaxLengthListener listener) {
        mMaxLength = seconds * 1000000;
        mMaxListener = listener;
    }

    /**
     * 这个只能在某个线程中启动，多线程不安全
     */
    public void start() {
        if (mMuxHandler != null) {
            return;
        }
        HandlerThread thread = new HandlerThread("MovieMuxCore");
        thread.start();
        mMuxHandler = new MovieMuxHandler(thread.getLooper(), this);
    }

    /**
     * 退出Muxer
     */
    public void quit() {
        if (mMuxHandler == null) {
            return;
        }

        mMuxHandler.getLooper().quitSafely();
    }

    public void resume() {
        if (mMuxHandler == null) {
            return;
        }
        mMuxHandler.sendMessage(mMuxHandler.obtainMessage(MSG_RESUME));
    }

    public void stop() {
        if (mMuxHandler == null) {
            return;
        }
        mMuxHandler.sendMessage(mMuxHandler.obtainMessage(MSG_END));
    }

    public void pause() {
        if (mMuxHandler == null) {
            return;
        }
        mMuxHandler.sendMessage(mMuxHandler.obtainMessage(MSG_PAUSE));
    }

    public void writeVideo(ByteBuffer data, MediaCodec.BufferInfo info) {
        if (mMuxHandler == null) {
            return;
        }
        ByteBuffer copy = ByteBuffer.allocate(data.capacity());
        copy.put(data);
        mMuxHandler.sendMessage(mMuxHandler.obtainMessage(MSG_NEW_VIDEO_DATA,
                new MuxerData(copy, info)));
    }

    public void writeAudio(ByteBuffer data, MediaCodec.BufferInfo info) {
        if (mMuxHandler == null) {
            return;
        }
        ByteBuffer copy = ByteBuffer.allocate(data.capacity());
        copy.put(data);
        mMuxHandler.sendMessage(mMuxHandler.obtainMessage(MSG_NEW_AUDIO_DATA,
                new MuxerData(copy, info)));
    }

    public void setVideoReady(MediaFormat format) {
        if (mMuxHandler == null) {
            return;
        }
        mMuxHandler.sendMessage(mMuxHandler.obtainMessage(MSG_VIDEO_READY, format));
    }

    public void setAudioReady(MediaFormat format) {
        if (mMuxHandler == null) {
            return;
        }
        mMuxHandler.sendMessage(mMuxHandler.obtainMessage(MSG_AUDIO_READY, format));
    }

    private void prepareMuxer() {
        if (mMuxer != null) {
            return;
        }
        try {
            mMuxer = new MediaMuxer(mOutputFile,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            LogUtil.e(TAG, e);
            if (mMuxer == null) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 停止Muxer，录制完成
     */
    private void releaseMuxer() {
        if (!mMuxerStarted || mMuxer == null) {
            return;
        }
        try {
            mMuxer.stop();
            mMuxer.release();
        } catch (Throwable e) {
            LogUtil.e(TAG, e);
        }
        mMuxer = null;
        mMuxerStarted = false;
    }

    private void handlePause() {
        mMuxerStarted = false;
        mVideoModifier.onPause();
        mAudioModifier.onPause();
    }

    private void handleResume() {
        if (mVideoReady && (mAudioReady || !mSupportAudio)) {
            mMuxerStarted = true;
        }
    }

    private void handleVideoReady(MediaFormat format) {
        mVideoReady = true;
        prepareMuxer();
        mVideoTrack = mMuxer.addTrack(format);
        tryStart();
    }

    private void handleAudioReady(MediaFormat format) {
        mAudioReady = true;
        prepareMuxer();
        mAudioTrack = mMuxer.addTrack(format);
        tryStart();
    }

    private void tryStop() {
        if (!mMuxerStarted) {
            return;
        }
        boolean audio = true;
        if (mSupportAudio) {
            audio = mAudioModifier.getCurrentLength() >= mMaxLength;
        }
        if (audio && mVideoModifier.getCurrentLength() >= mMaxLength) {
            stop();
            if (mMaxListener != null) {
                mMaxListener.onReachMaxLength();
            }
        }
    }

    private void tryStart() {
        if (mMuxerStarted) {
            return;
        }
        if (mVideoReady && (mAudioReady || !mSupportAudio)) {
            mMuxer.start();
            mMuxerStarted = true;
        }
    }


    private void handleVideoData(MuxerData data) {
        mVideoModifier.processData(data);
        if (mVideoModifier.getCurrentLength() >= mMaxLength) {
            tryStop();
            return;
        }

        data.data.position(data.info.offset);
        data.data.limit(data.info.offset + data.info.size);

        if (!mMuxerStarted) {
            mVideoData.add(data);
            return;
        }
        if (!mVideoData.isEmpty()) {
            for (MuxerData toAdd : mVideoData) {
                addVideoData(toAdd);
            }
            mVideoData.clear();
        }
        addVideoData(data);
    }

    private int video = 0;
    private void addVideoData(MuxerData data) {
        mMuxer.writeSampleData(mVideoTrack, data.data, data.info);
        video++;
        LogUtil.d("xue","video frames number " + video);
    }

    private void handleAudioData(MuxerData data) {
        mAudioModifier.processData(data);
        if (mAudioModifier.getCurrentLength() >= mMaxLength) {
            tryStop();
            return;
        }

        data.data.position(data.info.offset);
        data.data.limit(data.info.offset + data.info.size);
        mAudioData.add(data);

        if (!mMuxerStarted) {
            mAudioData.add(data);
            return;
        }
        if (!mAudioData.isEmpty()) {
            for (MuxerData toAdd : mAudioData) {
                addAudioData(toAdd);
            }
            mAudioData.clear();
        }
        addAudioData(data);
    }

    private int audio = 0;
    private void addAudioData(MuxerData data) {
        mMuxer.writeSampleData(mAudioTrack, data.data, data.info);
        audio++;
        LogUtil.d("xue","audio frames number " + audio);
    }


    private static class MovieMuxHandler extends Handler {

        WeakReference<MovieMuxCore> ref;

        MovieMuxHandler(Looper looper, MovieMuxCore core) {
            super(looper);
            ref = new WeakReference<>(core);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Object obj = msg.obj;
            MovieMuxCore core = ref.get();
            if (core == null) {
                getLooper().quitSafely();
                return;
            }
            switch (what) {
                case MSG_RESUME:
                    core.handleResume();
                    break;
                case MSG_PAUSE:
                    core.handlePause();
                    break;
                case MSG_END:
                    core.releaseMuxer();
                    break;
                case MSG_VIDEO_READY:
                    core.handleVideoReady((MediaFormat) obj);
                    break;
                case MSG_AUDIO_READY:
                    core.handleAudioReady((MediaFormat) obj);
                    break;
                case MSG_NEW_AUDIO_DATA:
                    core.handleAudioData((MuxerData) obj);
                    break;
                case MSG_NEW_VIDEO_DATA:
                    core.handleVideoData((MuxerData) obj);
                    break;

            }
        }
    }

    public interface OnVideoMaxLengthListener {
        void onReachMaxLength();
    }

    static class MuxerData {
        ByteBuffer data;

        MediaCodec.BufferInfo info;

        public MuxerData(ByteBuffer data, MediaCodec.BufferInfo info) {
            this.data = data;
            this.info = new MediaCodec.BufferInfo();
            this.info.set(info.offset, info.size, info.presentationTimeUs, info.flags);
        }
    }
}
