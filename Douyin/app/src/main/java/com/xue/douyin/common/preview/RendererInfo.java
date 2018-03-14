package com.xue.douyin.common.preview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by 薛贤俊 on 2018/3/7.
 */

public class RendererInfo {

    private static final float FULL_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f,   // 3 top right
    };
    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    private FloatBuffer mVertexBuffer;

    private FloatBuffer mTexBuffer;

    public RendererInfo() {
        mVertexBuffer = ByteBuffer.allocateDirect(FULL_RECTANGLE_COORDS.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.position(0);
        mVertexBuffer.put(FULL_RECTANGLE_COORDS);

        mTexBuffer = ByteBuffer.allocateDirect(FULL_RECTANGLE_TEX_COORDS.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexBuffer.position(0);
        mTexBuffer.put(FULL_RECTANGLE_TEX_COORDS);
    }

    public FloatBuffer getVertexBuffer() {
        return mVertexBuffer;
    }

    public FloatBuffer getTextureBuffer() {
        return mTexBuffer;
    }
}
