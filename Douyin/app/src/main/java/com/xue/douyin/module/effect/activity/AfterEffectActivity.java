package com.xue.douyin.module.effect.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.xue.douyin.R;
import com.xue.douyin.base.activity.BaseBlankActivity;
import com.xue.douyin.common.recorder.MediaData;
import com.xue.douyin.module.effect.presenter.AfterEffectPresenter;

import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class AfterEffectActivity extends BaseBlankActivity<AfterEffectPresenter> {


    public static final String KEY_FINAL_PATH = "key:path";

    public static final String KEY_CLIP_INFO = "key:info";

    public static void start(Activity from, String finalFile, List<MediaData> clips) {
        Intent intent = new Intent(from, AfterEffectActivity.class);
        intent.putExtra(KEY_FINAL_PATH, finalFile);
        intent.putExtra(KEY_CLIP_INFO, clips.toArray());
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
        mPresenter.init();
    }
}
