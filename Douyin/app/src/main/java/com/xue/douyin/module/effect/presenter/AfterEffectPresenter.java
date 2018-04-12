package com.xue.douyin.module.effect.presenter;

import android.content.Intent;

import com.xue.douyin.base.presenter.BaseActivityPresenter;
import com.xue.douyin.module.effect.activity.AfterEffectActivity;


/**
 * Created by 薛贤俊 on 2018/3/24.
 */

public class AfterEffectPresenter extends BaseActivityPresenter<AfterEffectActivity> {


    private String mFilePath;

    public AfterEffectPresenter(AfterEffectActivity target) {
        super(target);
    }

    public void init() {
        Intent intent = getTarget().getIntent();
        mFilePath = intent.getStringExtra(AfterEffectActivity.KEY_FINAL_PATH);
    }
}
