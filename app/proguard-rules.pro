# Keep Moshi-generated code for OpenAI request/response models
-keepclasseswithmembers class com.dokonhisob.app.data.remote.** {
    <init>(...);
}
-keep class com.squareup.moshi.** { *; }
-dontwarn okhttp3.internal.platform.**
