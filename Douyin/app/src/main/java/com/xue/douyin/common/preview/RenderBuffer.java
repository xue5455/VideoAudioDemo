package com.xue.douyin.common.preview;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.xue.douyin.common.preview.filters.ImageFilter.checkGlError;

/**
 * Created by 薛贤俊 on 2018/9/7.
 */
public class RenderBuffer {
    private int mTextureId;

    private int mActiveTextureUnit;

    private int mRenderBufferId;

    private int mFrameBufferId;

    private int mWidth, mHeight;

    public RenderBuffer(int activeTextureUnit, int width, int height) {
        this.mActiveTextureUnit = activeTextureUnit;
        this.mWidth = width;
        this.mHeight = height;
        int[] buffer = new int[1];
        GLES20.glActiveTexture(activeTextureUnit);
        mTextureId = GLUtils.genTexture();
        IntBuffer texBuffer =
                ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, texBuffer);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        // Generate frame buffer
        GLES20.glGenFramebuffers(1, buffer, 0);
        mFrameBufferId = buffer[0];
        // Bind frame buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
        // Generate render buffer
        GLES20.glGenRenderbuffers(1, buffer, 0);
        mRenderBufferId = buffer[0];
        // Bind render buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mRenderBufferId);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
    }

    public void bind() {
        GLES20.glViewport(0, 0, mWidth, mHeight);
        checkGlError("glViewport");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
        checkGlError("glBindFramebuffer");
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mTextureId, 0);
        checkGlError("glFramebufferTexture2D");
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, mRenderBufferId);
        checkGlError("glFramebufferRenderbuffer");
    }


    public void unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public int getTextureId(){
        return mTextureId;
    }
}
