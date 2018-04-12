package com.xue.douyin.base.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xue.douyin.base.presenter.BaseActivityPresenter;

/**
 * Created by 薛贤俊 on 2018/3/1.
 */

public abstract class BaseActivity<T extends BaseActivityPresenter> extends AppCompatActivity {

    protected T mPresenter;

    public T getPresenter() {
        return mPresenter;
    }

    protected abstract void initPresenter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPresenter();
        if(mPresenter==null) {
            return;
        }
        mPresenter.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mPresenter==null) {
            return;
        }
        mPresenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPresenter==null) {
            return;
        }
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPresenter==null) {
            return;
        }
        mPresenter.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mPresenter==null) {
            return;
        }
        mPresenter.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPresenter==null) {
            return;
        }
        mPresenter.onDestroy();
    }


    protected void hideSystemNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
