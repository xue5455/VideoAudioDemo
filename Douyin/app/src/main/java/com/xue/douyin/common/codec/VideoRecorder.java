package com.xue.douyin.common.codec;

import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.StorageUtil;
import com.xue.douyin.common.util.VideoUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/13.
 */

public class VideoRecorder implements MovieMuxCore.OnVideoMaxLengthListener {

    private long mMaxLength;

    private long mRemainingDuration;

    private static final String VIDEO_NAME_FORMAT = StorageUtil.getExternalStoragePath()+ File.separator + "video_segment%1$d.mp4";

    private VideoEncoder mVideoEncoder;

    private AudioEncoder mAudioEncoder;

    private MovieMuxCore mMuxer;

    private VideoConfig mVideoConfig;

    private AudioConfig mAudioConfig;
    /**
     * 片段
     */
    private List<VideoSegment> mSegments;
    /**
     * 当前是第几段视频
     */
    private int mCurrentSegment;

    private MovieMuxCore.OnVideoMaxLengthListener mMaxLengthListener;

    public VideoRecorder(VideoConfig videoConfig, AudioConfig audioConfig, int seconds) {
        this.mVideoConfig = videoConfig;
        this.mAudioConfig = audioConfig;
        this.mSegments = new ArrayList<>();
        //毫秒
        this.mMaxLength = seconds * 1000000;
        this.mRemainingDuration = mMaxLength;
    }

    /**
     * 开始录制
     *
     * @param speed 视频倍数 0.5 - 2
     *              0.5代表加速一倍 2代表减慢一倍
     */
    public void start(double speed) {
        mMuxer = new MovieMuxCore(true, generateFile(), speed);
        mMuxer.setMaxLength(mRemainingDuration, this);
        try {
            mVideoEncoder = new VideoEncoder(mMuxer, mVideoConfig);
            mAudioEncoder = new AudioEncoder(mMuxer, mAudioConfig);
        } catch (Throwable e) {
            mMuxer = null;
            mVideoEncoder = null;
            mAudioEncoder = null;
            return;
        }
        mCurrentSegment++;
        mMuxer.start();
        mAudioEncoder.start();
        mVideoEncoder.start();
    }

    public void stop() {
        if (mVideoEncoder == null) {
            return;
        }
        mVideoEncoder.stop();
        mAudioEncoder.stop();
        mMuxer.stop();

        mVideoEncoder = null;
        mAudioEncoder = null;
        mMuxer = null;
    }


    private String generateFile() {
        String fileName = String.format(VIDEO_NAME_FORMAT, mCurrentSegment);
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            LogUtil.e(e);
        }
        return fileName;
    }

    public void setOnMaxLengthListener(MovieMuxCore.OnVideoMaxLengthListener maxLengthListener) {
        this.mMaxLengthListener = maxLengthListener;
    }

    @Override
    public void onReachMaxLength(String fileName, double speed) {
        LogUtil.d("MovieMuxCore","达到最大长度");
        long duration = (long) (VideoUtil.getDuration(fileName) * speed);
        mRemainingDuration -= duration;
        mRemainingDuration = Math.max(0, mRemainingDuration);
        mSegments.add(new VideoSegment(fileName, duration));
        if (mRemainingDuration == 0) {
            stop();
            if (mMaxLengthListener != null) {
                mMaxLengthListener.onReachMaxLength(fileName, speed);
            }
        }
    }

    public void videoNewFrame(VideoFrameData frameData) {
        if (mVideoEncoder != null) {
            mVideoEncoder.newFrame(frameData);
        }
    }

    public static class VideoSegment {
        //视频文件地址
        public String path;
        //视频长度
        public long length;

        public VideoSegment(String path, long length) {
            this.path = path;
            this.length = length;
        }
    }
}
