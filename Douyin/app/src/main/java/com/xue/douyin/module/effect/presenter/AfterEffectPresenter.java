package com.xue.douyin.module.effect.presenter;


import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.support.annotation.IntDef;
import android.view.Surface;
import android.view.View;
import android.widget.SeekBar;

import com.xue.douyin.R;
import com.xue.douyin.base.presenter.BaseActivityPresenter;
import com.xue.douyin.common.player.VideoPlayer;
import com.xue.douyin.module.effect.activity.AfterEffectActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by 薛贤俊 on 2018/3/24.
 */

public class AfterEffectPresenter extends BaseActivityPresenter<AfterEffectActivity> implements
        View.OnClickListener, VideoPlayer.OnPlayerProgressListener, SeekBar.OnSeekBarChangeListener {
    /**
     * 正在播放
     */
    private static final int STATE_PLAYING = 1;
    /**
     * 正在编辑
     */
    private static final int STATE_EDITING = 2;

    @IntDef({STATE_PLAYING, STATE_EDITING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    private @State
    int state = STATE_PLAYING;


    private String filePath;

    private VideoPlayer player;

    private boolean seekBarTouching = false;

    private boolean playing = true;

    public AfterEffectPresenter(AfterEffectActivity target) {
        super(target);
    }

    public void init() {
        Intent intent = getTarget().getIntent();
        filePath = intent.getStringExtra(AfterEffectActivity.KEY_FINAL_PATH);
        player = new VideoPlayer(filePath);
        player.setOnPlayerProgressListener(this);
    }


    public void start(SurfaceTexture surface) {
        player.configure(surface);
        getTarget().setMaxSeconds(player.getDuration());
        getTarget().setCurrentSeconds(0);
        getTarget().setSeekBarMax(player.getDuration());
        getTarget().setSeekBarProgress(0);
        player.start();
    }


    public void pause() {
        player.pause();
        getTarget().showPlayButton();
        playing = false;
    }

    public void resume() {
        player.resume();
        getTarget().hidePlayButton();
        playing = true;
    }

    public void stop() {
        player.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                onBackClick();
                break;
            case R.id.btn_effect:
                onEffectClick();
                break;
            case R.id.btn_play:
                onPlayClick();
                break;
        }
    }

    private void onPlayClick() {
        if (state != STATE_EDITING) {
            return;
        }
        if (playing) {
            pause();
        } else {
            resume();
        }

    }

    private void onBackClick() {
        if (state == STATE_PLAYING) {
            //如果是播放状态，那么直接返回上一个页面
            getTarget().finish();
        } else {
            //编辑状态，则返回播放状态
            getTarget().hideEffectPanel();
            getTarget().enlargeSurfaceView();
            getTarget().hidePlayButton();
            state = STATE_PLAYING;
            if (!playing) {
                resume();
            }
        }
    }

    private void onEffectClick() {
        if (state == STATE_EDITING) {
            return;
        }
        pause();
        player.seekTo(0);
        getTarget().setSeekBarProgress(0);
        getTarget().showEffectPanel();
        getTarget().shrinkSurfaceView();
        getTarget().showPlayButton();
        state = STATE_EDITING;
    }


    @Override
    public void onPlayerProgress(final long currentTimeUs) {
        getTarget().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getTarget().setCurrentSeconds(currentTimeUs);
                getTarget().setSeekBarProgress(currentTimeUs);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBarTouching && fromUser) {
            if (playing) {
                pause();
            }
            player.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekBarTouching = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekBarTouching = false;
    }
}

