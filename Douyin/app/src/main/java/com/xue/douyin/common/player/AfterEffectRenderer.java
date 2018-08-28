package com.xue.douyin.common.player;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.xue.douyin.common.preview.GLUtils;
import com.xue.douyin.common.preview.filters.ImageFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 薛贤俊 on 2018/4/25.
 */

public class AfterEffectRenderer implements GLSurfaceView.Renderer{

    private SurfaceTexture surfaceTexture;

    private int textureId;

    private ImageFilter filter;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textureId = GLUtils.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        surfaceTexture = new SurfaceTexture(textureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
