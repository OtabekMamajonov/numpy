package com.uzcaptions.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.uzcaptions.app.data.local.entity.SubtitleSegment
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtitleSegmentDao {
    @Query("SELECT * FROM subtitle_segments WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun observeForProject(projectId: Long): Flow<List<SubtitleSegment>>

    @Query("SELECT * FROM subtitle_segments WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getForProject(projectId: Long): List<SubtitleSegment>

    @Insert
    suspend fun insert(segment: SubtitleSegment): Long

    @Insert
    suspend fun insertAll(segments: List<SubtitleSegment>)

    @Update
    suspend fun update(segment: SubtitleSegment)

    @Delete
    suspend fun delete(segment: SubtitleSegment)

    @Query("DELETE FROM subtitle_segments WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: Long)
}
