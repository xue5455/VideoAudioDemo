package com.xue.douyin.common.codec;

import android.opengl.EGLContext;
import android.support.annotation.Nullable;

import com.xue.douyin.common.codec.audio.AudioConfig;
import com.xue.douyin.common.codec.audio.AudioRecorder;
import com.xue.douyin.common.codec.video.VideoConfig;
import com.xue.douyin.common.codec.video.VideoFrameData;
import com.xue.douyin.common.codec.video.VideoRecorder;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.StorageUtil;
import com.xue.douyin.common.view.record.OnFrameAvailableListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class MediaRecorder implements OnFrameAvailableListener, OnRecordFinishListener {

    private VideoRecorder mVideoRecorder;

    private AudioRecorder mAudioRecorder;

    private long mMaxLength;

    private long mVideoRemaining;

    private long mAudioRemainning;

    private List<MediaData> mVideoData;

    private List<MediaData> mAudioData;

    private OnRecordFinishListener mFinishListener;

    public MediaRecorder(int seconds, @Nullable OnRecordFinishListener listener) {
        mVideoRecorder = new VideoRecorder();
        mAudioRecorder = new AudioRecorder();
        mVideoRecorder.setOnFinishListener(this);
        mAudioRecorder.setOnFinishListener(this);
        mMaxLength = seconds * 1000000;
        mVideoRemaining = mMaxLength;
        mAudioRemainning = mMaxLength;
        mFinishListener = listener;
        mVideoData = new ArrayList<>();
        mAudioData = new ArrayList<>();
    }


    public void start(EGLContext context, int width, int height, float speed) {
        mAudioRecorder.start(new AudioConfig(generateAudioFile(), speed));
        mVideoRecorder.start(new VideoConfig(context, width, height, 1000000, generateVideoFile(), mVideoRemaining, speed));
    }

    public void stop() {
        mVideoRecorder.stop();
        mAudioRecorder.stop();
    }

    public void quit() {
        mVideoRecorder.quit();
        mAudioRecorder.quit();
    }

    private void createFileIfNeeded(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            LogUtil.e(e);
        }
    }

    private String generateVideoFile() {
        String file = StorageUtil.getExternalStoragePath() + File.separator + "v_temp" + mVideoData.size() + ".mp4";
        createFileIfNeeded(file);
        return file;
    }

    private String generateAudioFile() {
        String file = StorageUtil.getExternalStoragePath() + File.separator + "a_temp" + mAudioData.size() + ".mp4";
        createFileIfNeeded(file);
        return file;
    }

    @Override
    public void onFrameAvailable(VideoFrameData frameData) {
        mVideoRecorder.frameAvailable(frameData);
    }

    public void setOnProgressListener(OnRecordProgressListener listener) {
        mVideoRecorder.setOnProgressListener(listener);
    }

    @Override
    public void onRecordFinish(String fileName, boolean isVideo, float speed) {
        if (isVideo) {
            mVideoRemaining -= mVideoRecorder.getDuration();
            mVideoData.add(new MediaData(fileName, true, speed));
        } else {
            mAudioRemainning -= mAudioRecorder.getDuration();
            mAudioData.add(new MediaData(fileName, false, speed));
        }

        if (mFinishListener != null) {
            mFinishListener.onRecordFinish(fileName, isVideo, speed);
        }
    }

    public boolean isReachMaxLength() {
        return mVideoRemaining <= 0;
    }

    public List<MediaData> getVideos() {
        return mVideoData;
    }

    public List<MediaData> getAudios() {
        return mAudioData;
    }
}
