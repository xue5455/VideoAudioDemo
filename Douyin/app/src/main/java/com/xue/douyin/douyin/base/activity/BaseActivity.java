package com.xue.douyin.douyin.base.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.xue.douyin.douyin.base.presenter.BaseActivityPresenter;

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
        mPresenter.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }
}
