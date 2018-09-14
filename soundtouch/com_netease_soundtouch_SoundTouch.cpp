////////////////////////////////////////////////////////////////////////////////
///
/// Example Interface class for SoundTouch native compilation
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// WWW           : http://www.surina.net
///
////////////////////////////////////////////////////////////////////////////////
//
// $Id: soundtouch-jni.cpp 212 2015-05-15 10:22:36Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////

#include "include/SoundTouch.h"
#include "com_netease_soundtouch_SoundTouch.h"
#include <string>
#include <android/log.h>
#define BUFF_SIZE 4096

using namespace soundtouch;

#define LOGD(...)   __android_log_print((int)ANDROID_LOG_DEBUG, "xue", __VA_ARGS__)

jlong Java_com_netease_soundtouch_SoundTouch_newInstance(JNIEnv *env, jclass jClass)
{
    SoundTouch *soundTouch = new SoundTouch();
    soundTouch->setSampleRate(44100);
    soundTouch->setChannels(1);
    return (jlong)(soundTouch);
}

void Java_com_netease_soundtouch_SoundTouch_deleteInstance(JNIEnv *env, jobject thiz, jlong handle)
{
    SoundTouch *ptr = (SoundTouch *)handle;
    delete ptr;
}

void Java_com_netease_soundtouch_SoundTouch_setTempo(JNIEnv *env, jobject thiz, jlong handle, jfloat tempo)
{
    SoundTouch *ptr = (SoundTouch *)handle;
    ptr->setTempo(tempo);
}

void Java_com_netease_soundtouch_SoundTouch_setPitchSemiTones(JNIEnv *env, jobject thiz, jlong handle, jfloat pitch)
{
    SoundTouch *ptr = (SoundTouch *)handle;
    ptr->setPitchSemiTones(pitch);
}

jint Java_com_netease_soundtouch_SoundTouch_getBytes(JNIEnv *env, jobject thiz, jlong handle, jbyteArray output, jint length)
{
    int receiveSamples = 0;
    int maxReceiveSamples = length/2;

    SoundTouch *soundTouch = (SoundTouch *)handle;

    jbyte *data;

    data = env->GetByteArrayElements(output, JNI_FALSE);

    receiveSamples = soundTouch->receiveSamples((SAMPLETYPE *)data,
                                                maxReceiveSamples);
    LOGD("receiveSamples %d",receiveSamples);

    //memcpy(data, sampleBuffer, receiveSize);

    env->ReleaseByteArrayElements(output, data, 0);

    return receiveSamples;
}

void Java_com_netease_soundtouch_SoundTouch_putBytes(JNIEnv *env, jobject thiz, jlong handle, jbyteArray input, jint offset, jint length)
{
    SoundTouch *soundTouch = (SoundTouch *)handle;

    jbyte *data;

    data = env->GetByteArrayElements(input, JNI_FALSE);

    //soundtouch::SAMPLETYPE sampleBuffer[length];
    //memcpy(&sampleBuffer, data, length);

    //(16*1)/8=2bytes,length/2=x;x��sample

    soundTouch->putSamples((SAMPLETYPE *)data, length/2);

    env->ReleaseByteArrayElements(input, data, 0);
}


void Java_com_netease_soundtouch_SoundTouch_flush
  (JNIEnv *env, jobject thiz, jlong handle){
      SoundTouch* soundTouch = (SoundTouch*)handle;
      soundTouch->flush();
  }