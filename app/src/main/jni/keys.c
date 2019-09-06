#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_bink_wallet_MainActivity_getNativeKey(JNIEnv *env, jobject instance) {

 return (*env)->  NewStringUTF(env, "dlhrQWY2S0ZUVWNuUkJ0RFJ1cWRYYWRhSHA1Rk9QZFU1Z25XMlZiTkh1WDVONnNOTGU=");
}