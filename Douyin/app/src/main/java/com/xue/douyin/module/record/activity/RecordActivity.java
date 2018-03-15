package com.xue.douyin.module.record.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.netease.ffmpeg.VideoCmdCallback;
import com.netease.ffmpeg.VideoCommand;
import com.netease.ffmpeg.VideoQueue;
import com.xue.douyin.R;
import com.xue.douyin.base.activity.BaseBlankActivity;
import com.xue.douyin.common.camera.CameraCompat;
import com.xue.douyin.common.codec.OnRecordFinishListener;
import com.xue.douyin.common.codec.MediaData;
import com.xue.douyin.common.codec.MediaRecorder;
import com.xue.douyin.common.codec.OnRecordProgressListener;
import com.xue.douyin.common.util.FileUtils;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.StorageUtil;
import com.xue.douyin.common.view.RecordButton;
import com.xue.douyin.common.view.record.OnSurfaceCreatedCallback;
import com.xue.douyin.common.view.record.RecordSurfaceView;
import com.xue.douyin.module.record.presenter.RecordPresenter;
import com.xue.douyin.permission.PermissionManager;
import java.io.File;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class RecordActivity extends BaseBlankActivity<RecordPresenter> implements OnSurfaceCreatedCallback,
        CompoundButton.OnCheckedChangeListener, OnRecordProgressListener {

    private String mVideoFile = StorageUtil.getExternalStoragePath() + File.separator + "v_final.mp4";
    private String mFinal = StorageUtil.getExternalStoragePath() + File.separator + "final.mp4";

    private String mAudioFile = StorageUtil.getExternalStoragePath() + File.separator + "a_final.mp4";

    private RecordSurfaceView mSurfaceView;

    private CameraCompat mCamera;

    private CheckBox mCbFlashLight;

    private RecordButton mButton;

    private MediaRecorder mRecorder;

    private EGLContext mGlContext;

    private VideoQueue mQueue;

    @Override
    protected void initPresenter() {
        mPresenter = new RecordPresenter(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        hideSystemNavigationBar();
        mCamera = CameraCompat.newInstance(this);
        mSurfaceView = findViewById(R.id.sv_record);
        mSurfaceView.setOnSurfaceCreatedCallback(this);
        CheckBox cb = findViewById(R.id.cb_flashlight);
        cb.setOnCheckedChangeListener(this);
        mCbFlashLight = cb;
        cb = findViewById(R.id.cb_camera);
        cb.setOnCheckedChangeListener(this);
        mButton = findViewById(R.id.btn_record);
        mButton.setOnRecordListener(new RecordButton.OnRecordListener() {
            @Override
            public void onRecordStart() {
                mRecorder.start(mGlContext, 540, 960, 2.0f);
            }

            @Override
            public void onRecordStop() {
                mRecorder.stop();
            }
        });
        mQueue = new VideoQueue();
        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClick();
            }
        });

        PermissionManager.instance().checkPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                new PermissionManager.OnPermissionGrantedListener() {
                    @Override
                    public void onPermissionGranted(String permission) {

                    }

                    @Override
                    public void onPermissionDenied(String permission) {

                    }
                });
        mRecorder = new MediaRecorder(15, new OnRecordFinishListener() {
            @Override
            public void onRecordFinish(final String fileName, boolean isVideo, float speed) {
                if(!isVideo){
                    mQueue.execCommand(VideoCommand.changePTS(fileName, mAudioFile,
                            speed).toArray(), new VideoCmdCallback() {
                        @Override
                        public void onCommandFinish(boolean success) {
                            FileUtils.deleteFile(fileName);
                            FileUtils.renameFile(mAudioFile, fileName);
                        }
                    });
                }
            }
        });

        mRecorder.setOnProgressListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
        mCamera.startPreview();
    }


    private void onNextClick() {
        final Dialog dialog = ProgressDialog.show(this, "正在合成", "正在合成");
        List<MediaData> videos = mRecorder.getVideos();
        List<MediaData> audios = mRecorder.getAudios();
        mQueue.execCommand(VideoCommand.mergeMusic(audios, mAudioFile).toArray());
        mQueue.execCommand(VideoCommand.mergeVideo(videos, mVideoFile).toArray(),null);
        mQueue.execCommand(VideoCommand.mergeVideoAudio(mVideoFile, mAudioFile, mFinal).toArray(), new VideoCmdCallback() {
            @Override
            public void onCommandFinish(boolean success) {
                FileUtils.deleteFile(mVideoFile);
                FileUtils.deleteFile(mAudioFile);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RecordActivity.this, "合成完毕", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        mRecorder.stop();
        mCamera.stopPreview(true);
        mSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void hideSystemNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_camera:
                mCbFlashLight.setEnabled(!isChecked);
                mCamera.switchCamera();
                mCbFlashLight.setChecked(false);
                break;
            case R.id.cb_flashlight:
                if (isChecked) {
                    mCamera.turnLight(true);
                } else {
                    mCamera.turnLight(false);
                }
                break;
        }
    }

    @Override
    public void onSurfaceCreated(final SurfaceTexture texture, EGLContext context) {
        try {
            mGlContext = context;
            mSurfaceView.setFrameListener(mRecorder);
        } catch (Throwable e) {
            LogUtil.e("xue", e);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCamera.setSurfaceTexture(texture);
                mCamera.startPreview();
            }
        });


    }

    @Override
    public void onRecordProgress(long duration) {

    }
}
