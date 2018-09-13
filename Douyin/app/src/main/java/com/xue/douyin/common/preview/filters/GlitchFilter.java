package com.xue.douyin.common.preview.filters;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform2fv;

/**
 * Created by 薛贤俊 on 2018/8/29.
 * 毛刺
 */
public class GlitchFilter extends ImageFilter {

    private static final String FRAGMENT1 = "#extension GL_OES_EGL_image_external : require\n" +
            " precision highp float;\n" +
            " varying vec2 vTextureCoord;\n" +
            " uniform samplerExternalOES uTexture;\n" +
            " uniform vec2 uScanLineJitter;//displacement threshold\n" +
            " uniform float uColorDrift;\n" +
            " uniform float uGlobalTime;\n" +
            "float nrand(in float x, in float y)\n" +
            "{\n" +
            "     return fract(sin(dot(vec2(x, y), vec2(12.9898, 78.233))) * 43758.5453);\n" +
            "}\n" +
            "void main(){\n" +
            "    float u = vTextureCoord.x;\n" +
            "    float v = vTextureCoord.y;\n" +
            "    float jitter = nrand(v,0.0) * 2.0 - 1.0;\n" +
            "    float drift = uColorDrift;\n" +
            "    float offsetParam = step(uScanLineJitter.y,abs(jitter));\n" +
            "    jitter = jitter * offsetParam * uScanLineJitter.x;\n" +
            "    vec4 color1 = texture2D(uTexture,fract(vec2( u + jitter,v)));\n" +
            "    vec4 color2 = texture2D(uTexture,fract(vec2(u + jitter + v*drift ,v)));\n" +
            "    gl_FragColor = vec4(color1.r,color2.g,color1.b,1.0);\n" +
            "}";

    private int mScanLineJitterLocation;
    private int mColorDriftLocation;
    private int mGlobalTimeLocation;

    private long mStartTime;

    private int mFrames = 0;

    /**
     * 动画总共8帧
     */
    private int mMaxFrames = 8;

    private float[] mDriftSequence = new float[]{0f, 0.03f, 0.032f, 0.035f, 0.03f, 0.032f, 0.031f, 0.029f, 0.025f};

    private float[] mJitterSequence = new float[]{0f, 0.03f, 0.01f, 0.02f, 0.05f, 0.055f, 0.03f, 0.02f, 0.025f};

    private float[] mThreshHoldSequence = new float[]{1.0f, 0.965f, 0.9f, 0.9f, 0.9f, 0.6f, 0.8f, 0.5f, 0.5f};

    @Override
    protected String getFragmentCode() {
        return FRAGMENT1;
    }

    @Override
    protected void initFragmentArguments() {
        super.initFragmentArguments();
        mScanLineJitterLocation = glGetUniformLocation(getProgramId(), "uScanLineJitter");
        mColorDriftLocation = glGetUniformLocation(getProgramId(), "uColorDrift");
        mGlobalTimeLocation = glGetUniformLocation(getProgramId(), "uGlobalTime");
    }

    @Override
    protected void setFragmentAttrs() {
        super.setFragmentAttrs();
        long time = System.currentTimeMillis();
        if (mStartTime == 0) {
            mStartTime = time;
        }
        glUniform1f(mGlobalTimeLocation, mFrames);
        mStartTime = time;

        float slDisplacement = mJitterSequence[mFrames];
        float slThreshold = mThreshHoldSequence[mFrames];
        float drift = mDriftSequence[mFrames];
        mFrames++;
        if (mFrames > mMaxFrames) {
            mFrames = 0;
        }
        glUniform2fv(mScanLineJitterLocation, 1, new float[]{slDisplacement, slThreshold}, 0);
        glUniform1f(mColorDriftLocation, drift);
    }
}
