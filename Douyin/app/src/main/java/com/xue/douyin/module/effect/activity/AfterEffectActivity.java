package com.xue.douyin.module.effect.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xue.douyin.R;
import com.xue.douyin.base.activity.BaseBlankActivity;
import com.xue.douyin.common.preview.filters.GlitchFilter;
import com.xue.douyin.common.preview.filters.ImageFilter;
import com.xue.douyin.common.preview.filters.ScaleFilter;
import com.xue.douyin.common.preview.filters.ShakeEffectFilter;
import com.xue.douyin.common.preview.filters.ShineWhiteFilter;
import com.xue.douyin.common.preview.filters.SoulOutFilter;
import com.xue.douyin.common.preview.filters.TestFilter;
import com.xue.douyin.common.util.ScreenUtil;
import com.xue.douyin.common.view.record.OnSurfaceCreatedCallback;
import com.xue.douyin.common.view.record.RecordSurfaceView;
import com.xue.douyin.module.effect.presenter.AfterEffectPresenter;
import com.xue.douyin.utils.ResourcesUtil;


/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class AfterEffectActivity extends BaseBlankActivity<AfterEffectPresenter>
        implements SurfaceHolder.Callback,OnSurfaceCreatedCallback{

    public static final String KEY_FINAL_PATH = "key:path";

    private RecordSurfaceView surfaceView;

    private View btnBack;

    private View btnEffect;

    private View flEffectPanel;

    private View btnPlay;

    private static final long DURATION = 300;

    /**
     * 播放按钮的动画
     */
    private ObjectAnimator playButtonShow;
    private ObjectAnimator playButtonHide;


    private TextView tvMaxSeconds;

    private TextView tvCurrentSeconds;

    private SeekBar seekBar;

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
        initContentView();
        mPresenter.init();
    }

    private void initContentView() {
        surfaceView = findViewById(R.id.sv_play);
        surfaceView.setOnSurfaceCreatedCallback(this);
//        surfaceView.setFilter(new ShakeEffectFilter());
//        surfaceView.setFilter(new SoulOutFilter());
        surfaceView.setFilter(new TestFilter());
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(getPresenter());

        btnEffect = findViewById(R.id.btn_effect);
        btnEffect.setOnClickListener(getPresenter());

        flEffectPanel = findViewById(R.id.fl_effect_panel);
        btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(getPresenter());

        tvMaxSeconds = findViewById(R.id.tv_max_seconds);
        tvCurrentSeconds = findViewById(R.id.tv_current_seconds);

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(getPresenter());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        mPresenter.start(holder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemNavigationBar();
    }

    @Override
    protected void onStop() {
        mPresenter.pause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mPresenter.stop();
        super.onDestroy();
    }

    public void showEffectPanel() {
        ValueAnimator animator = ValueAnimator.ofInt(0, ResourcesUtil.getDimensionPixel(R.dimen.effect_panel_height));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = flEffectPanel.getLayoutParams();
                params.height = height;
                flEffectPanel.setLayoutParams(params);
            }
        });
        animator.setDuration(DURATION);
        animator.start();
    }

    public void shrinkSurfaceView() {
        AnimatorSet set = new AnimatorSet();
        float scale = 1.0f * (ScreenUtil.getScreenHeight() - ResourcesUtil.getDimensionPixel(R.dimen.effect_panel_height)
                - ResourcesUtil.getDimensionPixel(R.dimen.effect_play_margin_top) -
                ResourcesUtil.getDimensionPixel(R.dimen.effect_play_margin_bottom)) / ScreenUtil.getScreenHeight();
        ObjectAnimator x = ObjectAnimator.ofFloat(surfaceView, View.SCALE_X, 1.0f, scale);
        surfaceView.setPivotX(ScreenUtil.getScreenWidth() / 2);


        ObjectAnimator y = ObjectAnimator.ofFloat(surfaceView, View.SCALE_Y, 1.0f, scale);
        surfaceView.setPivotY(0);

        ObjectAnimator translate = ObjectAnimator.ofFloat(surfaceView, View.TRANSLATION_Y, 0,
                ResourcesUtil.getDimensionPixel(R.dimen.effect_play_margin_top));


        set.setDuration(DURATION);
        set.playTogether(x, y, translate);
        set.start();
    }

    public void enlargeSurfaceView() {
        AnimatorSet set = new AnimatorSet();
        float scale = 1.0f * (ScreenUtil.getScreenHeight() - ResourcesUtil.getDimensionPixel(R.dimen.effect_panel_height)
                - ResourcesUtil.getDimensionPixel(R.dimen.effect_play_margin_top) -
                ResourcesUtil.getDimensionPixel(R.dimen.effect_play_margin_bottom)) / ScreenUtil.getScreenHeight();
        ObjectAnimator x = ObjectAnimator.ofFloat(surfaceView, View.SCALE_X, scale, 1.0f);
        surfaceView.setPivotX(ScreenUtil.getScreenWidth() / 2);

        ObjectAnimator y = ObjectAnimator.ofFloat(surfaceView, View.SCALE_Y, scale, 1.0f);
        surfaceView.setPivotY(0);
        ObjectAnimator translate = ObjectAnimator.ofFloat(surfaceView, View.TRANSLATION_Y,
                ResourcesUtil.getDimensionPixel(R.dimen.effect_play_margin_top), 0);
        set.setDuration(DURATION);
        set.playTogether(x, y, translate);
        set.start();
    }

    public void hideEffectPanel() {
        ValueAnimator animator = ValueAnimator.ofInt(ResourcesUtil.getDimensionPixel(R.dimen.effect_panel_height), 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = flEffectPanel.getLayoutParams();
                params.height = height;
                flEffectPanel.setLayoutParams(params);
            }
        });
        animator.setDuration(DURATION);
        animator.start();
    }

    public void showPlayButton() {
        if (playButtonShow == null) {
            playButtonShow = ObjectAnimator.ofFloat(btnPlay, View.ALPHA, btnPlay.getAlpha(), 1f);
            playButtonShow.setDuration(DURATION);
        }
        playButtonShow.setFloatValues(btnPlay.getAlpha(), 1f);
        playButtonShow.start();
    }

    public void hidePlayButton() {
        if (playButtonHide == null) {
            playButtonHide = ObjectAnimator.ofFloat(btnPlay, View.ALPHA, btnPlay.getAlpha(), 0f);
            playButtonHide.setDuration(DURATION);
        }
        playButtonHide.setFloatValues(btnPlay.getAlpha(), 0f);
        playButtonHide.start();
    }

    /**
     * 设置最大视频时长
     *
     * @param duration 微秒
     */
    public void setMaxSeconds(long duration) {
        long seconds = duration / 1000000L;
        if (seconds < 10) {
            tvMaxSeconds.setText("00:0" + seconds);
        } else {
            tvMaxSeconds.setText("00:" + seconds);
        }
    }

    public void setCurrentSeconds(long duration) {
        long seconds = duration / 1000000L;
        if (seconds < 10) {
            tvCurrentSeconds.setText("00:0" + seconds);
        } else {
            tvCurrentSeconds.setText("00:" + seconds);
        }
    }

    public void setSeekBarMax(long duration) {
        seekBar.setMax((int)duration);
    }


    public void setSeekBarProgress(long duration) {

        seekBar.setProgress((int) duration);
    }

    @Override
    public void onSurfaceCreated(final SurfaceTexture texture, EGLContext context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPresenter.start(texture);
            }
        });

    }
}
