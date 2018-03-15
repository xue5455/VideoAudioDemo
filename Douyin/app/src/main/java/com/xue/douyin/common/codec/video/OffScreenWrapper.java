package com.xue.douyin.common.codec.video;

import android.opengl.EGLContext;
import android.view.Surface;
import com.xue.douyin.common.preview.CameraFilter;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class OffScreenWrapper {
    private GLCore mGLCore;

    private WindowSurface mEncoderSurface;

    public OffScreenWrapper(EGLContext context, Surface surface) {
        mGLCore = new GLCore(context, GLCore.FLAG_RECORDABLE);
        mEncoderSurface = new WindowSurface(mGLCore, surface, true);
        mEncoderSurface.makeCurrent();
    }

    public void release() {
        if (mEncoderSurface == null) {
            return;
        }

        mEncoderSurface.release();
        mGLCore.release();
        mEncoderSurface = null;
        mGLCore = null;
    }

    public void draw(CameraFilter filter, float[] matrix, int textureId,long time) {
        mEncoderSurface.makeCurrent();
        filter.init();
        filter.draw(textureId, matrix);
        mEncoderSurface.setPresentationTime(time);
        mEncoderSurface.swapBuffers();
    }
}
