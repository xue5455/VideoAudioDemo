package com.xue.douyin.common.player;

/**
 * Created by 薛贤俊 on 2018/4/4.
 */

public interface MediaClock {
    /**
     * 返回当前的时间戳 微秒
     *
     * @return
     */
    long getPositionUs();

    /**
     * 设置播放参数,并且返回即将生效的参数
     *
     * @param playbackParameters
     * @return
     */
    PlaybackParameters setPlaybackParameters(PlaybackParameters playbackParameters);

    /**
     * 返回当前生效的播放参数
     *
     * @return
     */
    PlaybackParameters getPlaybackParameters();

}
