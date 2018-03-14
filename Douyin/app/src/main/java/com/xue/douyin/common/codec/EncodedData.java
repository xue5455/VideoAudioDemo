package com.xue.douyin.common.codec;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public class EncodedData {
    private ByteBuffer mData;

    private MediaCodec.BufferInfo mInfo;

    public EncodedData(ByteBuffer data, MediaCodec.BufferInfo info) {
        this.mData = ByteBuffer.allocate(data.capacity());
        this.mData.put(data);
        this.mInfo = new MediaCodec.BufferInfo();
        this.mInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags);
    }

    public ByteBuffer getData() {
        return mData;
    }

    public MediaCodec.BufferInfo getInfo() {
        return mInfo;
    }
}
