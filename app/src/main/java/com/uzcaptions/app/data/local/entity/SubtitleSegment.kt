package com.uzcaptions.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubtitleWord(
    val text: String,
    val startMs: Long,
    val endMs: Long
)

@Entity(tableName = "subtitle_segments")
data class SubtitleSegment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val orderIndex: Int,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    /** JSON-encoded List<SubtitleWord>, used for word-by-word (karaoke) highlight animation. Null if not available. */
    val wordsJson: String? = null
)
