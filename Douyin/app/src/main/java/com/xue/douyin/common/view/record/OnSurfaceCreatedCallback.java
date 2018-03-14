package com.xue.douyin.common.view.record;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public interface OnSurfaceCreatedCallback {
    void onSurfaceCreated(SurfaceTexture texture, EGLContext context);
}
