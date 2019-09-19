#include <jni.h>
#include <string.h>

JNIEXPORT jstring JNICALL
Java_com_bink_wallet_MainActivity_getNativeKey(JNIEnv *env, jobject instance, jstring jstring1) {
    const char *nativeString1 = (*env)->GetStringUTFChars(jstring1, 0, NULL);
    if (strstr(nativeString1, "staging")) {
        return (*env)->NewStringUTF(env,
                                    "dlhrQWY2S0ZUVWNuUkJ0RFJ1cWRYYWRhSHA1Rk9QZFU1Z25XMlZiTkh1WDVONnNOTGU");
    }
    return (*env)->  NewStringUTF(env, "UUZVa0tHblc4U0VDWXdLUlZXcXJRS2xhb2dOYWtLNElxRXVuMDlHb0ZSQmxoeWltc3c");
}