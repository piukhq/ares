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

// PAYMENT HASHING SECRETS

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_SplashFragment_paymentCardHashingDevKey(
        JNIEnv *env,
jobject /* this */) {
std::string key = "Uf5AIu6ehfHM2By6GAsIDsclEYFlWQNt";
return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_SplashFragment_paymentCardHashingStagingKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "nwQ20oYliVN9EFRmvtOAtp12uJAw8ptT";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_SplashFragment_paymentCardHashingProdKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "octXGgMMZC02QDajJPiXDC2Q74DJX1g0";
    return env->NewStringUTF(key.c_str());
}