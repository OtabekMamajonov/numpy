package com.dokonhisob.app.ui.settings

import androidx.lifecycle.ViewModel
import com.dokonhisob.app.data.preferences.SettingsManager

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    fun getApiKey() = settingsManager.getApiKey()
    fun setApiKey(value: String) = settingsManager.setApiKey(value)

    fun getShopName() = settingsManager.getShopName()
    fun setShopName(value: String) = settingsManager.setShopName(value)

    fun getCurrency() = settingsManager.getCurrency()
    fun setCurrency(value: String) = settingsManager.setCurrency(value)

    fun getModel() = settingsManager.getModel()
    fun setModel(value: String) = settingsManager.setModel(value)
}
