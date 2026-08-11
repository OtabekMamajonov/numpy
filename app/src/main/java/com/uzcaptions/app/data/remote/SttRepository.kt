package com.uzcaptions.app.data.remote

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.uzcaptions.app.data.local.entity.SubtitleSegment
import com.uzcaptions.app.data.preferences.SettingsManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SttResult {
    data class Success(val segments: List<SubtitleSegment>) : SttResult()
    data class Error(val message: String) : SttResult()
}

/**
 * Talks to the Muxlisa AI speech-to-text API (https://service.muxlisa.uz/api/v2/stt).
 * Each request accepts at most a 5 MB / 60 second audio file, so the source
 * video's audio is split into short chunks first (see [AudioChunkExtractor])
 * and transcribed one chunk at a time; each chunk becomes one timed subtitle
 * segment.
 */
class SttRepository(private val settingsManager: SettingsManager) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val errorAdapter = moshi.adapter(SttErrorResponse::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val service: SttService by lazy {
        Retrofit.Builder()
            .baseUrl("https://service.muxlisa.uz/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SttService::class.java)
    }

    suspend fun transcribe(context: Context, videoUri: Uri, durationMs: Long): SttResult {
        val apiKey = settingsManager.getSttApiKey()
        if (apiKey.isBlank()) {
            return SttResult.Error("Avval Sozlamalar bo'limida Muxlisa AI API key kiriting.")
        }
        if (durationMs <= 0) {
            return SttResult.Error("Video davomiyligini aniqlab bo'lmadi.")
        }

        val chunks = AudioChunkExtractor.extractChunks(context, videoUri, durationMs)
        if (chunks.isEmpty()) {
            return SttResult.Error(
                "Videodan audio ajratib bo'lmadi. Videoda ovoz yo'q yoki format qo'llab-quvvatlanmaydi."
            )
        }

        val segments = mutableListOf<SubtitleSegment>()
        try {
            for ((index, chunk) in chunks.withIndex()) {
                try {
                    val requestFile = chunk.file.asRequestBody("audio/mp4".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("audio", chunk.file.name, requestFile)

                    val response = service.speechToText(apiKey, part)

                    if (response.isSuccessful) {
                        val text = response.body()?.text?.trim().orEmpty()
                        if (text.isNotEmpty()) {
                            segments.add(
                                SubtitleSegment(
                                    projectId = 0,
                                    orderIndex = index,
                                    startMs = chunk.startMs,
                                    endMs = chunk.endMs,
                                    text = text
                                )
                            )
                        }
                    } else {
                        val message = parseError(response.code(), response.errorBody()?.string())
                        val partial = if (segments.isNotEmpty()) {
                            " (${formatTime(segments.last().endMs)} gacha bo'lgan qism tayyor)"
                        } else ""
                        return SttResult.Error("Xatolik ($message)$partial")
                    }
                } finally {
                    chunk.file.delete()
                }
            }
        } catch (e: IOException) {
            chunks.forEach { it.file.delete() }
            return SttResult.Error("Internet aloqasi topilmadi. Ulanishni tekshirib qayta urinib ko'ring.")
        } catch (e: Exception) {
            chunks.forEach { it.file.delete() }
            return SttResult.Error("Xatolik yuz berdi: ${e.message}")
        }

        return if (segments.isNotEmpty()) {
            SttResult.Success(segments)
        } else {
            SttResult.Error("Subtitr yaratilmadi — videoda tushunarli nutq aniqlanmadi.")
        }
    }

    private fun parseError(code: Int, body: String?): String {
        val detail = try {
            body?.let { errorAdapter.fromJson(it)?.detail }
        } catch (e: Exception) {
            null
        }

        return when (code) {
            400 -> detail ?: "audio fayl formati yoki hajmi mos emas"
            402 -> "Muxlisa AI hisobingizda mablag' yetarli emas"
            429 -> "so'rovlar limiti tugadi, birozdan keyin qayta urinib ko'ring"
            in 500..599 -> "Muxlisa AI serverida vaqtinchalik xatolik"
            else -> detail ?: "noma'lum xatolik (kod $code)"
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
    }
}
