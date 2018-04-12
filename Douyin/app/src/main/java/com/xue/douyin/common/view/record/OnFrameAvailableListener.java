package com.xue.douyin.common.view.record;

import com.xue.douyin.common.recorder.video.VideoFrameData;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public interface OnFrameAvailableListener {
    void onFrameAvailable(VideoFrameData frameData);
}
