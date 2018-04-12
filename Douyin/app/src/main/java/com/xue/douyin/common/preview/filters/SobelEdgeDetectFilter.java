package com.xue.douyin.common.preview.filters;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1fv;
import static android.opengl.GLES20.glUniform2fv;

/**
 * Created by 薛贤俊 on 2018/3/26.
 */

public class SobelEdgeDetectFilter extends ImageFilter {


    private static final String VERTEX_CODE = "uniform mat4 uTexMatrix;\n" +
            "attribute vec2 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "uniform highp float texelWidth;\n" +
            "uniform highp float texelHeight;\n" +
            "varying vec2 vTextureCoordinate;\n" +
            "varying vec2 vLeftTextureCoordinate;\n" +
            "varying vec2 vRightTextureCoordinate;\n" +
            "varying vec2 vTopTextureCoordinate;\n" +
            "varying vec2 vTopLeftTextureCoordinate;\n" +
            "varying vec2 vTopRightTextureCoordinate;\n" +
            "varying vec2 vBottomTextureCoordinate;\n" +
            "varying vec2 vBottomLeftTextureCoordinate;\n" +
            "varying vec2 vBottomRightTextureCoordinate;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_Position = vec4(aPosition,0.0,1.0);\n" +
            "    vTextureCoordinate = (uTexMatrix * aTextureCoord).xy;\n" +
            "    vec2 widthStep = vec2(texelWidth, 0.0);\n" +
            "    vec2 heightStep = vec2(0.0, texelHeight);\n" +
            "    vec2 widthHeightStep = vec2(texelWidth, texelHeight);\n" +
            "    vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);\n" +
            "    vLeftTextureCoordinate = vTextureCoordinate - widthStep;\n" +
            "    vRightTextureCoordinate = vTextureCoordinate + widthStep;\n" +
            "    vTopTextureCoordinate = vTextureCoordinate - heightStep;\n" +
            "    vTopLeftTextureCoordinate = vTextureCoordinate - widthHeightStep;\n" +
            "    vTopRightTextureCoordinate = vTextureCoordinate + widthNegativeHeightStep;\n" +
            "    vBottomTextureCoordinate = vTextureCoordinate + heightStep;\n" +
            "    vBottomLeftTextureCoordinate = vTextureCoordinate - widthNegativeHeightStep;\n" +
            "    vBottomRightTextureCoordinate = vTextureCoordinate + widthHeightStep;\n" +
            "}";

    private static final String FRAGMENT_CODE = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES uTexture;\n" +
            "varying vec2 vTextureCoordinate;\n" +
            "varying vec2 vLeftTextureCoordinate;\n" +
            "varying vec2 vRightTextureCoordinate;\n" +
            "varying vec2 vTopTextureCoordinate;\n" +
            "varying vec2 vTopLeftTextureCoordinate;\n" +
            "varying vec2 vTopRightTextureCoordinate;\n" +
            "varying vec2 vBottomTextureCoordinate;\n" +
            "varying vec2 vBottomLeftTextureCoordinate;\n" +
            "varying vec2 vBottomRightTextureCoordinate;\n" +
            "\n" +
            "void main(){\n" +
            "    float bottomLeftIntensity = texture2D(uTexture, vBottomLeftTextureCoordinate).r;\n" +
            "    float topRightIntensity = texture2D(uTexture, vTopRightTextureCoordinate).r;\n" +
            "    float topLeftIntensity = texture2D(uTexture, vTopLeftTextureCoordinate).r;\n" +
            "    float bottomRightIntensity = texture2D(uTexture, vBottomRightTextureCoordinate).r;\n" +
            "    float leftIntensity = texture2D(uTexture, vLeftTextureCoordinate).r;\n" +
            "    float rightIntensity = texture2D(uTexture, vRightTextureCoordinate).r;\n" +
            "    float bottomIntensity = texture2D(uTexture, vBottomTextureCoordinate).r;\n" +
            "    float topIntensity = texture2D(uTexture, vTopTextureCoordinate).r;\n" +
            "    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;\n" +
            "    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;\n" +
            "    float mag = length(vec2(h, v));\n" +
            "    gl_FragColor = vec4(vec3(mag), 1.0);\n" +
            "}\n";

    private int mTexelWidthLocation;

    private int mTexelHeightLocation;

    private int mOutputWidth;

    private int mOutputHeight;

    private float mTexelWidth;

    private float mTexelHeight;

    public SobelEdgeDetectFilter(int width, int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        mTexelWidth = 0.2f / mOutputWidth;
        mTexelHeight = 0.2f / mOutputHeight;
    }

    @Override
    protected String getFragmentCode() {
        return FRAGMENT_CODE;
    }

    @Override
    protected String getVertexCode() {
        return VERTEX_CODE;
    }

    @Override
    protected void initFragmentArguments() {
        super.initFragmentArguments();
    }

    @Override
    protected void initVertexArguments() {
        super.initVertexArguments();
        mTexelWidthLocation = glGetUniformLocation(getProgramId(), "texelWidth");
        mTexelHeightLocation = glGetUniformLocation(getProgramId(), "texelHeight");
    }

    @Override
    protected void setFragmentAttrs() {
        glUniform1f(mTexelWidthLocation, mTexelWidth);
        glUniform1f(mTexelHeightLocation, mTexelHeight);
    }
}
