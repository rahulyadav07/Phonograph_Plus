# http://proguard.sourceforge.net/index.html#manual/usage.html
#
# Starting with version 2.2 of the Android plugin for Gradle, this file is distributed together with
# the plugin and unpacked at build-time. The files in $ANDROID_HOME are no longer maintained and
# will be ignored by new version of the Android plugin for Gradle.

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize steps (and performs some
# of these optimizations on its own).
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

#-obfuscationdictionary dict.txt
#-classobfuscationdictionary dict.txt
#-packageobfuscationdictionary dict.txt

# Preserve some attributes that may be required for reflection.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod


# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick.
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Preserve annotated Javascript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# The support libraries contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontnote android.support.**
-dontnote androidx.**
-dontwarn android.support.**
-dontwarn androidx.**


# Understand the @Keep support annotation.
#-keep class android.support.annotation.Keep
-keep class androidx.annotation.Keep

#-keep @android.support.annotation.Keep class * {*;}
-keep @androidx.annotation.Keep class * {*;}

#-keepclasseswithmembers class * {
#    @android.support.annotation.Keep <methods>;
#}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

#-keepclasseswithmembers class * {
#    @android.support.annotation.Keep <fields>;
#}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

#-keepclasseswithmembers class * {
#    @android.support.annotation.Keep <init>(...);
#}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# These classes are duplicated between android.jar and org.apache.http.legacy.jar.
-dontnote org.apache.http.**
-dontnote android.net.http.**

# These classes are duplicated between android.jar and core-lambda-stubs.jar.
-dontnote java.lang.invoke.**


# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/aidanfollestad/Documents/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# RetroFit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Glide
-keep public class * implements com.bumptech.glide.module.AppGlideModule
-keep public class * implements com.bumptech.glide.module.LibraryGlideModule
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# AndroidX
-keep,allowshrinking class androidx.** {*;}
-keep,allowshrinking interface androidx.** {*;}
-keep,allowshrinking,allowoptimization class com.google.android.material.** {*;}
-keep class android.support.v4.** {*;}

#
# jaudiotagger
#
-keepclassmembers class org.jaudiotagger.FileConstants {*;}
-keepclassmembers,allowoptimization class org.jaudiotagger.audio.** {<init>(...);public <methods>;public <fields>;}
-keepclassmembers,allowoptimization class org.jaudiotagger.utils.** {<init>(...);public <methods>;public <fields>;}
-keepclassmembers class org.jaudiotagger.tag.**{*;}
#


# Serialization
-keepnames class ** implements java.io.Serializable

# Json
-keep class * {
    @com.google.gson.annotations.SerializedName *;
}


# StatusBarLyric API
-keep class StatusBarLyric.API.StatusBarLyric {*;}


#
#  Phonograph
#

-keep class lib.phonograph.view.** {*;}
-keep class lib.phonograph.preference.** {*;}

-keep class player.phonograph.preferences.** {*;}
-keep class player.phonograph.views.** {*;}
-keep class player.phonograph.model.** {*;}

-keep,allowoptimization,allowshrinking class player.phonograph.ui.** {public <methods>;public <fields>;}
-keep,allowoptimization,allowshrinking class player.phonograph.service.** {public <methods>;public <fields>;}
-keepnames,allowoptimization,allowshrinking class player.phonograph.adapter.** {public <methods>;<fields>;}
-keepnames,allowoptimization,allowshrinking class player.phonograph.dialogs.** {public <methods>;}
-keepnames,allowoptimization,allowshrinking class player.phonograph.util.** {public <methods>;public <fields>;<init>(...);}
-keepnames,allowoptimization,allowshrinking class player.phonograph.glide.** {<init>(...);public <methods>;}
-keepclassmembernames,allowoptimization,allowshrinking class player.phonograph.settings.** {public <methods>;}
-keepclassmembernames,allowoptimization class player.phonograph.notification.** {public <methods>;}
