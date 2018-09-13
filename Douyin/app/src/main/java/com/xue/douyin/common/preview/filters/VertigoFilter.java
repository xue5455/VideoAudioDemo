package com.xue.douyin.common.preview.filters;


import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.xue.douyin.R;

import com.xue.douyin.application.AppProfile;
import com.xue.douyin.common.preview.GLUtils;
import com.xue.douyin.common.preview.RenderBuffer;
import com.xue.douyin.common.util.FileUtils;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE10;
import static android.opengl.GLES20.GL_TEXTURE8;
import static android.opengl.GLES20.GL_TEXTURE9;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by 薛贤俊 on 2018/9/7.
 * 幻觉
 */
public class VertigoFilter extends ImageFilter {

    private RenderBuffer mRenderBuffer;

    private RenderBuffer mRenderBuffer2;

    private RenderBuffer mRenderBuffer3;

    private int mLutTexture;
    //当前帧
    private int mCurrentFrameProgram;
    //上一帧
    private int mLastFrameProgram;

    private boolean mFirst = true;

    @Override
    public void draw(int textureId, float[] texMatrix, int canvasWidth, int canvasHeight) {
        if (mRenderBuffer == null) {
            mRenderBuffer = new RenderBuffer(GL_TEXTURE8, canvasWidth, canvasHeight);
            mRenderBuffer2 = new RenderBuffer(GL_TEXTURE9, canvasWidth, canvasHeight);
            mRenderBuffer3 = new RenderBuffer(GL_TEXTURE10, canvasWidth, canvasHeight);
            mLastFrameProgram = GLUtils.buildProgram(FileUtils.readFromRaw(R.raw.vertex_common), FileUtils.readFromRaw(R.raw.fragment_common));
            mCurrentFrameProgram = GLUtils.buildProgram(FileUtils.readFromRaw(R.raw.vertex_common), FileUtils.readFromRaw(R.raw.fragment_current_frame));
            mLutTexture = GLUtils.genLutTexture();
            android.opengl.GLUtils.texImage2D(GL_TEXTURE_2D, 0, BitmapFactory.decodeResource(AppProfile.getContext().getResources(), R.raw.lookup_vertigo), 0);
        }
        mRenderBuffer.bind();
        super.draw(textureId, texMatrix, canvasWidth, canvasHeight);
        mRenderBuffer.unbind();
        //绘制当前帧
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawCurrentFrame();
        mRenderBuffer3.bind();
        drawCurrentFrame();
        mRenderBuffer3.unbind();
        //只用两个buffer的话，屏幕中会有黑格子
        //把3中的内容画到2中
        mRenderBuffer2.bind();
        drawToBuffer();
        mRenderBuffer2.unbind();
        mFirst = false;
    }

    private void drawToBuffer() {
        glUseProgram(mLastFrameProgram);
        setup(mLastFrameProgram, new int[]{mRenderBuffer3.getTextureId()});
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }

    private void drawCurrentFrame() {
        glUseProgram(mCurrentFrameProgram);
        int textureId = mRenderBuffer.getTextureId();
        setup(mCurrentFrameProgram, new int[]{textureId, mFirst ? textureId : mRenderBuffer2.getTextureId(), mLutTexture});
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private void setup(int programId, int[] textureId) {
        glUseProgram(programId);
        int aPositionLocation = glGetAttribLocation(programId, "aPosition");
        int aTexCoordLocation = glGetAttribLocation(programId, "aTextureCoord");
        mRendererInfo.getVertexBuffer().position(0);
        glEnableVertexAttribArray(aPositionLocation);
        glVertexAttribPointer(aPositionLocation, 2,
                GL_FLOAT, false, 0, mRendererInfo.getVertexBuffer());
        mRendererInfo.getTextureBuffer().position(0);
        glEnableVertexAttribArray(aTexCoordLocation);
        glVertexAttribPointer(aTexCoordLocation, 2,
                GL_FLOAT, false, 0, mRendererInfo.getTextureBuffer());
        for (int i = 0; i < textureId.length; i++) {
            int textureLocation = glGetUniformLocation(programId, "uTexture" + i);
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GLES20.GL_TEXTURE_2D, textureId[i]);
            glUniform1i(textureLocation, i);
        }
    }


}
