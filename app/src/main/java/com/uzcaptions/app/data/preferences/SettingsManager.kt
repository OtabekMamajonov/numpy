package com.uzcaptions.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the Muxlisa/uzbekvoice speech-to-text API key using
 * EncryptedSharedPreferences so it never sits in plain text on disk.
 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getSttApiKey(): String = prefs.getString(KEY_STT_API_KEY, "") ?: ""

    fun setSttApiKey(value: String) {
        prefs.edit().putString(KEY_STT_API_KEY, value.trim()).apply()
    }

    companion object {
        private const val KEY_STT_API_KEY = "stt_api_key"
    }
}
