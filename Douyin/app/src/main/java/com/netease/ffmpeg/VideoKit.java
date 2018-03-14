package com.netease.ffmpeg;

import com.xue.douyin.common.util.LogUtil;

/**
 * Created by 薛贤俊 on 2018/3/12.
 */

public class VideoKit {
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("videokit");

    }

    public static native int exec(String[] cmds);
}
