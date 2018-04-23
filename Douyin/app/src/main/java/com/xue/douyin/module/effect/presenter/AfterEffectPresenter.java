package com.xue.douyin.module.effect.presenter;

import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Surface;

import com.xue.douyin.base.presenter.BaseActivityPresenter;
import com.xue.douyin.common.C;
import com.xue.douyin.common.player.VideoPlayer;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.ThreadUtil;
import com.xue.douyin.module.effect.activity.AfterEffectActivity;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC;


/**
 * Created by 薛贤俊 on 2018/3/24.
 */

public class AfterEffectPresenter extends BaseActivityPresenter<AfterEffectActivity> {

    private String filePath;

    private VideoPlayer player;

    public AfterEffectPresenter(AfterEffectActivity target) {
        super(target);
    }

    public void init() {
        Intent intent = getTarget().getIntent();
        filePath = intent.getStringExtra(AfterEffectActivity.KEY_FINAL_PATH);
        player = new VideoPlayer(filePath);
    }


    public void start(Surface surface) {
        player.configure(surface);
        player.start();
    }

    public void pause() {
        player.pause();
    }

    public void stop() {
        player.stop();
    }

}

