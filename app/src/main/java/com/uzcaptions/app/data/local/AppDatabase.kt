package com.uzcaptions.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uzcaptions.app.data.local.dao.SubtitleProjectDao
import com.uzcaptions.app.data.local.dao.SubtitleSegmentDao
import com.uzcaptions.app.data.local.entity.SubtitleProject
import com.uzcaptions.app.data.local.entity.SubtitleSegment

@Database(
    entities = [SubtitleProject::class, SubtitleSegment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subtitleProjectDao(): SubtitleProjectDao
    abstract fun subtitleSegmentDao(): SubtitleSegmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uzcaptions.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
