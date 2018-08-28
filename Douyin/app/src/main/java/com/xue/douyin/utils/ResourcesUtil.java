package com.xue.douyin.utils;

import android.support.annotation.DimenRes;

import com.xue.douyin.application.AppProfile;

/**
 * Created by 薛贤俊 on 2018/4/23.
 */

public class ResourcesUtil {

    public static int getDimensionPixel(@DimenRes int id) {
        return AppProfile.getContext().getResources().getDimensionPixelSize(id);
    }
}
