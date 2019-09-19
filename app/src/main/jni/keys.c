#include <jni.h>
#include <string.h>

JNIEXPORT jstring JNICALL
Java_com_bink_wallet_MainActivity_getNativeKey(JNIEnv *env, jobject instance, jstring jstring1) {
    const char *nativeString = (*env)->GetStringUTFChars(env, jstring1, 0);
    char *response = "UUZVa0tHblc4U0VDWXdLUlZXcXJRS2xhb2dOYWtLNElxRXVuMDlHb0ZSQmxoeWltc3c";
    if (strstr(nativeString, "staging")) {
        response = "dlhrQWY2S0ZUVWNuUkJ0RFJ1cWRYYWRhSHA1Rk9QZFU1Z25XMlZiTkh1WDVONnNOTGU";
    }
    return (*env)->  NewStringUTF(env, response);
}
