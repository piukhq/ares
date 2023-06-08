# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

-dontwarn android.support.**
-keep class * extends androidx.support.v4.app.Fragment{}
-keep class * extends android.support.v4.app.Fragment{}
-keep class  androidx.navigation.fragment.NavHostFragment.** { *; }
-keep class com.bink.wallet.model.** { *; }
-keep class com.bink.wallet.modal.** { *; }
-keep class com.bink.wallet.utils.** { *; }
-keep class com.bink.wallet.utils.enums.** { *; }
-keep public class androidx.support.v7.widget.** { *; }
-keep public class android.support.v7.widget.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Having minify enabled seems to remove some methods from crypto lib causing a crash on version 1.1.0-alpha01
-keep class com.google.crypto.** { *; }

-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type