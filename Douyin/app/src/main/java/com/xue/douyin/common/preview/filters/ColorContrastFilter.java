package com.xue.douyin.common.preview.filters;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;

/**
 * Created by 薛贤俊 on 2018/3/26.
 * 对比度滤镜
 */

public class ColorContrastFilter extends ImageFilter {

    public static final String CONTRAST_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            " \n" +
            "uniform samplerExternalOES uTexture;\n" +
            " uniform float contrast;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     vec4 textureColor = texture2D(uTexture, vTextureCoord);\n" +
            "     \n" +
            "     gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);\n" +
            " }";

    private int mContrastLocation;

    private float mContrastValue;



    public ColorContrastFilter(float value) {
        this.mContrastValue = value;
    }

    @Override
    protected String getFragmentCode() {
        return CONTRAST_FRAGMENT_SHADER;
    }

    @Override
    protected void initFragmentArguments() {
        super.initFragmentArguments();
        mContrastLocation = glGetUniformLocation(getProgramId(), "contrast");
    }

    @Override
    protected void setFragmentAttrs() {
        glUniform1f(mContrastLocation, mContrastValue);
    }
}
