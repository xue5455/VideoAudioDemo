package com.xue.douyin.permission;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class PermissionFragment extends Fragment {

    private OnRequestPermissionsResultCallback mCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = new View(getActivity());
        view.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        view.setVisibility(View.INVISIBLE);
        return view;
    }

    public void setCallback(OnRequestPermissionsResultCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (mCallback != null) {
            mCallback.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public interface OnRequestPermissionsResultCallback {
        void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults);
    }

}
