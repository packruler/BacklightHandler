#include <jni.h>
#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <android/bitmap.h>
#include <string.h>
#include <unistd.h>

#define  LOG_TAG    "DEBUG"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT void JNICALL
Java_com_packruler_backlighthandler_Processing_ImageProcessing_processNative(JNIEnv *env,
                                                                             jobject instance,
                                                                             jobject bitmap,
                                                                             jintArray horizontalLED_,
                                                                             jintArray verticalLED_,
                                                                             jint xDepth,
                                                                             jint yDepth) {
    jint *horizontalLED = (*env)->GetIntArrayElements(env, horizontalLED_, NULL);
    jint *verticalLED = (*env)->GetIntArrayElements(env, verticalLED_, NULL);

    AndroidBitmapInfo *info;
    void **pixels;

    AndroidBitmap_getInfo(env, bitmap, info);
    AndroidBitmap_lockPixels(env, bitmap, pixels);
    // TODO

    (*env)->ReleaseIntArrayElements(env, horizontalLED_, horizontalLED, 0);
    (*env)->ReleaseIntArrayElements(env, verticalLED_, verticalLED, 0);
}