package com.xue.douyin.common.recorder;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public class MediaFrame {
    private ByteBuffer frame;
    private MediaCodec.BufferInfo info;

    public MediaFrame(ByteBuffer frame, MediaCodec.BufferInfo info) {
        this.frame = frame.duplicate();
        this.frame.position(info.offset);
        this.frame.limit(info.offset + info.size);
        this.info = new MediaCodec.BufferInfo();
        this.info.size = info.size;
        this.info.presentationTimeUs = info.presentationTimeUs;
        this.info.flags = info.flags;
        this.info.offset = info.offset;
    }

    public ByteBuffer getFrame() {
        return frame;
    }

    public MediaCodec.BufferInfo getInfo() {
        return info;
    }
}
