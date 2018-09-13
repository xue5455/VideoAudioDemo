package com.xue.douyin.common.preview.filters;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_DST_ALPHA;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by 薛贤俊 on 2018/8/27.
 * 灵魂出窍
 */
public class SoulOutFilter extends ImageFilter {
    private static final String VERTEX = "uniform mat4 uTexMatrix;\n" +
            "attribute vec2 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform mat4 uMvpMatrix;\n" +
            "void main(){\n" +
            "    gl_Position = uMvpMatrix * vec4(aPosition,0.1,1.0);\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}\n";


    private static final String FRAGMENT = "#extension GL_OES_EGL_image_external : require\n" +
            " precision mediump float;\n" +
            " varying vec2 vTextureCoord;\n" +
            " uniform samplerExternalOES uTexture;\n" +
            " uniform float uAlpha;\n" +
            " void main(){\n" +
            "      gl_FragColor = vec4(texture2D(uTexture,vTextureCoord).rgb,uAlpha);\n" +
            " }";


    private float mProgress = 0.0f;

    private int mFrames = 0;

    private static final int mMaxFrames = 15;

    private static final int mSkipFrames = 8;

    private float[] mMvpMatrix = new float[16];

    private int mMvpMatrixLocation;

    private int mAlphaLocation;

    public SoulOutFilter() {
    }

    @Override
    protected String getVertexCode() {
        return VERTEX;
    }

    @Override
    protected String getFragmentCode() {
        return FRAGMENT;
    }

    @Override
    protected void initVertexArguments() {
        super.initVertexArguments();
        mMvpMatrixLocation = glGetUniformLocation(getProgramId(), "uMvpMatrix");
    }

    @Override
    protected void initFragmentArguments() {
        super.initFragmentArguments();
        mAlphaLocation = glGetUniformLocation(getProgramId(), "uAlpha");
    }

    @Override
    protected void onDraw(int textureId, float[] texMatrix) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);
        mProgress = (float) mFrames / mMaxFrames;
        if (mProgress > 1f) {
            mProgress = 0f;
        }
        mFrames++;
        if (mFrames > mMaxFrames + mSkipFrames) {
            mFrames = 0;
        }
        setVertexAttrs();
        setFragmentAttrs();
        Matrix.setIdentityM(mMvpMatrix, 0);
        glUniformMatrix4fv(mMvpMatrixLocation, 1, false, mMvpMatrix, 0);
        float backAlpha = 1f;
        float alpha = 0f;
        if (mProgress > 0f) {
            alpha = 0.2f - mProgress * 0.2f;
            backAlpha = 1 - alpha;
        }
        glUniform1f(mAlphaLocation, backAlpha);
        glUniformMatrix4fv(mUniformTexMatrixLocation, 1, false, texMatrix, 0);
        mRendererInfo.getVertexBuffer().position(0);
        glVertexAttribPointer(mAttrPositionLocation, 2,
                GL_FLOAT, false, 0, mRendererInfo.getVertexBuffer());
        mRendererInfo.getTextureBuffer().position(0);
        glVertexAttribPointer(mAttrTexCoordLocation, 2,
                GL_FLOAT, false, 0, mRendererInfo.getTextureBuffer());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        if (mProgress > 0f) {
            glUniform1f(mAlphaLocation, alpha);
            float scale = 1.0f + 1f * mProgress;
            Matrix.scaleM(mMvpMatrix, 0, scale, scale, scale);
            glUniformMatrix4fv(mMvpMatrixLocation, 1, false, mMvpMatrix, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
        glDisable(GL_BLEND);
    }

}
