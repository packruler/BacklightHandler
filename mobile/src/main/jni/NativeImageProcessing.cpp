//
// Created by packr on 12/8/2015.
//

#import <jni.h>
#import <android/bitmap.h>

jintArray processImage(JNIEnv *env, jobject jbitmap, AndroidBitmapInfo *info) {
    if (NULL == env || NULL == jbitmap) {
        return NULL;
    }
}

