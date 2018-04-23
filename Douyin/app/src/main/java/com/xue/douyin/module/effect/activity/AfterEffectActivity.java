package com.xue.douyin.module.effect.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaExtractor;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.xue.douyin.R;
import com.xue.douyin.base.activity.BaseBlankActivity;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.module.effect.presenter.AfterEffectPresenter;

import java.io.IOException;


/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class AfterEffectActivity extends BaseBlankActivity<AfterEffectPresenter> implements SurfaceHolder.Callback {

    public static final String KEY_FINAL_PATH = "key:path";

    private SurfaceView surfaceView;

    public static void start(Activity from, String finalFile) {
        Intent intent = new Intent(from, AfterEffectActivity.class);
        intent.putExtra(KEY_FINAL_PATH, finalFile);
        from.startActivity(intent);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new AfterEffectPresenter(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_effect);
        hideSystemNavigationBar();
        initContentView();
        mPresenter.init();
    }

    private void initContentView() {
        surfaceView = findViewById(R.id.sv_play);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPresenter.start(holder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onStop() {
        mPresenter.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mPresenter.stop();
        super.onDestroy();
    }
}
