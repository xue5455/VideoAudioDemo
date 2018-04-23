package com.xue.douyin.common.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.util.Size;
import android.util.SparseArray;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 薛贤俊 on 2018/3/7.
 */

public abstract class CameraCompat {

    private boolean mSwitchFlag;

    protected static final int DESIRED_HEIGHT = 720;

    protected Context mContext;

    public static final int FRONT_CAMERA = 1;

    public static final int BACK_CAMERA = 2;

    @IntDef({FRONT_CAMERA, BACK_CAMERA})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraType {

    }

    protected SurfaceTexture mSurfaceTexture;
    /**
     * 是否支持闪光灯
     */
    private boolean mSupportFlash = false;
    /**
     * 相机是否初始化完成
     */
    protected boolean mCameraReady;
    /**
     * 是否开始预览
     */
    protected boolean mStarted;

    @CameraType
    protected int mCameraType = BACK_CAMERA;

    private CameraSize mOutputSize;

    private SparseArray<String> mCameraInfo = new SparseArray<>(2);

    public CameraCompat(Context context) {
        this.mContext = context;
        mSupportFlash = context.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        initCameraInfo();
    }

    public void setSurfaceTexture(SurfaceTexture texture) {
        this.mSurfaceTexture = texture;
    }

    protected void setFrontCameraId(String id) {
        mCameraInfo.put(FRONT_CAMERA, id);
    }

    protected void setFrontCameraId(int id) {
        mCameraInfo.put(FRONT_CAMERA, String.valueOf(id));
    }

    protected void setBackCameraId(int id) {
        mCameraInfo.put(BACK_CAMERA, String.valueOf(id));
    }

    protected void setBackCameraId(String id) {
        mCameraInfo.put(BACK_CAMERA, id);
    }

    protected int getFrontCameraIdV19() {
        return Integer.valueOf(mCameraInfo.get(FRONT_CAMERA));
    }

    protected int getBackCameraIdV19() {
        return Integer.valueOf(mCameraInfo.get(BACK_CAMERA));
    }

    protected String getFrontCameraIdV21() {
        return mCameraInfo.get(FRONT_CAMERA);
    }

    protected String getBackCameraIdV21() {
        return mCameraInfo.get(BACK_CAMERA);
    }

    protected abstract void initCameraInfo();

    public void startPreview() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("you must start camera preview in main thread");
        }
        if (mStarted) {
            return;
        }
        if (mSwitchFlag) {
            mStarted = true;
        }
        if (!mCameraReady) {
            openCamera(mCameraType);
            return;
        }
        if (mSurfaceTexture == null) {
            return;
        }
        mStarted = true;
        onStartPreview();
    }

    protected abstract void onStartPreview();

    public void stopPreview(boolean releaseSurface) {
        if (!mStarted) {
            return;
        }
        mCameraReady = false;
        mStarted = false;
        if (releaseSurface) {
            mSurfaceTexture = null;
        }
        onStopPreview();
    }

    public abstract void onStopPreview();

    protected void openCamera(@CameraType int cameraType) {
        mCameraType = cameraType;
        onOpenCamera(cameraType);
    }

    protected abstract void onOpenCamera(@CameraType int cameraType);

    public CameraSize getOutputSize() {
        return mOutputSize;
    }

    public void setOutputSize(CameraSize outputSize) {
        this.mOutputSize = outputSize;
    }


    public void turnLight(boolean on) {
        if (!mSupportFlash) {
            return;
        }
        onTurnLight(on);
    }

    protected abstract void onTurnLight(boolean on);


    public void switchCamera() {
        if (!mStarted) {
            return;
        }
        mSwitchFlag = true;
        turnLight(false);
        stopPreview(false);
        mCameraType = mCameraType == FRONT_CAMERA ? BACK_CAMERA : FRONT_CAMERA;
        startPreview();
        mSwitchFlag = false;
    }

    public static CameraCompat newInstance(Context context) {
        int api = Build.VERSION.SDK_INT;
        if (api >= 21) {
            return new CameraCompatV21(context);
        } else {
            return new CameraCompatV19(context);
        }
//        return new CameraCompatV19(context);
    }

    public static class CameraSize {
        public int width;
        public int height;

        public CameraSize(Camera.Size size) {
            width = size.width;
            height = size.height;
        }

        @TargetApi(21)
        public CameraSize(Size size) {
            width = size.getWidth();
            height = size.getHeight();
        }
    }

}
