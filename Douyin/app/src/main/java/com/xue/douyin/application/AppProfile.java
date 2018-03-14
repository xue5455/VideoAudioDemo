package com.xue.douyin.application;

import android.app.Application;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class AppProfile {
    public static Application sContext;

    public static void setContext(Application context) {
        sContext = context;
    }

    public static Application getContext() {
        return sContext;
    }

    public static void registerActivityLifeCycle(Application.ActivityLifecycleCallbacks cb) {
        sContext.registerActivityLifecycleCallbacks(cb);
    }
}
