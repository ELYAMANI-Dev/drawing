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

# Facebook Audience Network references optional Infer annotations (not on classpath at runtime)
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe

# Room - keep generated _Impl classes so R8 doesn't remove them
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** INSTANCE;
    public static ** Companion;
}
-keep class **_Impl { *; }
-keep class **_Impl$* { *; }

# WorkManager - required for androidx.startup initialization
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keepnames class androidx.work.impl.WorkDatabase
-keep class androidx.work.impl.WorkDatabase_Impl { *; }

# androidx.startup
-keep class androidx.startup.** { *; }
-keep class * implements androidx.startup.Initializer { *; }

# OkHttp (remote ad config)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Coil image loading
-keep class coil.** { *; }

# Mixpanel
-keep class com.mixpanel.** { *; }

# AppLovin / MAX
-keep class com.applovin.** { *; }

# Meta Audience Network
-keep class com.facebook.ads.** { *; }

# OneSignal
-keep class com.onesignal.** { *; }