package com.xue.douyin.ffmpeg;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.xue.douyin.common.util.FileUtils;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.StorageUtil;
import com.xue.douyin.common.util.VideoUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/3/14.
 */

public class VideoCommand {

    public ArrayList<String> mContent;

    public VideoCommand() {
        mContent = new ArrayList<>();
    }

    public VideoCommand append(String cmd) {
        mContent.add(cmd);
        return this;
    }

    public VideoCommand append(long cmd) {
        return append(String.valueOf(cmd));
    }

    public VideoCommand append(int cmd) {
        return append(String.valueOf(cmd));
    }

    public VideoCommand append(float cmd) {
        return append(String.valueOf(cmd));
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : mContent) {
            stringBuilder.append(str).append(" ");
        }
        return stringBuilder.toString();
    }

    public String[] toArray() {
        String[] array = new String[mContent.size()];
        mContent.toArray(array);
        LogUtil.d(toString());
        return array;
    }


    /**
     * 无损合并多个视频
     * <p>
     * 注意：此方法要求视频格式非常严格，需要合并的视频必须分辨率相同，帧率和码率也得相同
     */
    public static VideoCommand mergeVideo(List<String> videos, String output) {
        String appDir = StorageUtil.getExternalStoragePath() + File.separator;
        String fileName = "ffmpeg_concat.txt";
        FileUtils.writeTxtToFile(videos, appDir, fileName);
        VideoCommand cmd = new VideoCommand();
        cmd.append("ffmpeg").append("-y").append("-f").append("concat").append("-safe")
                .append("0").append("-i").append(appDir + fileName)
                .append("-c").append("copy").append("-threads").append("5").append(output);
        return cmd;
    }


    /**
     * 添加背景音乐
     *
     * @param inputVideo  视频文件
     * @param inputMusic  音频文件
     * @param output      输出路径
     * @param videoVolume 视频原声音音量(例:0.7为70%)
     * @param audioVolume 背景音乐音量(例:1.5为150%)
     */
    public static VideoCommand music(String inputVideo, String inputMusic, String output, float videoVolume, float audioVolume) {
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(inputVideo);
        } catch (IOException e) {
            return null;
        }
        int at = VideoUtil.selectAudioTrack(mediaExtractor);
        VideoCommand cmd = new VideoCommand();
        cmd.append("ffmpeg").append("-y").append("-i").append(inputVideo);
        if (at == -1) {
            int vt = VideoUtil.selectVideoTrack(mediaExtractor);
            float duration = (float) mediaExtractor.getTrackFormat(vt).getLong(MediaFormat.KEY_DURATION) / 1000 / 1000;
            cmd.append("-ss").append("0").append("-t").append(duration).append("-i").append(inputMusic).append("-acodec").append("copy").append("-vcodec").append("copy");
        } else {
            cmd.append("-i").append(inputMusic).append("-filter_complex")
                    .append("[0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + videoVolume + "[a0];[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + audioVolume + "[a1];[a0][a1]amix=inputs=2:duration=first[aout]")
                    .append("-map").append("[aout]").append("-ac").append("2").append("-c:v")
                    .append("copy").append("-map").append("0:v:0");
        }
        cmd.append(output);
        mediaExtractor.release();
        return cmd;
    }

    public static VideoCommand mergeVideoAudio(String inputVideo, String inputMusic, String output) {
        VideoCommand cmd = new VideoCommand();
//        cmd.append("ffmpeg").append("-i").append(inputVideo).append("-i").append(inputMusic).
//                append("-c").append("copy").append("-threads").append("5").append(output);
//        "ffmpeg -i video.mp4 -i audio.aac -map 0:0 -map 1:0 -vcodec copy -acodec copy newvideo.mp4";
//        cmd.append("ffmpeg").append("-i").append(inputVideo).append("-i").append(inputMusic).append("-map").append("0:0")
//                .append("-map").append("1:0").append("-vcodec").append("copy").append("-acodec").append("copy").append(output);
        cmd.append("ffmpeg").append("-y").append("-i").append(inputMusic);
        float duration = VideoUtil.getDuration(inputMusic) / 1000000f;
        cmd.append("-ss").append("0").append("-t").append(duration).append("-i").
                append(inputVideo).
                append("-acodec").
                append("copy").
                append("-vcodec").
                append("copy");
        cmd.append(output);
        return cmd;
    }


    public static VideoCommand changeSpeed(String input, String output, double times) {
        if (times < 0.25f || times > 4.0f) {
            return null;
        }
        VideoCommand command = new VideoCommand();
        command.append("ffmpeg").append("-y").append("-i").append(input);
        String t = "atempo=" + times;
        if (times < 0.5f) {
            t = "atempo=0.5,atempo=" + (times / 0.5f);
        } else if (times > 2.0f) {
            t = "atempo=2.0,atempo=" + (times / 2.0f);
        }
        command.append("-filter_complex").append("[0:v]setpts=" + (1 / times) + "*PTS[v];[0:a]" + t + "[a]")
                .append("-map").append("[v]").append("-map").append("[a]");
        command.append("-threads").append("5");
        command.append("-preset").append("superfast").append(output);
        return command;
    }

    public static VideoCommand changePTS(String input, String output, double times) {
        if (times < 0.25f || times > 4.0f) {
            Log.e("ffmpeg", "times can only be 0.25 to 4");
            return null;
        }
        VideoCommand command = new VideoCommand();
        command.append("ffmpeg").append("-y").append("-i").append(input);
        String t = "atempo=" + times;
        if (times < 0.5f) {
            t = "atempo=0.5,atempo=" + (times / 0.5f);
        } else if (times > 2.0f) {
            t = "atempo=2.0,atempo=" + (times / 2.0f);
        }
        command.append("-filter:a").append(t).append("-threads").append("5");
        command.append("-preset").append("superfast").append(output);
        return command;
    }
}
