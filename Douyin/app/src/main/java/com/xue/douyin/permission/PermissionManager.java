package com.xue.douyin.permission;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.xue.douyin.application.AppProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class PermissionManager implements PermissionFragment.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_CODE = 888;

    private static PermissionManager sInstance;

    private PermissionLifeCycleImpl mCycleImpl;

    private OnPermissionGrantedListener mPermissionCallback;

    public static PermissionManager instance() {
        if (sInstance == null) {
            synchronized (PermissionManager.class) {
                if (sInstance == null) {
                    sInstance = new PermissionManager();
                }
            }
        }
        return sInstance;
    }

    private PermissionManager() {
        mCycleImpl = new PermissionLifeCycleImpl();
        AppProfile.registerActivityLifeCycle(mCycleImpl);
    }

    public void checkPermission(String[] permissions, OnPermissionGrantedListener listener) {
        if (permissions == null || listener == null) {
            return;
        }
        mPermissionCallback = listener;
        List<String> shouldRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(AppProfile.getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                shouldRequest.add(permission);
            } else {
                listener.onPermissionGranted(permission);
            }
        }
        if (shouldRequest.isEmpty()) {
            return;
        }
        String[] requests = new String[shouldRequest.size()];
        mCycleImpl.getFragment().setCallback(this);
        mCycleImpl.getFragment().requestPermissions(shouldRequest.toArray(requests),
                PERMISSION_REQUEST_CODE);

    }

    public void checkPermission(String permission, OnPermissionGrantedListener listener) {
        if (permission == null) {
            return;
        }
        checkPermission(new String[]{permission}, listener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (mPermissionCallback == null) {
            return;
        }
        if (permissions.length != grantResults.length) {
            return;
        }
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mPermissionCallback.onPermissionGranted(permissions[i]);
                    } else {
                        mPermissionCallback.onPermissionDenied(permissions[i]);
                    }
                }
        }
    }

    public interface OnPermissionGrantedListener {
        void onPermissionGranted(String permission);

        void onPermissionDenied(String permission);
    }


}
