package com.xue.douyin.common.preview.filters;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.xue.douyin.common.preview.RendererInfo;
import com.xue.douyin.common.preview.TextureProgram;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by 薛贤俊 on 2018/3/7.
 */

public class ImageFilter {
    /**
     * 默认代码
     */
    private static final String FRAGMENT_CODE =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES uTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(uTexture,vTextureCoord);\n" +
                    "}\n";
    /**
     * 默认代码
     */
    private static final String VERTEX_CODE =
            "uniform mat4 uTexMatrix;\n" +
                    "attribute vec2 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = vec4(aPosition,0.0,1.0);\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    protected RendererInfo mRendererInfo = new RendererInfo();

    private ThreadLocal<TextureProgram> mProgram = new ThreadLocal<>();

    protected int mAttrPositionLocation;

    protected int mAttrTexCoordLocation;

    protected int mUniformTexMatrixLocation;

    protected int mTextureLocation;

    protected int mWidth;

    protected int mHeight;

    public void init() {
        if (mProgram.get() != null) {
            return;
        }
        mProgram.set(new TextureProgram(getVertexCode(), getFragmentCode()));
        onInit();
    }

    protected void onInit() {
        initVertexArguments();
        initFragmentArguments();
    }


    protected String getVertexCode() {
        return VERTEX_CODE;
    }

    protected String getFragmentCode() {
        return FRAGMENT_CODE;
    }

    protected int getProgramId() {
        return mProgram.get().getProgramId();
    }

    protected void initVertexArguments() {
        mAttrPositionLocation = glGetAttribLocation(getProgramId(), "aPosition");
        mAttrTexCoordLocation = glGetAttribLocation(getProgramId(), "aTextureCoord");
        mUniformTexMatrixLocation = glGetUniformLocation(getProgramId(), "uTexMatrix");
    }

    protected void initFragmentArguments() {
        mTextureLocation = glGetUniformLocation(getProgramId(),"uTexture");
    }

    protected void enableArguments() {
        glEnableVertexAttribArray(mAttrPositionLocation);
        glEnableVertexAttribArray(mAttrTexCoordLocation);
    }

    protected void disableArguments() {
        glDisableVertexAttribArray(mAttrPositionLocation);
        glDisableVertexAttribArray(mAttrTexCoordLocation);
    }

    protected void setFragmentAttrs() {

    }

    protected void setVertexAttrs() {

    }

    protected void onDraw(int textureId, float[] texMatrix) {
        setVertexAttrs();
        setFragmentAttrs();
        glUniformMatrix4fv(mUniformTexMatrixLocation, 1, false, texMatrix, 0);
        checkGlError("glUniformMatrix4fv");
        mRendererInfo.getVertexBuffer().position(0);
        glVertexAttribPointer(mAttrPositionLocation, 2,
                GL_FLOAT, false, 0, mRendererInfo.getVertexBuffer());
        checkGlError("glVertexAttribPointer");
        mRendererInfo.getTextureBuffer().position(0);
        glVertexAttribPointer(mAttrTexCoordLocation, 2,
                GL_FLOAT, false, 0, mRendererInfo.getTextureBuffer());
        checkGlError("glVertexAttribPointer");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    protected void onDraw(int textureId, float[] texMatrix, int width, int height) {
        mWidth = width;
        mHeight = height;
        onDraw(textureId, texMatrix);
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e("GLError", msg);
        }
    }

    public void draw(int textureId, float[] texMatrix, int width, int height) {
        if (mProgram == null || textureId == -1) {
            return;
        }
        mProgram.get().useProgram();
        checkGlError("glUseProgram");
        enableArguments();
        onDraw(textureId, texMatrix, width, height);
        disableArguments();
    }

    public void release() {
        if (mProgram == null || mProgram.get() == null) {
            return;
        }
        GLES20.glDeleteProgram(mProgram.get().getProgramId());
        mProgram.set(null);
    }
}
