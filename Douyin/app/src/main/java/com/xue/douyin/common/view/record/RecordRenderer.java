package com.xue.douyin.common.view.record;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.xue.douyin.common.recorder.video.VideoFrameData;
import com.xue.douyin.common.preview.filters.ImageFilter;
import com.xue.douyin.common.preview.GLUtils;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glViewport;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class RecordRenderer implements GLSurfaceView.Renderer {

    private RecordSurfaceView mTarget;

    private ImageFilter mOldFilter;

    private List<ImageFilter> mFilterList;

    private ImageFilter mFilter;

    private int mTextureId;

    private SurfaceTexture mSurfaceTexture;

    private OnFrameAvailableListener mFrameListener;

    private int mPreviewWidth;

    private int mPreviewHeight;

    private float[] mMatrix = new float[16];

    private int mCanvasWidth;

    private int mCanvasHeight;

    public RecordRenderer(RecordSurfaceView target) {
        this.mTarget = target;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mTextureId = GLUtils.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mTarget.onSurfaceCreated(mSurfaceTexture, EGL14.eglGetCurrentContext());
        if (mFilter == null) {
            mFilter = new ImageFilter();
        } else {
            mFilter.release();
        }

//        mFilter = new ImageFilter();
    }

    public void setPreviewSize(int width, int height) {
        mPreviewHeight = height;
        mPreviewWidth = width;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCanvasWidth = width;
        mCanvasHeight = height;
        glViewport(0, 0, width, height);
    }

    public void setFilter(ImageFilter filter) {
        mOldFilter = mFilter;
        mFilter = filter;
    }

    public void setFilterList(List<ImageFilter> filterList) {
        mFilterList = filterList;
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        if (mFilter == null) {
            return;
        }
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
        if (mFilterList != null && !mFilterList.isEmpty()) {
            for (ImageFilter filter : mFilterList) {
                filter.draw(mTextureId, mMatrix, mCanvasWidth, mCanvasHeight);
            }
        } else {
            mFilter.draw(mTextureId, mMatrix, mCanvasWidth, mCanvasHeight);
        }
    }

    public void setFrameListener(OnFrameAvailableListener listener) {
        this.mFrameListener = listener;
    }
}
