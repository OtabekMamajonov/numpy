package com.uzcaptions.app

import android.app.Application
import com.uzcaptions.app.data.local.AppDatabase
import com.uzcaptions.app.data.preferences.SettingsManager
import com.uzcaptions.app.data.remote.SttRepository
import com.uzcaptions.app.data.repository.SubtitleRepository

class UzCaptionsApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var settingsManager: SettingsManager
        private set
    lateinit var subtitleRepository: SubtitleRepository
        private set
    lateinit var sttRepository: SttRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        settingsManager = SettingsManager(this)
        subtitleRepository = SubtitleRepository(database)
        sttRepository = SttRepository(settingsManager)
    }
}
