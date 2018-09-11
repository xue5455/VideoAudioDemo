package com.xue.douyin.common.preview.filters;

import android.opengl.Matrix;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by 薛贤俊 on 2018/3/26.
 * 抖音-抖动滤镜
 */
public class ShakeEffectFilter extends ImageFilter {

    private static final String VERTEX = "uniform mat4 uTexMatrix;\n" +
            "attribute vec2 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform mat4 uMvpMatrix;\n" +
            "void main(){\n" +
            "    gl_Position = uMvpMatrix * vec4(aPosition,0.1,1.0);\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}";
    private static final String FRAGMENT = "#extension GL_OES_EGL_image_external : require\n" +
            " precision mediump float;\n" +
            " varying vec2 vTextureCoord;\n" +
            " uniform samplerExternalOES uTexture;\n" +
            " uniform float uTextureCoordOffset;\n" +
            " void main(){\n" +
            "      vec4 blue = texture2D(uTexture,vTextureCoord);\n" +
            "      vec4 green = texture2D(uTexture,vec2(vTextureCoord.x + uTextureCoordOffset,vTextureCoord.y + uTextureCoordOffset));\n" +
            "      vec4 red = texture2D(uTexture,vec2(vTextureCoord.x - uTextureCoordOffset,vTextureCoord.y - uTextureCoordOffset));\n" +
            "      gl_FragColor = vec4(red.x,green.y,blue.z,blue.w);\n" +
            "}";

    private float[] mMvpMatrix = new float[16];

    private int mMvpMatrixLocation;

    private int mTextureCoordOffsetLocation;

    private float mProgress = 0.0f;

    private int mFrames = 0;

    private static final int mMaxFrames = 8;

    private static final int mSkipFrames = 4;

    public ShakeEffectFilter() {
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
    protected void initFragmentArguments() {
        super.initFragmentArguments();
        mTextureCoordOffsetLocation = glGetUniformLocation(getProgramId(), "uTextureCoordOffset");
    }

    @Override
    protected void setFragmentAttrs() {
        float textureCoordOffset = 0.01f * mProgress;
        glUniform1f(mTextureCoordOffsetLocation, textureCoordOffset);
    }

    @Override
    protected void initVertexArguments() {
        super.initVertexArguments();
        mMvpMatrixLocation = glGetUniformLocation(getProgramId(), "uMvpMatrix");
    }


    @Override
    protected void onDraw(int textureId, float[] texMatrix) {
        mProgress = (float) mFrames / mMaxFrames;
        if (mProgress > 1f) {
            mProgress = 0f;
        }
        mFrames++;
        if (mFrames > mMaxFrames + mSkipFrames) {
            mFrames = 0;
        }
        super.onDraw(textureId, texMatrix);
    }

    @Override
    protected void setVertexAttrs() {
        super.setVertexAttrs();
        float scale = 1.0f + 0.2f * mProgress;
        Matrix.setIdentityM(mMvpMatrix, 0);
        Matrix.scaleM(mMvpMatrix, 0, scale, scale, 1.0f);
        glUniformMatrix4fv(mMvpMatrixLocation, 1, false, mMvpMatrix, 0);
    }
}
