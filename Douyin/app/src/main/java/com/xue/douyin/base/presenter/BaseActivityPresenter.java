package com.xue.douyin.base.presenter;

import com.xue.douyin.base.activity.BaseActivity;

/**
 * Created by 薛贤俊 on 2018/3/1.
 */

public class BaseActivityPresenter<T extends BaseActivity> extends BasePresenter<T> {

    public BaseActivityPresenter(T target) {
        super(target);
    }
}
