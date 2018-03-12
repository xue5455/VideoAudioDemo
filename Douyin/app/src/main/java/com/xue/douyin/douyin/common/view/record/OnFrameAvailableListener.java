package com.xue.douyin.douyin.common.view.record;

import com.xue.douyin.douyin.common.codec.VideoFrameData;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public interface OnFrameAvailableListener {
    void onFrameAvailable(VideoFrameData frameData);
}
