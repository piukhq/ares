#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_bink_wallet_MainActivity_getNativeKey(JNIEnv *env, jobject instance) {

 return (*env)->  NewStringUTF(env, "UUZVa0tHblc4U0VDWXdLUlZXcXJRS2xhb2dOYWtLNElxRXVuMDlHb0ZSQmxoeWltc3c");
}