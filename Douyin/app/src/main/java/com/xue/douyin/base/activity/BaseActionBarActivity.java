package com.xue.douyin.base.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.xue.douyin.R;
import com.xue.douyin.base.presenter.BaseActivityPresenter;

/**
 * Created by 薛贤俊 on 2018/3/1.
 */

public abstract class BaseActionBarActivity<T extends BaseActivityPresenter> extends BaseActivity<T> {

    private ViewGroup mContentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_with_actionbar);
        mContentView = findViewById(R.id.ll_content);
    }

    @Override
    public void setContentView(int layoutResID) {
        if (mContentView == null) {
            throw new RuntimeException("should call super.onCreate first");
        }
        mContentView.removeAllViews();
        View view = getLayoutInflater().inflate(layoutResID, null);
        mContentView.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
