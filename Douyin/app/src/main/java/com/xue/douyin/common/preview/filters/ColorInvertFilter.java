package com.xue.douyin.common.preview.filters;

/**
 * Created by 薛贤俊 on 2018/3/24.
 * 负片
 */

public class ColorInvertFilter extends ImageFilter {
    public static final String FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES uTexture;\n" +
            "void main()\n" +
            "{\n" +
            "    lowp vec4 textureColor = texture2D(uTexture, vTextureCoord);\n" +
            "    \n" +
            "    gl_FragColor = vec4((1.0 - textureColor.rgb), textureColor.w);\n" +
            "}";

    @Override
    protected String getFragmentCode() {
        return FRAGMENT_SHADER;
    }
}
