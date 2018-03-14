package com.xue.douyin.application;

import android.app.Application;

import com.xue.douyin.permission.PermissionManager;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class DYApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppProfile.setContext(this);
        PermissionManager.instance();
    }
}
