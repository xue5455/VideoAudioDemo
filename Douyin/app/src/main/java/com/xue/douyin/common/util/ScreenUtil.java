package com.xue.douyin.common.util;

import android.util.DisplayMetrics;

import com.xue.douyin.application.AppProfile;

/**
 * Created by 薛贤俊 on 2018/4/19.
 */

public class ScreenUtil {

    private static int sScreenWidth;

    private static int sScreenHeight;

    private static void init() {
        DisplayMetrics dm = AppProfile.getContext().getApplicationContext().getResources().getDisplayMetrics();
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;
    }

    public static int getScreenWidth() {
        if (sScreenWidth == 0) {
            init();
        }
        return sScreenWidth;
    }

    public static int getScreenHeight() {
        if (sScreenHeight == 0) {
            init();
        }
        return sScreenHeight;
    }
}
