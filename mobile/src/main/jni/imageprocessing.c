#include <jni.h>

JNIEXPORT jintArray JNICALL
Java_com_packruler_backlighthandler_Processing_ImageProcessing_processNative(JNIEnv *env,
                                                                             jobject instance,
                                                                             jintArray buffer_) {
    jint *buffer = (*env)->GetIntArrayElements(env, buffer_, NULL);

    // TODO

    (*env)->ReleaseIntArrayElements(env, buffer_, buffer, 0);
}