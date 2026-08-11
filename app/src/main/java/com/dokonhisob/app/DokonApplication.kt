package com.dokonhisob.app

import android.app.Application
import com.dokonhisob.app.data.local.AppDatabase
import com.dokonhisob.app.data.preferences.SettingsManager
import com.dokonhisob.app.data.repository.AiAssistantRepository
import com.dokonhisob.app.data.repository.ShopRepository

class DokonApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var settingsManager: SettingsManager
        private set
    lateinit var shopRepository: ShopRepository
        private set
    lateinit var aiAssistantRepository: AiAssistantRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        settingsManager = SettingsManager(this)
        shopRepository = ShopRepository(database)
        aiAssistantRepository = AiAssistantRepository(shopRepository, settingsManager)
    }
}
