package com.netease.ffmpeg;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.xue.douyin.common.codec.MediaData;
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
     * 合并多个视频
     */
    public static VideoCommand mergeMusic(List<MediaData> videos, String output) {
        //检测是否有无音轨视频
        VideoCommand cmd = new VideoCommand();
        cmd.append("ffmpeg");
        cmd.append("-y");
        //添加输入标示
        for (MediaData e : videos) {
            cmd.append("-i").append(e.getFilePath());
        }
        //添加滤镜标识
        cmd.append("-filter_complex");
        StringBuilder filter_complex = new StringBuilder();
        for (int i = 0; i < videos.size(); i++) {
            filter_complex.append("[").append(i).append(":a]");
        }
        filter_complex.append("concat=n=").append(videos.size()).append(":v=0:a=1[outa]");

        if (!filter_complex.toString().equals("")) {
            cmd.append(filter_complex.toString());
        }
        cmd.append("-map").append("[outa]");
        cmd.append("-preset").append("superfast").append(output);
        return cmd;
    }


    /**
     * 无损合并多个视频
     * <p>
     * 注意：此方法要求视频格式非常严格，需要合并的视频必须分辨率相同，帧率和码率也得相同
     */
    public static VideoCommand mergeVideo(List<MediaData> videos, String output) {
        String appDir = StorageUtil.getExternalStoragePath() + File.separator;
        String fileName = "ffmpeg_concat.txt";
        List<String> list = new ArrayList<>();
        for (MediaData e : videos) {
            list.add(e.getFilePath());
        }
        FileUtils.writeTxtToFile(list, appDir, fileName);
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
        cmd.append("ffmpeg").append("-i").append(inputVideo).append("-i").append(inputMusic).
                append("-c").append("copy").append("-threads").append("5").append(output);
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
                .append("-map").append("[v]").append("-map").append("[a]").append("-threads").append("2");
        command.append("-preset").append("superfast").append(output);
        return command;
    }

    public static VideoCommand changePTS(String input, String output, float times) {
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
