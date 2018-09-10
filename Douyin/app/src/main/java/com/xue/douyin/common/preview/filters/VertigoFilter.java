package com.xue.douyin.common.preview.filters;

import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.xue.douyin.R;
import com.xue.douyin.application.AppProfile;
import com.xue.douyin.common.preview.GLUtils;
import com.xue.douyin.common.preview.RenderBuffer;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_DST_ALPHA;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE8;
import static android.opengl.GLES20.GL_TEXTURE9;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by 薛贤俊 on 2018/9/7.
 */
public class VertigoFilter extends ImageFilter {

    private RenderBuffer mRenderBuffer;

    private RenderBuffer mRenderBuffer2;

    private boolean mFirstFrame = true;

    private int mVertigoProgram;

    private static final String VERTEX_CODE = "attribute vec2 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main(){\n" +
            "    gl_Position = vec4(aPosition.x,aPosition.y,0.0,1.0);\n" +
            "    vTextureCoord = aTextureCoord.xy;\n" +
            "}\n" +
            "\n";

    private static final String FRAGMENT_CODE = "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform sampler2D uTexture1;\n" +
            "uniform sampler2D uTexture2;\n" +
            "const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
            "void main(){\n" +
            "     float strength = 0.5;\n" +
            "     vec4 textureColor = texture2D(uTexture,vTextureCoord);\n" +
            "     mediump float blueColor = textureColor.b * 63.0;\n" +
            "     mediump vec2 quad1;\n" +
            "     quad1.y = floor(blueColor/8.0);\n" +
            "     quad1.x = floor(blueColor) - quad1.y*8.0;\n" +
            "     mediump vec2 quad2;\n" +
            "     quad2.y = floor(ceil(blueColor)/7.999);\n" +
            "     quad2.x = ceil(blueColor) - quad2.y * 8.0;\n" +
            "     highp vec2 texPos1;\n" +
            "     texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "     texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "     highp vec2 texPos2;\n" +
            "     texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "     texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "     lowp vec4 newColor1 = texture2D(uTexture1, texPos1);\n" +
            "     lowp vec4 newColor2 = texture2D(uTexture1, texPos2);\n" +
            "     lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            "     vec4 origin = mix(textureColor, vec4(newColor.rgb, textureColor.w), strength);\n" +
            "     lowp float luminance = dot(textureColor.rgb, luminanceWeighting);\n" +
            "     lowp vec3 greyScaleColor = vec3(luminance);\n" +
            "     gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, 3.0), textureColor.w);\n" +
            "}";


    private static final String FRAGMENT1 = "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform sampler2D uTexture1;\n" +
            "void main(){\n" +
            "     vec4 origin = texture2D(uTexture,vTextureCoord);\n" +
            "     vec4 last = texture2D(uTexture1,vTextureCoord);\n" +
            "     float alpha = 0.8;\n" +
            "     float lastAlpha = 1.0 - alpha;\n" +
            "     gl_FragColor = vec4(origin.r + last.r * 0.2,origin.g  + last.g * 0.2,origin.b  + last.b * 0.4,1.0);\n" +
            "}";


    private int mVertigoTexture;

    @Override
    public void draw(int textureId, float[] texMatrix, int canvasWidth, int canvasHeight) {


        if (mRenderBuffer == null) {
            mRenderBuffer = new RenderBuffer(GL_TEXTURE8, canvasWidth, canvasHeight);
            mVertigoProgram = GLUtils.buildProgram(VERTEX_CODE, FRAGMENT_CODE);
            mVertigoTexture = GLUtils.genTexture();
            android.opengl.GLUtils.texImage2D(mVertigoTexture, 0,
                    BitmapFactory.decodeResource(AppProfile.getContext().getResources(),
                            R.raw.lookup_vertigo), 0);

            mRenderBuffer2 = new RenderBuffer(GL_TEXTURE9, canvasWidth, canvasHeight);
        }


        super.draw(textureId, texMatrix, canvasWidth, canvasHeight);
        mRenderBuffer.bind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        disableArguments();
        mRenderBuffer.unbind();
        glUseProgram(mVertigoProgram);
        checkGlError("glUseProgram");
        int textureLocation = glGetUniformLocation(mVertigoProgram, "uTexture");
        int vPositionLocation = glGetAttribLocation(mVertigoProgram, "aPosition");
        int vTextureCoordLocation = glGetAttribLocation(mVertigoProgram, "aTextureCoord");
        int textureLocation1 = glGetUniformLocation(mVertigoProgram, "uTexture1");
        int textureLocation2 = glGetUniformLocation(mVertigoProgram, "uTexture2");
        mRendererInfo.getVertexBuffer().position(0);
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        glVertexAttribPointer(vPositionLocation, 2,
                GL_FLOAT, false, 0, mRendererInfo.getVertexBuffer());
        mRendererInfo.getTextureBuffer().position(0);
        GLES20.glEnableVertexAttribArray(vTextureCoordLocation);
        glVertexAttribPointer(vTextureCoordLocation, 2,
                GL_FLOAT, false, 0, mRendererInfo.getTextureBuffer());
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRenderBuffer.getTextureId());
        glUniform1i(textureLocation, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mVertigoTexture);
        glUniform1i(textureLocation1, 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);

        if (mFirstFrame) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRenderBuffer.getTextureId());
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRenderBuffer2.getTextureId());
        }
        glUniform1i(textureLocation2, 2);

        mRenderBuffer2.bind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        mRenderBuffer2.unbind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        mFirstFrame = true;
    }
}
