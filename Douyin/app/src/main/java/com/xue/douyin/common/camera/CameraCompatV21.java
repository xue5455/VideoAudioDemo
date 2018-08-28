package com.xue.douyin.common.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.permission.PermissionManager;
import com.xue.douyin.permission.SimplePermissionCallback;

import java.util.Collections;

/**
 * Created by 薛贤俊 on 2018/3/7.
 */
@TargetApi(21)
public class CameraCompatV21 extends CameraCompat {

    private static final String TAG = "CameraCompatV21";

    private CameraManager mManager;

    private CameraDevice mCamera;

    private CameraCaptureSession mCaptureSession;

    private boolean mIsFlashLightOn = false;

    private CaptureRequest.Builder mRequestBuilder;

    private Surface mSurface;

    private final CameraCaptureSession.StateCallback mCaptureStateCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (mCamera == null) {
                        return;
                    }
                    mCaptureSession = session;
                    startRequest(session);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    LogUtil.e(TAG, "onConfigureFailed");
                }
            };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCamera = camera;
            mCameraReady = true;
            mStarted = false;
            if (mSurface != null) {
                startPreview();
            }
            LogUtil.d(TAG, "onOpened");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCamera = null;
            LogUtil.d(TAG, "onDisconnected");
            mCameraReady = false;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCamera = null;
            mCameraReady = false;
            LogUtil.d(TAG, "open camera onError " + error);
        }
    };


    public CameraCompatV21(Context context) {
        super(context);
    }

    @Override
    protected void initCameraInfo() {
        mManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (mManager == null) {
            return;
        }
        try {
            //获取可用摄像头列表
            for (String cameraId : mManager.getCameraIdList()) {
                //获取相机的相关参数
                CameraCharacteristics characteristics =
                        mManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == null) {
                    continue;
                }
                StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    setBackCameraId(cameraId);
                } else {
                    setFrontCameraId(cameraId);
                }
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, "", e);
        }
    }

    private void updateOutputSize() {
        try {
            CameraCharacteristics characteristics =
                    mManager.getCameraCharacteristics(mCameraType == FRONT_CAMERA ?
                            getFrontCameraIdV21() : getBackCameraIdV21());
            StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                return;
            }
            android.util.Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            setOutputSize(CameraUtil.findBestSize(DESIRED_HEIGHT, sizes));
        } catch (CameraAccessException e) {
            LogUtil.e(TAG, e);
        }

    }


    private void startRequest(CameraCaptureSession session) {
        try {
            session.setRepeatingRequest(mRequestBuilder.build(), null,
                    null);
        } catch (Throwable e) {
            LogUtil.e(TAG, "", e);
        }
    }

    private void abortSession() {
        if (mCaptureSession == null) {
            return;
        }
        try {
            mCaptureSession.abortCaptures();
        } catch (Throwable e) {
            LogUtil.e(TAG, "", e);
        }
    }


    @Override
    protected void onStartPreview() {
        try {
            mSurface = new Surface(mSurfaceTexture);
            mSurfaceTexture.setDefaultBufferSize(getOutputSize().width, getOutputSize().height);
            mRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mRequestBuilder.addTarget(mSurface);
            mRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            mRequestBuilder.set(CaptureRequest.FLASH_MODE, mIsFlashLightOn ?
                    CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF);
            mCamera.createCaptureSession(Collections.singletonList(mSurface),
                    mCaptureStateCallback, null);
        } catch (Throwable e) {
            LogUtil.e(TAG, "", e);
        }
    }

    @Override
    public void onStopPreview() {
        abortSession();
        mCamera.close();
        mCamera = null;
    }

    @Override
    protected void onOpenCamera(@CameraType final int cameraType) {
        PermissionManager.instance().checkPermission(new String[]{Manifest.permission.CAMERA},
                new SimplePermissionCallback() {
                    @Override
                    public void onPermissionGranted(String permission) {
                        initialize(cameraType);
                    }
                });
    }


    @SuppressLint("MissingPermission")
    private void initialize(@CameraType int cameraType) {
        try {
            mManager.openCamera(cameraType == FRONT_CAMERA ? getFrontCameraIdV21() :
                            getBackCameraIdV21(),
                    mStateCallback, null);
            updateOutputSize();
        } catch (Throwable e) {
            LogUtil.e(TAG, "", e);
        }
    }

    @Override
    protected void onTurnLight(boolean on) {
        if (mIsFlashLightOn == on) {
            return;
        }
        mIsFlashLightOn = on;
        if (mCameraType == FRONT_CAMERA && on) {
            return;
        }
        abortSession();
        mRequestBuilder.set(CaptureRequest.FLASH_MODE, mIsFlashLightOn ?
                CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF);
        startRequest(mCaptureSession);
    }
}
