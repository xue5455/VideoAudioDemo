package com.xue.douyin.module.effect.presenter;

import android.content.Intent;

import com.xue.douyin.base.presenter.BaseActivityPresenter;
import com.xue.douyin.common.recorder.MediaData;
import com.xue.douyin.module.effect.activity.AfterEffectActivity;

import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/24.
 */

public class AfterEffectPresenter extends BaseActivityPresenter<AfterEffectActivity> {

    private List<MediaData> mDataList;

    private String mFilePath;

    public AfterEffectPresenter(AfterEffectActivity target) {
        super(target);
    }

    public void init() {
        Intent intent = getTarget().getIntent();
        mFilePath = intent.getStringExtra(AfterEffectActivity.KEY_FINAL_PATH);
        mDataList = intent.getParcelableArrayListExtra(AfterEffectActivity.KEY_CLIP_INFO);
    }
}
