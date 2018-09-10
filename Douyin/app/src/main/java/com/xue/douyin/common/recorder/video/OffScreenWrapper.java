package com.xue.douyin.common.recorder.video;

import android.opengl.EGLContext;
import android.view.Surface;

import com.xue.douyin.common.preview.filters.ImageFilter;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class OffScreenWrapper {
    private GLCore mGLCore;

    private WindowSurface mEncoderSurface;

    private ImageFilter mFilter;

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

    public void draw(ImageFilter filter, float[] matrix, int textureId, long time) {
        mEncoderSurface.makeCurrent();
        if (mFilter != filter && mFilter != null) {
            mFilter.release();
        }
        mFilter = filter;
        filter.init();
        filter.draw(textureId, matrix,0,0);
        mEncoderSurface.setPresentationTime(time);
        mEncoderSurface.swapBuffers();
    }
}
