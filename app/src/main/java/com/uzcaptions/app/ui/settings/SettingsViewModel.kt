package com.uzcaptions.app.ui.settings

import androidx.lifecycle.ViewModel
import com.uzcaptions.app.data.preferences.SettingsManager

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {
    fun getSttApiKey() = settingsManager.getSttApiKey()
    fun setSttApiKey(value: String) = settingsManager.setSttApiKey(value)
}
