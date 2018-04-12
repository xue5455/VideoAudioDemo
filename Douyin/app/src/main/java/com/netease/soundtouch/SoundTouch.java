package com.netease.soundtouch;

/**
 * Created by 薛贤俊 on 2018/4/8.
 */

public class SoundTouch {
    private native final void setTempo(long handle, float tempo);

    private native final void setPitchSemiTones(long handle, float pitch);

    private native final void putBytes(long handle, byte[] input, int offset, int length);

    private native final int getBytes(long handle, byte[] output, int length);

    private native final static long newInstance();

    private native final void deleteInstance(long handle);

    private native final void flush(long handle);

    private long handle = 0;

    public SoundTouch() {
        handle = newInstance();
    }

    public void putBytes(byte[] input) {
        this.putBytes(handle, input, 0, input.length);
    }

    public int getBytes(byte[] output) {
        return this.getBytes(handle, output, output.length);
    }


    public void close() {
        deleteInstance(handle);
        handle = 0;
    }

    public void flush() {
        this.flush(handle);
    }

    public void setTempo(float tempo) {
        setTempo(handle, tempo);
    }


    public void setPitchSemiTones(float pitch) {
        setPitchSemiTones(handle, pitch);
    }

    static {
        System.loadLibrary("soundtouch");
    }

}
