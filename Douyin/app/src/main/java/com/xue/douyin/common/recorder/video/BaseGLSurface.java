package com.xue.douyin.common.recorder.video;

import android.opengl.EGL14;
import android.opengl.EGLSurface;

import com.xue.douyin.common.util.LogUtil;

/**
 * Created by 薛贤俊 on 2018/3/8.
 */

public class BaseGLSurface {
    protected static final String TAG = "BaseGLSurface";

    protected GLCore mGlCore;

    private EGLSurface mGlSurface = EGL14.EGL_NO_SURFACE;

    protected BaseGLSurface(GLCore core) {
        mGlCore = core;
    }

    public void createWindowSurface(Object surface) {
        if (mGlSurface != EGL14.EGL_NO_SURFACE) {
            LogUtil.d(TAG, "try to create window surface redundantly");
            return;
        }
        mGlSurface = mGlCore.createWindowSurface(surface);
    }

    /**
     * Release the EGL surface.
     */
    public void releaseEglSurface() {
        mGlCore.releaseSurface(mGlSurface);
        mGlSurface = EGL14.EGL_NO_SURFACE;
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        mGlCore.makeCurrent(mGlSurface);
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     *
     * @return false on failure
     */
    public boolean swapBuffers() {
        boolean result = mGlCore.swapBuffers(mGlSurface);
        if (!result) {
            LogUtil.d(TAG, "WARNING: swapBuffers() failed");
        }
        return result;
    }

    /**
     * Sends the presentation time stamp to EGL.
     */
    public void setPresentationTime(long nanoSeconds) {
        mGlCore.setPresentationTime(mGlSurface, nanoSeconds);
    }

}
