#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "ImageProcessing"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_imagprocessor_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_imagprocessor_MainActivity_processImage(JNIEnv *env, jobject obj, jbyteArray imageData, jint width, jint height) {
    // Get the byte array data
    jbyte *data = env->GetByteArrayElements(imageData, nullptr);
    if (data == nullptr) {
        LOGD("Error retrieving byte array.");
        return nullptr;
    }
    // Create a new byte array for the processed image
    jbyteArray processedImage = env->NewByteArray(env->GetArrayLength(imageData));
    jbyte *processedData = env->GetByteArrayElements(processedImage, nullptr);

    if (processedData == nullptr) {
        LOGD("Error creating processed byte array.");
        env->ReleaseByteArrayElements(imageData, data, 0);
        return nullptr;
    }

    // Convert jbyte array to a usable format for processing (e.g., grayscale)
    int imageSize = width * height * 4;  // Assuming RGBA format
    // Sample processing (inverting colors)
    for (int i = 0; i < imageSize; i += 4) {
        processedData[i] = 255 - data[i];       // Red
        processedData[i + 1] = 255 - data[i + 1]; // Green
        processedData[i + 2] = 255 - data[i + 2]; // Blue
        processedData[i + 3] = data[i + 3]; // Alpha
    }

    // Release the original byte array
    env->ReleaseByteArrayElements(imageData, data, 0);

    // Commit changes and return the processed image
    env->SetByteArrayRegion(processedImage, 0, imageSize, processedData);
    env->ReleaseByteArrayElements(processedImage, processedData, 0);

    return processedImage;
}