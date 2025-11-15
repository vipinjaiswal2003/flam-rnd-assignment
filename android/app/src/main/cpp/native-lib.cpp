#include <jni.h>
#include <android/log.h>
#include "opencv_processing.hpp"

#define LOG_TAG "native-lib"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_flam_edgeviewer_NativeBridge_processFrameRgba(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray data,
        jint width,
        jint height) {

    jboolean isCopy = JNI_FALSE;
    jbyte *inputBytes = env->GetByteArrayElements(data, &isCopy);
    if (!inputBytes) {
        LOGE("Failed to get input bytes");
        return nullptr;
    }

    std::vector<signed char> output;

    try {
        process_rgba_frame(
                reinterpret_cast<unsigned char *>(inputBytes),
                static_cast<int>(width),
                static_cast<int>(height),
                output);
    } catch (const std::exception &e) {
        LOGE("Exception in process_rgba_frame: %s", e.what());
        env->ReleaseByteArrayElements(data, inputBytes, JNI_ABORT);
        return nullptr;
    }

    env->ReleaseByteArrayElements(data, inputBytes, JNI_ABORT);

    jbyteArray result = env->NewByteArray(static_cast<jsize>(output.size()));
    if (!result) {
        LOGE("Failed to allocate result array");
        return nullptr;
    }

    env->SetByteArrayRegion(result, 0, static_cast<jsize>(output.size()), output.data());
    return result;
}
