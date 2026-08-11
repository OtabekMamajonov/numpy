package com.dokonhisob.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stores sensitive settings (OpenAI API key, shop name, currency) using
 * EncryptedSharedPreferences so the API key never sits in plain text on disk.
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

    private val _apiKey = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKey.asStateFlow()

    private val _shopName = MutableStateFlow(getShopName())
    val shopNameFlow: StateFlow<String> = _shopName.asStateFlow()

    private val _currency = MutableStateFlow(getCurrency())
    val currencyFlow: StateFlow<String> = _currency.asStateFlow()

    fun getApiKey(): String = prefs.getString(KEY_OPENAI_API_KEY, "") ?: ""

    fun setApiKey(value: String) {
        prefs.edit().putString(KEY_OPENAI_API_KEY, value.trim()).apply()
        _apiKey.value = value.trim()
    }

    fun getShopName(): String = prefs.getString(KEY_SHOP_NAME, "Mening do'konim") ?: "Mening do'konim"

    fun setShopName(value: String) {
        prefs.edit().putString(KEY_SHOP_NAME, value).apply()
        _shopName.value = value
    }

    fun getCurrency(): String = prefs.getString(KEY_CURRENCY, "so'm") ?: "so'm"

    fun setCurrency(value: String) {
        prefs.edit().putString(KEY_CURRENCY, value).apply()
        _currency.value = value
    }

    fun getModel(): String = prefs.getString(KEY_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"

    fun setModel(value: String) {
        prefs.edit().putString(KEY_MODEL, value).apply()
    }

    companion object {
        private const val KEY_OPENAI_API_KEY = "openai_api_key"
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_MODEL = "openai_model"
    }
}
