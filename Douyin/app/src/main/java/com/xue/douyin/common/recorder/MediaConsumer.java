package com.xue.douyin.common.recorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import com.xue.douyin.common.recorder.tst.Consumer;
import com.xue.douyin.common.util.LogUtil;
import com.xue.douyin.common.util.ThreadUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/4/10.
 */

public class MediaConsumer implements Consumer {

    private static final String TAG = "MediaConsumer";

    private Handler handler;

    private int audioTrack = -1;

    private int videoTrack = -1;

    private boolean audioStopped = true;

    private boolean videoStopped = true;

    /**
     * 是否支持语音
     */
    private boolean supportAudio;

    private MediaConfig config;

    private MediaMuxer muxer;

    private List<MediaFrame> audioList;

    private List<MediaFrame> videoList;

    private MediaClock audioClock;

    private MediaClock videoClock;

    private String fileName;

    private boolean started = false;

    private Recorder audioRecorder;

    private Recorder videoRecorder;

    private OnRecordFinishListener finishListener;

    private OnRecordProgressListener progressListener;

    public MediaConsumer() {
        handler = ThreadUtil.newHandlerThread(TAG);
    }

    @Override
    public void consume(final int type, ByteBuffer sample, MediaCodec.BufferInfo info) {
        final MediaFrame frame = new MediaFrame(sample, info);
        handler.post(new Runnable() {
            @Override
            public void run() {
                onConsume(type, frame);
            }
        });
    }

    private void onConsume(int type, MediaFrame frame) {
        switch (type) {
            case VIDEO:
                consumeVideo(frame);
                break;
            case AUDIO:
                consumeAudio(frame);
                break;
        }
    }

    private void consumeAudio(MediaFrame frame) {
        if (!isReady() && audioList != null) {
            audioList.add(frame);
            return;
        }
        if (audioList != null) {
            for (MediaFrame f : audioList) {
                writeSample(f, audioClock, audioTrack, audioRecorder);
            }
            audioList.clear();
        }
        audioList = null;
        writeSample(frame, audioClock, audioTrack, audioRecorder);
    }

    private void consumeVideo(MediaFrame frame) {
        if (!isReady() && videoList != null) {
            videoList.add(frame);
            return;
        }
        if (videoList != null) {
            for (MediaFrame f : videoList) {
                writeSample(f, videoClock, videoTrack, videoRecorder);
            }
            videoList.clear();
        }
        videoList = null;
        writeSample(frame, videoClock, videoTrack, videoRecorder);
    }

    private boolean isReady() {
        return (!supportAudio || audioTrack >= 0) && videoTrack >= 0;
    }

    private boolean isStopped() {
        return (!supportAudio || audioStopped) && videoStopped;
    }

    private void writeSample(MediaFrame frame, MediaClock clock, int track, Recorder recorder) {
        if (clock.reachMaxCounts()) {
            recorder.stop();
            return;
        }
        long timeUs = frame.getInfo().presentationTimeUs;
        clock.addSample(timeUs);
        frame.getInfo().presentationTimeUs = clock.getTimeUs();
        muxer.writeSampleData(track, frame.getFrame(), frame.getInfo());

        if (track == videoTrack && progressListener != null) {
            progressListener.onRecordProgress(clock.getDuration());
        }
    }

    @Override
    public void setProducerReady(final int type, final MediaFormat format) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onProducerReady(type, format);
            }
        });
    }

    private void onProducerReady(int type, MediaFormat format) {
        switch (type) {
            case VIDEO:
                videoTrack = muxer.addTrack(format);
                videoStopped = false;
                break;
            case AUDIO:
                audioTrack = muxer.addTrack(format);
                audioStopped = false;
                break;
        }
        if (isReady()) {
            muxer.start();
        }
    }

    @Override
    public void producerStopped(int type) {
        switch (type) {
            case VIDEO:
                videoTrack = -1;
                videoStopped = true;
                break;
            case AUDIO:
                audioTrack = -1;
                audioStopped = true;
                break;
        }
        if (isStopped()) {
            pause();
        }
    }

    @Override
    public void start(final MediaConfig config) {
        if (started) {
            throw new IllegalStateException("Consumer is already started");
        }
        started = true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStart(config);
            }
        });
    }

    private void onStart(MediaConfig config) {
        this.config = config;
        long startTime = 0;
        audioClock = new AudioClock(startTime,
                config.getAudioConfig().getSampleRate(),
                config.getAudioConfig().getSamplePerFrame());
        audioClock.setMaxDuration(config.getMaxDuration());
        videoClock = new VideoClock(MediaConfig.getSpeedFactor(config.getSpeedMode()), startTime);
        videoClock.setMaxDuration(config.getMaxDuration());
        fileName = config.getFilePath();
        supportAudio = config.supportAudio();
        try {
            prepareMuxer();
        } catch (Throwable e) {
            LogUtil.e(e);
        }
        audioList = new ArrayList<>();
        videoList = new ArrayList<>();
    }

    @Override
    public void stop() {
        if (!started) {
            throw new IllegalStateException("Consumer is not started");
        }
        started = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStop();
            }
        });
    }

    @Override
    public void addProducer(Recorder producer) {
        int type = producer.getDataType();
        switch (type) {
            case AUDIO:
                audioRecorder = producer;
                break;
            case VIDEO:
                videoRecorder = producer;
                break;
        }
    }

    @Override
    public void setProgressListener(OnRecordProgressListener listener) {
        this.progressListener = listener;
    }

    private void pause() {
        muxer.stop();
        muxer.release();
        if (finishListener != null) {
//            finishListener.onRecordFinish(new ClipInfo(config.getFilePath(),
//                    videoClock.getDuration(),
//                    config.getSpeedMode()));
        }
        muxer = null;
        fileName = null;
        config = null;
    }

    private void onStop() {
        handler.getLooper().quitSafely();
        handler = null;
    }

    @Override
    public void setFinishListener(OnRecordFinishListener listener) {
        this.finishListener = listener;
    }

    @Override
    public void stopFeeding() {
//        canFeed = false;
    }

    private void prepareMuxer() throws IOException {
        if (muxer != null) {
            return;
        }
        muxer = new MediaMuxer(fileName,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }
}
