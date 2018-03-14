package com.xue.douyin.common.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class StorageUtil {
    private static final String APP_DIRECTORY_NAME = "DYDemo";

    private static boolean sMounted = false;

    private static String APP_DIRECTORY = null;

    public static String getExternalStoragePath() {
        if (APP_DIRECTORY != null) {
            return APP_DIRECTORY;
        }
        sMounted = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sMounted) {
            APP_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + File.separator + APP_DIRECTORY_NAME;
            File file = new File(APP_DIRECTORY);
            if (file.exists() && !file.isDirectory()) {
                file.delete();
            }
            if(!file.exists()){
                file.mkdirs();
            }
        }
        return APP_DIRECTORY;
    }
}
