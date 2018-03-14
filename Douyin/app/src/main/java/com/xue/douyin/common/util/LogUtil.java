package com.xue.douyin.common.util;

import android.util.Log;

/**
 * Created by 薛贤俊 on 2018/3/1.
 */

public class LogUtil {

    private static final String TAG = "DouyinDemo";

    public static void e(String tag, String msg, Throwable e) {
        Log.e(tag, msg, e);
    }

    public static void e(String tag, Throwable e) {
        Log.e(tag, "", e);
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(Throwable e) {
        e(TAG, e);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }
}
