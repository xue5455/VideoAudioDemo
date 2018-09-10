package com.xue.douyin.common.util;

import android.content.res.AssetManager;
import android.util.Log;

import com.xue.douyin.R;
import com.xue.douyin.application.AppProfile;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/15.
 */

public class FileUtils {
    /**
     * 此类用于生成合并视频所需要的文档
     *
     * @param strcontent 视频路径集合
     * @param filePath   生成的地址
     * @param fileName   生成的文件名
     */
    public static void writeTxtToFile(List<String> strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = "";
        for (int i = 0; i < strcontent.size(); i++) {
            strContent += "file " + strcontent.get(i) + "\r\n";
        }
        try {
            File file = new File(strFilePath);
            //检查文件是否存在，存在则删除
            if (file.isFile() && file.exists()) {
                file.delete();
            }
            file.getParentFile().mkdirs();
            file.createNewFile();
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
            Log.e("TestFile", "写入成功:" + strFilePath);
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void renameFile(String original, String dest) {
        File file = new File(original);
        file.renameTo(new File(dest));
    }

    public static void createFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            LogUtil.e(e);
        }
    }

    public static void closeSafely(OutputStream fos) {
        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            LogUtil.e(e);
        }

    }

    public static String readFromRaw(int rawId) {
        InputStream inputStream = AppProfile.getContext().getResources().openRawResource(rawId);
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        BufferedReader br = new BufferedReader(new InputStreamReader(bis));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
        } catch (Throwable e) {
            LogUtil.e(e);
        } finally {
            try {
                br.close();
                bis.close();
                inputStream.close();
            } catch (Throwable e) {

            }
        }
        return builder.toString();
    }
}
