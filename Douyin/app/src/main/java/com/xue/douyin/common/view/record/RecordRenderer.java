package com.xue.douyin.common.view.record;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.xue.douyin.common.codec.video.VideoFrameData;
import com.xue.douyin.common.preview.CameraFilter;
import com.xue.douyin.common.preview.GLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class RecordRenderer implements GLSurfaceView.Renderer {

    private RecordSurfaceView mTarget;

    private CameraFilter mOldFilter;

    private CameraFilter mFilter;

    private int mTextureId;

    private SurfaceTexture mSurfaceTexture;

    private OnFrameAvailableListener mFrameListener;

    private float[] mMatrix = new float[16];

    public RecordRenderer(RecordSurfaceView target) {
        this.mTarget = target;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mTextureId = GLUtils.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mTarget.onSurfaceCreated(mSurfaceTexture, EGL14.eglGetCurrentContext());
        if(mFilter!=null){
            mFilter.release();
        }
        mFilter = new CameraFilter();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    public void setFilter(CameraFilter filter) {
        mOldFilter = mFilter;
        mFilter = filter;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        float matrix[] = new float[16];
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
        mSurfaceTexture.getTransformMatrix(matrix);

        if (mFrameListener != null) {
            mFrameListener.onFrameAvailable(new VideoFrameData(mFilter,
                    matrix, mSurfaceTexture.getTimestamp(), mTextureId));
        }

        mFilter.init();
        if (mOldFilter != null) {
            mOldFilter.release();
            mOldFilter = null;
        }
        mSurfaceTexture.getTransformMatrix(mMatrix);
        mFilter.draw(mTextureId, mMatrix);


    }

    public void setFrameListener(OnFrameAvailableListener listener) {
        this.mFrameListener = listener;
    }
}
