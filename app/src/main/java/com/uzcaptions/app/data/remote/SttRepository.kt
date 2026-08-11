package com.uzcaptions.app.data.remote

import android.content.Context
import android.net.Uri
import com.uzcaptions.app.data.local.entity.SubtitleSegment
import com.uzcaptions.app.data.preferences.SettingsManager

sealed class SttResult {
    data class Success(val segments: List<SubtitleSegment>) : SttResult()
    data class Error(val message: String) : SttResult()
}

/**
 * Wraps the Muxlisa AI / uzbekvoice.ai speech-to-text API.
 *
 * NOT YET WIRED UP: the exact endpoint URL, auth header, multipart field
 * names and response JSON shape for that API were not available while this
 * was written (their docs pages were unreachable from this environment).
 * Once confirmed, replace the body of [transcribe] with a real Retrofit
 * call instead of returning [SttResult.Error]. Manual subtitle entry in the
 * editor works independently of this and is not affected.
 */
class SttRepository(private val settingsManager: SettingsManager) {

    suspend fun transcribe(context: Context, videoUri: Uri, languageCode: String = "uz"): SttResult {
        val apiKey = settingsManager.getSttApiKey()
        if (apiKey.isBlank()) {
            return SttResult.Error(
                "Avval Sozlamalar bo'limida Muxlisa AI (yoki uzbekvoice.ai) API key kiriting."
            )
        }

        return SttResult.Error(
            "Avtomatik subtitr yaratish hali ulanmagan: Muxlisa AI API'ning aniq so'rov " +
                "formati tasdiqlanishi kerak. Hozircha pastdagi \"Qo'lda qo'shish\" tugmasi " +
                "orqali subtitrlarni qo'lda yozib, dizaynlarni sinab ko'rishingiz mumkin."
        )
    }
}
