package com.xue.douyin.common.player;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.view.Surface;

import com.xue.douyin.common.C;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.ThreadUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC;

/**
 * Created by 薛贤俊 on 2018/4/19.
 */

public class VideoPlayer {
    private String filePath;
    private MediaExtractor extractor;
    private Handler handler;
    private int videoTrack = -1;
    private boolean consumed = false;
    private boolean stopped = true;
    private MediaCodec.BufferInfo bufferInfo;
    private MediaCodec decoder;


    //当前视频播放的时间
    private long timeLine = 0;

    //上一帧图像的时间
    private long lastSampleTime = 0;

    private long duration;

    private OnPlayerProgressListener progressListener;

    public VideoPlayer(String filePath) {
        this.filePath = filePath;
    }

    public void configure(Surface surface) {
        initDecoder(surface);
        initTrack();
        handler = ThreadUtil.newHandlerThread(getClass().getSimpleName());
    }

    public void setOnPlayerProgressListener(OnPlayerProgressListener listener) {
        this.progressListener = listener;
    }

    public long getDuration() {
        return duration;
    }

    public void configure(SurfaceTexture surfaceTexture) {
        configure(new Surface(surfaceTexture));
    }

    public void start() {
        stopped = false;
        batch();
    }

    public void pause() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                timeLine = 0;
                stopped = true;
            }
        });
    }

    public void stop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (stopped) {
                    return;
                }
                stopped = true;
                decoder.stop();
                decoder.release();
                decoder = null;
                extractor.release();
                extractor = null;
                handler.getLooper().quitSafely();
            }
        });
    }

    public void seekTo(final long timeUs) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                extractor.seekTo(timeUs, SEEK_TO_CLOSEST_SYNC);
                timeLine = 0;
                consumed = false;
                consumeFrame(getNextSampleTime(), false);
            }
        });

    }

    public void resume() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                timeLine = 0;
                stopped = false;
                consumed = true;
                batch();
            }
        });

    }


    private long getNextSampleTime() {
        if (consumed) {
            consumed = false;
            extractor.advance();
        }
        return extractor.getSampleTime();
    }

    private void consumeFrame(long timeStamp, boolean shouldUpdateProgress) {
        long t = System.nanoTime() / 1000L;
        long duration = t - timeLine;
        if(timeLine!=0){
            if (lastSampleTime + duration < timeStamp) {
//                batch();
                return;
            }
        }

        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
        int inputBufferIndex = decoder.dequeueInputBuffer(C.BUFFER_TIME_OUT);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = extractor.readSampleData(inputBuffer, 0);
            if (sampleSize <= 0) {
                lastSampleTime = 0;
                timeLine = 0;
                extractor.seekTo(0, SEEK_TO_CLOSEST_SYNC);
                decoder.flush();
//                batch();
                return;
            }
            decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.getSampleTime(), 0);
            consumed = true;
            timeLine = t;
            lastSampleTime = timeStamp;
            while (true) {
                int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, C.BUFFER_TIME_OUT);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
                        || outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER
                        || outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    continue;
                }
                decoder.releaseOutputBuffer(outputBufferIndex, true);
                if (progressListener != null && shouldUpdateProgress) {
                    progressListener.onPlayerProgress(timeStamp);
                }
                break;
            }
        }
    }


    private void batch() {
        if (stopped) {
            timeLine = 0;
            return;
        }
        long timeStamp = getNextSampleTime();

        consumeFrame(timeStamp, true);

        handler.post(new Runnable() {
            @Override
            public void run() {
                batch();
            }
        });
    }


    private void initDecoder(Surface surface) {
        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(filePath);
            initTrack();
            decoder = MediaCodec.createDecoderByType(C.VideoParams.MIME_TYPE);
        } catch (IOException e) {
            return;
        }
        bufferInfo = new MediaCodec.BufferInfo();
        MediaFormat format = extractor.getTrackFormat(videoTrack);
        decoder.configure(format, surface, null, 0);
        extractor.selectTrack(videoTrack);
        decoder.start();
        stopped = false;
    }

    private void initTrack() {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.equals(C.VideoParams.MIME_TYPE)) {
                videoTrack = i;
                duration = format.containsKey(MediaFormat.KEY_DURATION) ? format.getLong(MediaFormat.KEY_DURATION) : 0;//时长
            }
        }
        if (videoTrack == -1) {
            LogUtil.e("initTrack error " +
                    "\nvideoTrack = " + videoTrack);
        }

    }

    public interface OnPlayerProgressListener {
        void onPlayerProgress(long currentTimeUs);
    }
}
