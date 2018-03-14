package com.xue.douyin.common.camera;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.util.Size;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/7.
 */

public class CameraUtil {

    public static CameraCompat.CameraSize findBestSize(int height, List<Camera.Size> supportList) {
        Collections.sort(supportList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                if (o1.width > o2.width) {
                    return -1;
                } else if (o1.width < o2.width) {
                    return 1;
                }
                return 0;
            }
        });

        for (Camera.Size size : supportList) {
            if (size.height <= height) {
                //找到第一个相等或者小于width的尺寸
                return new CameraCompat.CameraSize(size);
            }
        }
        //如果不存在比width还小的，那就返回最小的那个
        return new CameraCompat.CameraSize(supportList.get(supportList.size() - 1));

    }

    @TargetApi(21)
    public static CameraCompat.CameraSize findBestSize(int height, Size[] sizes) {
        Arrays.sort(sizes, new Comparator<Size>() {
            @Override
            public int compare(android.util.Size o1, android.util.Size o2) {
                if (o1.getHeight() < o2.getHeight()) {
                    return -1;
                } else if (o1.getHeight() > o2.getHeight()) {
                    return 1;
                }
                return 0;
            }
        });
        for (android.util.Size size : sizes) {
            if (size.getHeight() >= height) {
                //找到第一个相等或者小于width的尺寸
                return new CameraCompat.CameraSize(size);
            }
        }
        //如果不存在比width还小的，那就返回最小的那个
        return new CameraCompat.CameraSize(sizes[sizes.length-1]);
    }
}
