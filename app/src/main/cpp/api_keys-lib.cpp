#include "api_keys-lib.h"
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_spreedlyKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "1Lf7DiKgkcx5Anw7QxWdDxaKtTa";
    return env->NewStringUTF(key.c_str());
}

// PAYMENT HASHING SECRETS

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_paymentCardHashingDevKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "Uf5AIu6ehfHM2By6GAsIDsclEYFlWQNt";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_paymentCardHashingStagingKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "nwQ20oYliVN9EFRmvtOAtp12uJAw8ptT";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_paymentCardHashingProdKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "octXGgMMZC02QDajJPiXDC2Q74DJX1g0";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_paymentCardEncryptionPublicKeyDev(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAkuwzWAdw2+t7gAy+ciSQ\n"
                      "        IOtqvEg3C23cWnjHZpe465ZUJmnyW5B5HBJVLd+A0fpVlujkwowwvZ4GjAP8J0hY\n"
                      "0wYi8yCvGGD8CJ2XPqyfv/w+kOr/AnpWOajFMe0tA65Q2xs77N2JrwPAv/Cyr8Iu\n"
                      "        QK/6B4QjB21nf6RkoxR8uFm7AQbxFa2EpWpBswbU0J2hXWiYSLJOcSAxs0/8e6SQ\n"
                      "1Kz50nZe1GcQebOx1DD6F2XtIy4Z/klJ8X2OeJrOblTjFnOYw0F+NHk0FTChTA4U\n"
                      "        yypM5tZ3jxSQVEPxTyqXYF00mHJP1WsPGiYIpRfgbnUDkZwpM2+4+hIc5Q4OK8wH\n"
                      "        N6dacU0dQqBcao9BZwWgQdwUSVUTwYYuwLjXV0ApfOrU1fXDnoG3ZJcP1jTqxygT\n"
                      "Nwuwn28sRujfWrOAcdukYMb3S4IpCvRupeJExh8Tuhwej10gIPaO+MoJcLqYns7F\n"
                      "        aYjJtkzORjcyr4sBhNiektkQP76qUMn9aluJt0cbqXuREiL9cSahHACY0f3StoDg\n"
                      "BrEprwvlkk+9E65rfOsBTiZ2chbHYlTYAzqVtSg8pcc5Dh9/xeFm7D75upWlpspZ\n"
                      "        C7RpZ2pBDx3vKnJKGg7qFlgdo58qvQJ0UQXOB9Qtn4pNR8U73t/uSThkPteqAUmg\n"
                      "        qXak680pVcC+wOsH4nQ3YtMCAwEAAQ==";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_paymentCardEncryptionPublicKeyStaging(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAl67uxg9puXz6QT57aXL3\n"
                      "+bpEg3H4nU4OQYSrmOwX8BCUJ5JmVcSztqGwTYONJSthZnRrIT5XPHzQH3ANDwTi\n"
                      "ka+TxqRcXMNUw/CKUCNbkryeY0z31MImC4zCWCpwxe88+DkhnOSIUMB6X8YyylZ1\n"
                      "Pzw7DqHOXJPaV5aTfInpxatW30fURFniXWnsjlafievaY5qmWR/DUV/1a50gn6b4\n"
                      "pHkSCu8SKdO+DlKmf27rGgNaBrmxDMJ3kOlMrW/eZgEP5U2XjdbdwFJMMnm80SVK\n"
                      "OaX2tBwDJK0RkxnWuWCYHxTIDtYpgt2dR7/2WKsU56OaGGKFZHlKeatUEftNhUAq\n"
                      "f4vbL3Fj9ZlhU5zy7QkbmWFr9cPSh0vu0u+C87ZmSUpDcFB/itzzwuKOjrpt9a2W\n"
                      "88OqzMKl5YJSAvpxr2zJ816W0waIfSuWPb6vnW/CRUit1psgflZc/H1rS9hBzgFU\n"
                      "PFIUEH+q/gt7kSOXhUdyFkkHhHjSY9UrpG495j1SXcQ1jEzHZhnBDwtd40ae/Gp9\n"
                      "WuA2bSd32Wjm0p8dSUlD6LlxiqtYtKYHPB/W0+5KG05b4rVslBTJmupKVCzY0Nuy\n"
                      "jIDD99VqKwUJgZbWbFjEW80P2UKk9gQyH3BxnkOeAokC8c7vgbYvtVR0vvYs2pel\n"
                      "8cEhHqwz7vqwGDt8jWBeNicCAwEAAQ==";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_paymentCardEncryptionPublicKeyProd(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA0mImTn4ZbSXIZ6KFRz52\n"
                      "A0AT7eEmXFYZiYEYEUVCZWnmtvqcOFVTGfq3YwDSHHiD0vLAutsuNa6sVZnaU9RK\n"
                      "YKEcgrb28ZGVfuRKqaOqw4+6axBZb4A5Yo5XsQ0Q/hWSaGmC4ssaHehwWzQHMImE\n"
                      "mPzUAvhekDLsxcktukwkeDnHYh6QH5rchiVRdFRcffujStjjaz1BZP98jtqKbFMi\n"
                      "uOH2IluvQgf1gguNy22MySOOYJz46VL/K2BjnGTebtAkzRDJbInpt8sQTWLPHNt6\n"
                      "2rnCEVOy2c5nQ83ElzBhSJsEP5K7xo1I9Ofox3P8YDDPKQ1cUy8cD0K94aZrOKIy\n"
                      "v9dY0QLbwAs8sYDpA7iA0PSakiBzmlQez8WTtOpj7MFJA9P5unwdckIP9dBnosWn\n"
                      "tn77oz/ZrVRYHPr5T73jy3qV74r14C144Ka0UjTaN/WYQ+ONdo4MJjHAnGS7syUl\n"
                      "0qYYqjbWWw/kOu+LRzivko9+SGGEKv+vvDcZMtA+4NoMyMEB4EnZPl/YyX2FkX2M\n"
                      "PrAI94jgu7uI6iV8XgE6b9KxVzJPk99lX8hajlCasrZCGe+EU/BNwLJJCwG/w6Cu\n"
                      "gL8lknAdiTK2v1GXfSFqXA6Rd1VMl+iRyShanCHamYSkxizcVtFaQbHfbNLnegGq\n"
                      "6QZCyghMp1YSYf4gfxdM0SsCAwEAAQ==";
    return env->NewStringUTF(key.c_str());
}

// ZENDESK SANDBOX KEYS

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_zendeskSandboxUrl(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "https://binkcx1573467900.zendesk.com";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_zendeskSandboxAppId(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "9604542ca7d21d05137fbc8cb56366bedba80fb52daaa318";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_zendeskSandboxOAuthId(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "mobile_sdk_client_13ad51f66e08c5466b42";
    return env->NewStringUTF(key.c_str());
}

// ZENDESK PRODUCTION KEYS

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_zendeskProdUrl(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "https://binkcx.zendesk.com";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_zendeskProdAppId(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "52fd669e427c2f4710fc53d980d928b0da009da8f6942b82";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_zendeskProdOAuthId(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "mobile_sdk_client_4e5138822a3a80bb44fd";
    return env->NewStringUTF(key.c_str());
}

// BOUNCER KEYS

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_bouncerDevKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "H0lUg0d0dtg9whJr5JN_oz1WgzaKHDGE";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_scenes_splash_SplashFragment_bouncerProdKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string key = "cWSWCwxWzjaMCnSdGfgU9syEqRQeGP7E";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_utils_Keys_mixPanelBetaApiKey(JNIEnv *env, jobject ){
    std::string key = "20c9540b354f828aebee0dd478432187";
    return env ->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_utils_Keys_mixPanelProductionApiKey(JNIEnv *env, jobject ){
    std::string key = "b900cc7644c628aaef87bf6475d20d6c";
    return env ->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bink_wallet_utils_Keys_binkTestAuthToken(JNIEnv *env,jobject ){
    std::string key = "e66cd653a8a1a4ee49ef7b4f2f44517e01e4e513c0c0ad4cc0818696847f98be";
    return env ->NewStringUTF(key.c_str());
}
