package com.xue.douyin.common.util;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.xue.douyin.common.C;

/**
 * Created by 薛贤俊 on 2018/3/14.
 */

public class VideoUtil {

    /**
     * 获取视频信息
     *
     * @param url
     * @return 视频时长（单位微秒）
     */
    public static long getDuration(String url) {
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(url);
            int videoExt = selectVideoTrack(mediaExtractor);
            if (videoExt == -1) {
                videoExt = selectAudioTrack(mediaExtractor);
                if (videoExt == -1) {
                    return 0;
                }
            }
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(videoExt);
            long res = mediaFormat.containsKey(MediaFormat.KEY_DURATION) ? mediaFormat.getLong(MediaFormat.KEY_DURATION) : 0;//时长
            mediaExtractor.release();
            return res;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 查找视频轨道
     *
     * @param extractor
     * @return
     */
    public static int selectVideoTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                LogUtil.d("Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }
        return -1;
    }

    /**
     * 查找音频轨道
     *
     * @param extractor
     * @return
     */
    public static int selectAudioTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                LogUtil.d("Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }
        return -1;
    }

    public static int calculateAudioSamples(int seconds, int sampleRate, int samplePerFrame) {
        return (int) (Math.ceil(seconds * 1.0 * sampleRate / samplePerFrame)) * 2;
    }

    public static int calculateVideoSamples(int seconds, int fps) {
        return seconds * fps;
    }
}
