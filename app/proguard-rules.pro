# Keep Moshi-generated code for STT request/response models
-keepclasseswithmembers class com.uzcaptions.app.data.remote.** {
    <init>(...);
}
-keep class com.squareup.moshi.** { *; }
-dontwarn okhttp3.internal.platform.**
