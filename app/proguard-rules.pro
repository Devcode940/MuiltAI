# AndroidX Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.Database { *; }
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.material.** { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Activities and ViewModels
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends androidx.lifecycle.ViewModel

# R classes
-keep class **.R$* { *; }

# Data binding
-keep class **.BR { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# GSON
-keep class com.google.gson.** { *; }

# WebView
-keep class android.webkit.** { *; }
-keep class androidx.webkit.** { *; }

# Parcelable
-keep public class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Source file and line numbers
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Native methods
-keep class * extends java.lang.Object {
    native <methods>;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

# Lambda classes
-keep class * implements kotlin.jvm.functions.* { *; }
