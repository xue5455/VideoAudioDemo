package com.xue.douyin.common.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by 薛贤俊 on 2018/4/11.
 */

public class ThreadUtil {
    public static Handler newHandlerThread(String tag) {
        HandlerThread thread = new HandlerThread(tag);
        thread.start();
        return new Handler(thread.getLooper());
    }
}
