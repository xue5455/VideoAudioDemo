package com.xue.douyin.douyin.module.record.activity;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.xue.douyin.douyin.R;
import com.xue.douyin.douyin.base.activity.BaseBlankActivity;
import com.xue.douyin.douyin.common.camera.CameraCompat;
import com.xue.douyin.douyin.common.codec.AudioConfig;
import com.xue.douyin.douyin.common.codec.AudioEncoder;
import com.xue.douyin.douyin.common.codec.MovieMuxCore;
import com.xue.douyin.douyin.common.util.LogUtil;
import com.xue.douyin.douyin.common.util.StorageUtil;
import com.xue.douyin.douyin.common.view.RecordButton;
import com.xue.douyin.douyin.common.view.record.OnFrameAvailableListener;
import com.xue.douyin.douyin.common.view.record.OnSurfaceCreatedCallback;
import com.xue.douyin.douyin.common.view.record.RecordSurfaceView;
import com.xue.douyin.douyin.common.codec.VideoConfig;
import com.xue.douyin.douyin.common.codec.VideoEncoder;
import com.xue.douyin.douyin.common.codec.VideoFrameData;
import com.xue.douyin.douyin.module.record.presenter.RecordPresenter;
import com.xue.douyin.douyin.permission.PermissionManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class RecordActivity extends BaseBlankActivity<RecordPresenter> implements OnSurfaceCreatedCallback,
        OnFrameAvailableListener, CompoundButton.OnCheckedChangeListener {


    private RecordSurfaceView mSurfaceView;

    private MovieMuxCore mMuxCore;

    private VideoEncoder mVideoEncoder;

    private AudioEncoder mAudioEncoder;

    private CameraCompat mCamera;

    private CheckBox mCbFlashLight;

    private RecordButton mButton;

    private File mFile;

    private int i = 1;


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
                if (i == 1) {
//                    mAudioEncoder.start();
                    mVideoEncoder.start();
                } else {
                    mMuxCore.resume();
                    mVideoEncoder.resume();
//                    mAudioEncoder.resume();
                }
            }

            @Override
            public void onRecordStop() {
                if (i == 1) {
//                    mAudioEncoder.pause();
                    mVideoEncoder.pause();
                    mMuxCore.pause();
                }
                i--;
            }
        });
        mFile = new File(StorageUtil.getExternalStoragePath() + File.separator + "test.mp4");
        if (mFile.exists()) {
            mFile.delete();
        }
        try {
            mFile.createNewFile();
        } catch (IOException e) {
            LogUtil.e("RecordActivity", e);
        }
        mMuxCore = new MovieMuxCore(false,
                mFile.getAbsolutePath(),1);
        mMuxCore.setMaxLength(5, new MovieMuxCore.OnVideoMaxLengthListener() {
            @Override
            public void onReachMaxLength() {
                LogUtil.d("RecordActivity", "reach max length");
                mVideoEncoder.stop();
                mAudioEncoder.stop();
            }
        });
        mMuxCore.start();

        try {
            mAudioEncoder = new AudioEncoder(mMuxCore, new AudioConfig());
        } catch (IOException e) {
            LogUtil.e("RecordActivity", e);
        }
        PermissionManager.instance().checkPermission(Manifest.permission.RECORD_AUDIO, new PermissionManager.OnPermissionGrantedListener() {
            @Override
            public void onPermissionGranted(String permission) {
                LogUtil.d("语音授权");
            }

            @Override
            public void onPermissionDenied(String permission) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
        mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        mCamera.stopPreview(true);
        mSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMuxCore.quit();
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
            mVideoEncoder = new VideoEncoder(mMuxCore, new VideoConfig(context, 720, 1280, 1000000));
            mSurfaceView.setFrameListener(this);
        } catch (IOException e) {
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
    public void onFrameAvailable(VideoFrameData frameData) {
        if (mVideoEncoder != null) {
            mVideoEncoder.newFrame(frameData);
        }
    }
}
