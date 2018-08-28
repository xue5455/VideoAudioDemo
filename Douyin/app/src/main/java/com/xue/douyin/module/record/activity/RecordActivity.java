package com.xue.douyin.module.record.activity;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.xue.douyin.common.view.ProgressView;
import com.xue.douyin.common.view.record.OnFrameAvailableListener;
import com.xue.douyin.R;
import com.xue.douyin.base.activity.BaseBlankActivity;
import com.xue.douyin.common.camera.CameraCompat;
import com.xue.douyin.common.view.RecordButton;
import com.xue.douyin.common.view.record.RecordSurfaceView;
import com.xue.douyin.module.record.presenter.RecordPresenter;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class RecordActivity extends BaseBlankActivity<RecordPresenter> {


    private RecordSurfaceView mSurfaceView;

    private CameraCompat mCamera;

    private CheckBox mCbFlashLight;

    private RecordButton mButton;

    private RadioGroup mSpeedButtons;

    private ProgressView mProgressView;

    private CameraCompat.CameraSize mPreviewSize;

    @Override
    protected void initPresenter() {
        mPresenter = new RecordPresenter(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mPresenter.init();
        mCamera = CameraCompat.newInstance(this);

        mSurfaceView = findViewById(R.id.sv_record);
        mSurfaceView.setOnSurfaceCreatedCallback(mPresenter);
        CheckBox cb = findViewById(R.id.cb_flashlight);
        cb.setOnCheckedChangeListener(mPresenter);
        mCbFlashLight = cb;
        cb = findViewById(R.id.cb_camera);
        cb.setOnCheckedChangeListener(mPresenter);
        mButton = findViewById(R.id.btn_record);
        mButton.setOnRecordListener(mPresenter);
        findViewById(R.id.btn_next).setOnClickListener(mPresenter);
        mSpeedButtons = findViewById(R.id.rg_speed);
        mSpeedButtons.setOnCheckedChangeListener(mPresenter);
        findViewById(R.id.btn_delete).setOnClickListener(mPresenter);
        mProgressView = findViewById(R.id.record_progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
        mCamera.startPreview();
        mPreviewSize = mCamera.getOutputSize();
        mSurfaceView.setPreviewSize(mPreviewSize.height,mPreviewSize.width);
        hideSystemNavigationBar();
    }

    public CameraCompat.CameraSize getCameraSize(){
        return mCamera.getOutputSize();
    }


    @Override
    protected void onPause() {
        mPresenter.stopRecord();
        mCamera.stopPreview(true);
        mSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void setFrameListener(OnFrameAvailableListener listener) {
        mSurfaceView.setFrameListener(listener);
    }

    public void startPreview(final SurfaceTexture texture) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCamera.setSurfaceTexture(texture);
                mCamera.startPreview();
            }
        });
    }

    public void switchCamera(boolean checked) {
        mCbFlashLight.setEnabled(!checked);
        mCamera.switchCamera();
        mCbFlashLight.setChecked(false);
    }

    public void switchFlashLight(boolean checked) {
        if (checked) {
            mCamera.turnLight(true);
        } else {
            mCamera.turnLight(false);
        }
    }

    public void hideViews() {
        findViewById(R.id.record_group).setVisibility(View.GONE);
    }

    public void showViews() {
        findViewById(R.id.record_group).setVisibility(View.VISIBLE);
    }

    public void setProgress(float progress){
        mProgressView.setLoadingProgress(progress);
    }

    public void addProgress(float progress){
        mProgressView.addProgress(progress);
    }

    public void deleteProgress(){
        mProgressView.deleteProgress();
    }
}
