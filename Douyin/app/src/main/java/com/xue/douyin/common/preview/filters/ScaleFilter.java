package com.xue.douyin.common.preview.filters;

import android.opengl.Matrix;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by 薛贤俊 on 2018/8/30.
 * 缩放
 */
public class ScaleFilter extends ImageFilter {

    private static final String VERTEX = "uniform mat4 uTexMatrix;\n" +
            "attribute vec2 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform mat4 uMvpMatrix;\n" +
            "void main(){\n" +
            "    gl_Position = uMvpMatrix * vec4(aPosition,0.1,1.0);\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}\n" +
            "\n";

    private int mScaleMatrixLocation;

    private static final float mScale = 0.3f;

    private int mFrames;

    private int mMaxFrames = 14;

    private int mMiddleFrames = mMaxFrames / 2;

    private float[] mScaleMatrix = new float[16];

    @Override
    protected String getVertexCode() {
        return VERTEX;
    }

    @Override
    protected void initVertexArguments() {
        super.initVertexArguments();
        mScaleMatrixLocation = glGetUniformLocation(getProgramId(), "uMvpMatrix");
    }

    @Override
    protected void setVertexAttrs() {
        super.setVertexAttrs();
        Matrix.setIdentityM(mScaleMatrix, 0);
        float progress;
        if (mFrames <= mMiddleFrames) {
            progress = mFrames * 1.0f / mMiddleFrames;
        } else {
            progress = 2f - mFrames * 1.0f / mMiddleFrames;
        }
        float scale = 1f + mScale * progress;
        Matrix.scaleM(mScaleMatrix, 0, scale, scale, scale);
        glUniformMatrix4fv(mScaleMatrixLocation, 1, false, mScaleMatrix, 0);
        mFrames++;
        if (mFrames > mMaxFrames) {
            mFrames = 0;
        }
    }
}
