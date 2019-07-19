#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_bink_wallet_MainActivity_getNativeKey(JNIEnv *env, jobject instance) {

 return (*env)->  NewStringUTF(env, "QFUkKGnW8SECYwKRVWqrQKlaogNakK4IqEun09GoFRBlhyimsw");
}