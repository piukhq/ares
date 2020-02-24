#include "spreedly-lib.h"
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_SplashFragment_spreedlyKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "1Lf7DiKgkcx5Anw7QxWdDxaKtTa";
    return env->NewStringUTF(key.c_str());
}