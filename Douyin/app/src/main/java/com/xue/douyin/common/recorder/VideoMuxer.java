package com.xue.douyin.common.recorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.xue.douyin.common.recorder.audio.AudioCalculator;
import com.xue.douyin.common.recorder.video.VideoCalculator;
import com.xue.douyin.common.util.FileUtils;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.StorageUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/17.
 */

public class VideoMuxer {

    private static final int MSG_START = 1;

    private static final int MSG_WRITE_VIDEO = 3;

    private static final int MSG_WRITE_AUDIO = 4;

    private static final int MSG_VIDEO_READY = 5;

    private static final int MSG_AUDIO_READY = 6;

    private static final int MSG_AUDIO_STOP = 7;

    private static final int MSG_VIDEO_STOP = 8;

    private MediaMuxer mMuxer;

    private boolean mVideoReady;

    private boolean mAudioReady;

    private int mVideoTrack;

    private int mAudioTrack;

    private H mHandler;

    private boolean mStarted = false;

    private boolean mMuxerStarted = false;

    private long mMaxDuration;

    private List<Data> mAudioDataList = new ArrayList<>();

    private List<Data> mVideoDataList = new ArrayList<>();

    private OnRecordFinishListener mFinishListener;

    private OnRecordProgressListener mProgressListener;

    private MediaConfig mConfig;

    private VideoCalculator mVideoTimer;

    private AudioCalculator mAudioTimer;


    public VideoMuxer() {
        HandlerThread thread = new HandlerThread("VideoMuxer");
        thread.start();
        mHandler = new H(thread.getLooper(), this);
    }

    public void start(MediaConfig config) {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_START, config));
    }

    private void onStart(MediaConfig config) {
        if (mStarted) {
            return;
        }
        mStarted = true;
        mMaxDuration = 0;
        try {
            prepareMuxer(config.getFilePath());
        } catch (Throwable e) {
            LogUtil.e(e);
            mStarted = false;
        }
        mConfig = config;
        float factor = MediaConfig.getSpeedFactor(config.getSpeedMode());
        long timeUs = System.nanoTime() / 1000L;
        mVideoTimer = new VideoCalculator(factor, timeUs);
        mAudioTimer = new AudioCalculator(config.getAudioConfig(), timeUs);
    }

    private void prepareMuxer(String fileName) throws IOException {
        if (mMuxer != null) {
            return;
        }
        /*fileName = StorageUtil.getExternalStoragePath() + File.separator + "video.mp4";
        //FileUtils.createFile(fileName);*/
        mMuxer = new MediaMuxer(fileName,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    public void quit() {
        if (mHandler == null) {
            return;
        }
        mHandler.getLooper().quitSafely();
    }

    private void onStop() {
        if (!mStarted) {
            return;
        }
        mMuxer.stop();
        mMuxer.release();
        mStarted = false;
        mMuxerStarted = false;
        mMuxer = null;
        if (mFinishListener != null) {
//            mFinishListener.onRecordFinish(mConfig.getFilePath(), mVideoTimer.getDuration(), mConfig.getSpeedMode());
        }
    }

    public void setOnProgressListener(OnRecordProgressListener listener) {
        mProgressListener = listener;
    }

    public void setOnFinishListener(OnRecordFinishListener listener) {
        mFinishListener = listener;
    }

    public void writeAudioData(ByteBuffer data, MediaCodec.BufferInfo info) {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_WRITE_AUDIO, new Data(data, info)));
    }

    private void onWriteAudioData(Data data) {
        if (!mStarted) {
            return;
        }
        if (mAudioTimer.getDuration() >= mMaxDuration) {
            return;
        }
        if (!mMuxerStarted) {
            mAudioDataList.add(data);
            return;
        }

        if (!mAudioDataList.isEmpty()) {
            for (Data data1 : mAudioDataList) {
                addAudioData(data1.data, data1.info);
            }
            mAudioDataList.clear();
        }
        addAudioData(data.data, data.info);
    }

    public void writeVideoData(ByteBuffer data, MediaCodec.BufferInfo info) {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_WRITE_VIDEO, new Data(data, info)));
    }

    private void onWriteVideoData(Data data) {
        if (!mStarted) {
            return;
        }
        if (mVideoTimer.getDuration() >= mMaxDuration) {
            return;
        }
        if (!mMuxerStarted) {
            mVideoDataList.add(data);
            return;
        }
        if (!mVideoDataList.isEmpty()) {
            for (Data data1 : mVideoDataList) {
                addVideoData(data1.data, data1.info);
            }
            mVideoDataList.clear();
        }
        addVideoData(data.data, data.info);
    }


    public void setAudioReady(MediaFormat format) {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_AUDIO_READY, format));
    }

    private void onAudioReady(MediaFormat format) {
        if (!mStarted) {
            return;
        }
        mAudioTrack = mMuxer.addTrack(format);
        mAudioReady = true;
        tryStart();
    }

    public void setVideoReady(MediaFormat format) {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_VIDEO_READY, format));
    }

    private void onVideoReady(MediaFormat format) {
        if (!mStarted) {
            return;
        }
        mVideoTrack = mMuxer.addTrack(format);
        mVideoReady = true;
        tryStart();
    }

    private void onAudioStop() {
        mAudioReady = false;
        tryStop();
    }

    private void onVideoStop() {
        mVideoReady = false;
        tryStop();
    }

    public void stopAudio() {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_AUDIO_STOP));
    }

    public void stopVideo() {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_VIDEO_STOP));
    }

    private void tryStop() {
        if (!mAudioReady && !mVideoReady) {
            onStop();
        }
    }

    private void tryStart() {
        if (mMuxerStarted) {
            return;
        }
        if (!mAudioReady) {
            return;
        }
        if (!mVideoReady) {
            return;
        }
        mMuxer.start();
        mMuxerStarted = true;
    }

    void addAudioData(ByteBuffer data, MediaCodec.BufferInfo info) {
        info.presentationTimeUs = mAudioTimer.getTimeStampUs();
        mAudioTimer.addSample(data.remaining());
        mMuxer.writeSampleData(mAudioTrack, data, info);
    }


    void addVideoData(ByteBuffer data, MediaCodec.BufferInfo info) {
        info.presentationTimeUs = mVideoTimer.getTimeStampUs();
        mMuxer.writeSampleData(mVideoTrack, data, info);
        mVideoTimer.addSample(0);
        if (mProgressListener != null) {
            mProgressListener.onRecordProgress(mVideoTimer.getDuration());
        }
    }

    private static class H extends Handler {
        WeakReference<VideoMuxer> ref;

        public H(Looper looper, VideoMuxer target) {
            super(looper);
            ref = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoMuxer muxer = ref.get();
            if (muxer == null) {
                return;
            }
            switch (msg.what) {
                case MSG_START:
                    muxer.onStart((MediaConfig) msg.obj);
                    break;
                case MSG_VIDEO_READY:
                    muxer.onVideoReady((MediaFormat) msg.obj);
                    break;
                case MSG_AUDIO_READY:
                    muxer.onAudioReady((MediaFormat) msg.obj);
                    break;
                case MSG_WRITE_AUDIO:
                    muxer.onWriteAudioData((Data) msg.obj);
                    break;
                case MSG_WRITE_VIDEO:
                    muxer.onWriteVideoData((Data) msg.obj);
                    break;
                case MSG_VIDEO_STOP:
                    muxer.onVideoStop();
                    break;
                case MSG_AUDIO_STOP:
                    muxer.onAudioStop();
                    break;
            }
        }
    }

    private static class Data {
        ByteBuffer data;
        MediaCodec.BufferInfo info;

        public Data(ByteBuffer data, MediaCodec.BufferInfo info) {
            this.data = ByteBuffer.allocate(data.capacity());
            this.data.put(data);
            this.data.position(info.offset);
            this.data.limit(info.offset + info.size);
            this.info = info;
        }
    }

}
