package com.xue.douyin.common.preview.filters;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;

/**
 * Created by 薛贤俊 on 2018/8/30.
 * 闪白
 */
public class ShineWhiteFilter extends ImageFilter {
    private static final String FRAGMENT = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES uTexture;\n" +
            "uniform float uAdditionalColor;\n" +
            "void main(){\n" +
            "    vec4 color = texture2D(uTexture,vTextureCoord);\n" +
            "    gl_FragColor = vec4(color.r + uAdditionalColor,color.g + uAdditionalColor,color.b + uAdditionalColor,color.a);\n" +
            "}";

    private int mAdditionColorLocation;

    private int mFrames;

    private int mMaxFrames = 8;

    private int mHalfFrames = mMaxFrames / 2;

    @Override
    protected String getFragmentCode() {
        return FRAGMENT;
    }

    @Override
    protected void initFragmentArguments() {
        super.initFragmentArguments();
        mAdditionColorLocation = glGetUniformLocation(getProgramId(), "uAdditionalColor");
    }

    @Override
    protected void setFragmentAttrs() {
        super.setFragmentAttrs();
        float progress;
        if (mFrames <= mHalfFrames) {
            progress = mFrames * 1.0f / mHalfFrames;
        } else {
            progress = 2.0f - mFrames * 1.0f / mHalfFrames;
        }
        mFrames++;
        if (mFrames > mMaxFrames) {
            mFrames = 0;
        }
        glUniform1f(mAdditionColorLocation, progress);
    }
}
