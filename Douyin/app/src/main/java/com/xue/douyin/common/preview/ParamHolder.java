package com.xue.douyin.common.preview;

/**
 * Created by 薛贤俊 on 2018/3/13.
 */

public abstract class ParamHolder {

    private int mProgramId;

    public ParamHolder(int programId) {
        this.mProgramId = programId;
    }

    public abstract void enableAttrs();

    public abstract void disableAttrs();
}
