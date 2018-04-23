package com.xue.douyin.module.effect.presenter;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.xue.douyin.common.preview.GLUtils;
import com.xue.douyin.common.preview.filters.ImageFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glViewport;

/**
 * Created by 薛贤俊 on 2018/4/18.
 */

public class PlayRenderer implements GLSurfaceView.Renderer {

    private ImageFilter filter;

    private int textureId;

    private SurfaceTexture surfaceTexture;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textureId = GLUtils.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        surfaceTexture = new SurfaceTexture(textureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
