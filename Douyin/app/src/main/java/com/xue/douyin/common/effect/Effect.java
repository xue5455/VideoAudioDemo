package com.xue.douyin.common.effect;

import com.xue.douyin.common.preview.ParamHolder;
import com.xue.douyin.common.preview.TextureProgram;

/**
 * Created by 薛贤俊 on 2018/3/13.
 * 特效基类
 */

public abstract class Effect<T extends ParamHolder> {
    //对应的OpenGL Program
    private TextureProgram mProgram;

    private T mParamHolder;

    /**
     * 特效开始的帧数
     */
    private int mStartFrame;

    /**
     * 特效结束的帧数
     */
    private int mEndFrame;

    /**
     * 当前对应的帧数,从零开始，一帧一帧增加
     */
    private int mFrameNum;

    protected abstract String getVertexCode();

    protected abstract String getFragmentCode();

    public Effect() {
        mProgram = new TextureProgram(getVertexCode(), getFragmentCode());
    }

    public void increaseFrame() {
        mFrameNum++;
    }

    public void reset() {
        mFrameNum = 0;
    }

    public void takeEffect() {
        mProgram.useProgram();
    }

    public abstract void onDraw();


}
