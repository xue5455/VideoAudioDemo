package com.xue.douyin.common.preview;

import static android.opengl.GLES20.glUseProgram;

/**
 * Created by 薛贤俊 on 2018/3/7.
 */

public class TextureProgram {

    private int mProgramId;

    public TextureProgram(String vertexCode, String fragmentCode) {
        mProgramId = GLUtils.buildProgram(vertexCode, fragmentCode);
    }

    public void useProgram() {
        glUseProgram(mProgramId);
    }

    public int getProgramId() {
        return mProgramId;
    }

    public void enableAttrs() {

    }

    public void disableAttrs() {

    }
}
