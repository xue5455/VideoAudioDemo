package com.xue.douyin.common.codec;

import android.media.MediaCodec;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by 薛贤俊 on 2018/3/9.
 */

public abstract class BaseEncoder<T, K> {

    protected static final int TIME_OUT = 10000;

    protected static final String TAG = "encoder";

    protected static final int MSG_START = 1;

    protected static final int MSG_STOP = 2;

    protected static final int MSG_ON_NEW_FRAME = 3;

    protected static final int MSG_QUIT = 4;

    protected boolean mStarted = false;

    protected EncoderHandler mHandler;

    protected MediaCodec mCodec;

    protected MovieMuxCore mMuxCore;

    public BaseEncoder(MovieMuxCore core, T config) throws IOException {
        HandlerThread thread = new HandlerThread(getClass().getSimpleName());
        thread.start();
        mHandler = new EncoderHandler(thread.getLooper(), this);
        mMuxCore = core;
        initCodec(config);
    }

    protected abstract void initCodec(T config) throws IOException;


    public void start() {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_START));
    }

    public void stop() {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP));
        mHandler = null;
    }

    public void newFrame(K obj) {
        if (mHandler == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_ON_NEW_FRAME, obj));
    }

    /**
     * 当调用stop时，需要释放资源
     */
    protected abstract void release();

    protected abstract void onStart();

    protected abstract void onStop();

    protected void onFrameAvailable(K obj) {

    }

    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    protected long getPTSUs() {
        return System.nanoTime() / 1000L;
    }

    protected static class EncoderHandler extends Handler {
        private WeakReference<BaseEncoder> ref;

        protected EncoderHandler(Looper looper, BaseEncoder target) {
            super(looper);
            this.ref = new WeakReference<>(target);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Object obj = msg.obj;
            BaseEncoder target = ref.get();
            if (target == null) {
                getLooper().quitSafely();
                return;
            }
            switch (what) {
                case MSG_START:
                    target.mStarted = true;
                    target.onStart();
                    break;
                case MSG_STOP:
                    target.onStop();
                    target.mStarted = false;
                    target.release();
                    getLooper().quitSafely();
                    break;
                case MSG_ON_NEW_FRAME:
                    if (!target.mStarted) {
                        break;
                    }
                    target.onFrameAvailable(obj);
                    break;
            }
        }
    }
}
