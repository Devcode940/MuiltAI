# Keep Room entities/DAOs used by generated database code.
# WHY: Room resolves these types through generated adapters and annotations.
-keep class com.multaihub.app.data.model.** { *; }
-keep class com.multaihub.app.data.local.** { *; }

# Keep application entry points referenced by Android framework configuration.
-keep public class com.multaihub.app.MultiAIApp { *; }
-keep public class com.multaihub.app.MainActivity { *; }

# Keep ViewModel constructors used by AndroidX ViewModelProvider factories.
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# Preserve annotations/signatures used by Room and Kotlin reflection metadata.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keep class kotlin.Metadata { *; }

# Preserve source locations for actionable release crash reports.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Preserve Parcelable creators when an Android model implements Parcelable.
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Preserve enum conversion methods used by serialization/UI state.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
