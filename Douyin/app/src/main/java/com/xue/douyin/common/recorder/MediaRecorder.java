package com.xue.douyin.common.recorder;

import android.opengl.EGLContext;
import android.support.annotation.Nullable;
import com.xue.douyin.common.C;
import com.xue.douyin.common.recorder.audio.AudioConfig;
import com.xue.douyin.common.recorder.video.VideoConfig;
import com.xue.douyin.common.recorder.video.VideoFrameData;
import com.xue.douyin.common.util.FileUtils;
import com.xue.douyin.common.view.record.OnFrameAvailableListener;

import static com.xue.douyin.common.C.VIDEO;


/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class MediaRecorder implements OnFrameAvailableListener, OnRecordFinishListener {

    private long maxDuration;
    /**
     * 视频还可以拍摄的帧数
     */
    private int remainDuration;

    private OnRecordFinishListener mFinishListener;

    private AudioRecorder audioRecorder;

    private VideoRecorder videoRecorder;

    private boolean supportAudio = true;

    public MediaRecorder(int seconds, @Nullable OnRecordFinishListener listener) {
        mFinishListener = listener;
        audioRecorder = new AudioRecorder();
        videoRecorder = new VideoRecorder();
        remainDuration = seconds * C.SECOND_IN_US;
        maxDuration = remainDuration;
        audioRecorder.setOnRecordFinishListener(this);
        videoRecorder.setOnRecordFinishListener(this);
    }


    public boolean start(EGLContext context, int width, int height, @C.SpeedMode int mode) {
        if (remainDuration <= 0) {
            return false;
        }
        FileUtils.createFile(C.VIDEO_TEMP_FILE_NAME);
        FileUtils.createFile(C.AUDIO_TEMP_FILE_NAME);
        AudioConfig audio = new AudioConfig(C.AUDIO_TEMP_FILE_NAME, C.AudioParams.SAMPLE_RATE, C.AudioParams.SAMPLE_PER_FRAME);
        audio.setSpeedMode(mode);
        audio.setMaxDuration(remainDuration);
        VideoConfig video = new VideoConfig(C.VIDEO_TEMP_FILE_NAME, context, width, height, C.VideoParams.BIT_RATE);
        video.setFactor(MediaConfig.getSpeedFactor(mode));
        video.setMaxDuration(remainDuration);
        audioRecorder.configure(audio);
        videoRecorder.configure(video);
        try {
            audioRecorder.prepareCodec();
            videoRecorder.prepareCodec();
        } catch (Throwable e) {
            audioRecorder.shutdown();
            videoRecorder.shutdown();
            return false;
        }


        if (supportAudio) {
            audioRecorder.start();
        }
        videoRecorder.start();
        return true;
    }

    public void stop() {
        if (supportAudio)
            audioRecorder.stop();
        videoRecorder.stop();
    }

    private void quit() {

    }


    @Override
    public void onFrameAvailable(VideoFrameData frameData) {
        videoRecorder.frameAvailable(frameData);
    }

    public void setOnProgressListener(OnRecordProgressListener listener) {
        videoRecorder.setOnProgressListener(listener);
    }

    @Override
    public void onRecordFinish(ClipInfo info) {
        if (info.getType() == VIDEO) {
            remainDuration -= info.getDuration();
        }
        if (mFinishListener != null) {
            mFinishListener.onRecordFinish(info);
        }
    }


    public float getMaxDuration() {
        return maxDuration;
    }
}
