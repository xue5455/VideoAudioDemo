package com.xue.douyin.module.record.presenter;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.xue.douyin.R;
import com.xue.douyin.base.presenter.BaseActivityPresenter;
import com.xue.douyin.common.C;
import com.xue.douyin.common.recorder.ClipInfo;
import com.xue.douyin.common.recorder.MediaConfig;
import com.xue.douyin.common.recorder.MediaRecorder;
import com.xue.douyin.common.recorder.OnRecordFinishListener;
import com.xue.douyin.common.recorder.OnRecordProgressListener;
import com.xue.douyin.common.util.FileUtils;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.ScreenUtil;
import com.xue.douyin.common.util.StorageUtil;
import com.xue.douyin.common.view.RecordButton;
import com.xue.douyin.common.view.record.OnSurfaceCreatedCallback;
import com.xue.douyin.ffmpeg.VideoCmdCallback;
import com.xue.douyin.ffmpeg.VideoCommand;
import com.xue.douyin.ffmpeg.VideoQueue;
import com.xue.douyin.module.effect.activity.AfterEffectActivity;
import com.xue.douyin.module.record.activity.RecordActivity;
import com.xue.douyin.permission.PermissionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.xue.douyin.common.C.AUDIO;
import static com.xue.douyin.common.C.MODE_EXTRA_SLOW;
import static com.xue.douyin.common.C.MODE_NORMAL;
import static com.xue.douyin.common.C.VIDEO;


/**
 * Created by 薛贤俊 on 2018/3/1.
 */

public class RecordPresenter extends BaseActivityPresenter<RecordActivity>
        implements RecordButton.OnRecordListener,
        OnRecordFinishListener,
        OnRecordProgressListener,
        View.OnClickListener,
        OnSurfaceCreatedCallback,
        PermissionManager.OnPermissionGrantedListener,
        RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener {

    private static final String mFinalPath = StorageUtil.getExternalStoragePath() + File.separator + "temp.mp4";
//    private static final String temp_video = StorageUtil.getExternalStoragePath() + File.separator + "temp" + File.separator + "audio.aac";
//    private static final String temp_audio = StorageUtil.getExternalStoragePath() + File.separator + "temp" + File.separator + "video_tmp.mp4";

    private List<String> videoList = new ArrayList<>();

    /**
     * 初始速度1.0f
     */
    private @C.SpeedMode
    int mMode = MODE_NORMAL;

    private MediaRecorder mRecorder;

    private EGLContext mGlContext;

    private VideoQueue mQueue;

    private boolean mStarted = false;


    private ClipInfo audioInfo;
    private ClipInfo videoInfo;
    private float currentProgress;

    public RecordPresenter(RecordActivity target) {
        super(target);
    }

    public void init() {
        mQueue = new VideoQueue();
        PermissionManager.instance().checkPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                this);
        mRecorder = new MediaRecorder(15, this);

        mRecorder.setOnProgressListener(this);
    }

    @Override
    public void onRecordStart() {
        int width = ScreenUtil.getScreenWidth();
        int height = ScreenUtil.getScreenHeight();
        //540 960
        if (!mRecorder.start(mGlContext, getTarget().getCameraSize().width, getTarget().getCameraSize().height, mMode)) {
            Toast.makeText(getTarget(), "视频已达到最大长度", Toast.LENGTH_SHORT).show();
            return;
        }
        mStarted = true;
        getTarget().hideViews();
    }

    @Override
    public void onRecordStop() {
        if (!mStarted) {
            return;
        }
        mRecorder.stop();
    }


    @Override
    public void onRecordFinish(ClipInfo info) {
        if (info.getType() == VIDEO) {
            currentProgress = info.getDuration() * 1.0f / mRecorder.getMaxDuration();
        }
        setClipInfo(info);
    }

    public void setClipInfo(final ClipInfo info) {
        getTarget().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (info.getType()) {
                    case VIDEO:
                        videoInfo = info;
                        break;
                    case AUDIO:
                        audioInfo = info;
                        break;
                }
                mergeVideoAudio();
            }
        });
    }

    private void mergeVideoAudio() {
        if (audioInfo == null || videoInfo == null) {
            return;
        }
        final String currentFile = generateFileName();
        FileUtils.createFile(currentFile);
        getTarget().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getTarget().addProgress(currentProgress);
                getTarget().showViews();
                currentProgress = 0f;
            }
        });
        mQueue.execCommand(VideoCommand.mergeVideoAudio(videoInfo.getFileName(),
                audioInfo.getFileName(), currentFile).toArray(), new VideoCmdCallback() {
            @Override
            public void onCommandFinish(boolean success) {
                videoList.add(currentFile);
                FileUtils.deleteFile(audioInfo.getFileName());
                FileUtils.deleteFile(videoInfo.getFileName());
                audioInfo = null;
                videoInfo = null;
                getTarget().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getTarget().addProgress(currentProgress);
                        getTarget().showViews();
                        currentProgress = 0f;
                    }
                });
            }
        });
    }


    @Override
    public void onRecordProgress(long duration) {
        final float progress = duration * 1.0f / mRecorder.getMaxDuration();
        getTarget().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getTarget().setProgress(progress);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                if (videoList.size() == 0) {
                    return;
                }
                onNextClick();
                break;
            case R.id.btn_delete:
                if (videoList.isEmpty()) {
                    return;
                }
                String file = videoList.remove(videoList.size() - 1);
                getTarget().deleteProgress();
                FileUtils.deleteFile(file);
                break;
        }

    }

    private void onNextClick() {
        final Dialog dialog = ProgressDialog.show(getTarget(), "正在合成", "正在合成");
        FileUtils.createFile(mFinalPath);
        mQueue.execCommand(VideoCommand.mergeVideo(videoList, mFinalPath).toArray(), new VideoCmdCallback() {
            @Override
            public void onCommandFinish(boolean success) {
                dialog.dismiss();
                Toast.makeText(getTarget(), "合成完毕", Toast.LENGTH_SHORT).show();
                AfterEffectActivity.start(getTarget(),mFinalPath);
            }
        });
    }

    public void stopRecord() {
        mRecorder.stop();

    }

    @Override
    public void onSurfaceCreated(SurfaceTexture texture, EGLContext context) {
        try {
            mGlContext = context;
            getTarget().setFrameListener(mRecorder);
        } catch (Throwable e) {
            LogUtil.e(e);
        }
        getTarget().startPreview(texture);
    }


    @Override
    public void onPermissionGranted(String permission) {

    }

    @Override
    public void onPermissionDenied(String permission) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.btn_extra_slow:
                mMode = C.MODE_EXTRA_SLOW;
                break;
            case R.id.btn_slow:
                mMode = C.MODE_SLOW;
                break;
            case R.id.btn_normal:
                mMode = C.MODE_NORMAL;
                break;
            case R.id.btn_fast:
                mMode = C.MODE_FAST;
                break;
            case R.id.btn_extra_fast:
                mMode = C.MODE_EXTRA_FAST;
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_camera:
                getTarget().switchCamera(isChecked);
                break;
            case R.id.cb_flashlight:
                getTarget().switchFlashLight(isChecked);
                break;
        }
    }

    private String generateFileName() {
        return StorageUtil.getExternalStoragePath() + File.separator + "temp" + videoList.size() + ".mp4";
    }
}
