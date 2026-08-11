package com.uzcaptions.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtitle_projects")
data class SubtitleProject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val videoUri: String,
    val durationMs: Long,
    val styleId: String = CaptionStyles.DEFAULT_ID,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
