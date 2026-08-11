package com.uzcaptions.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uzcaptions.app.data.local.entity.SubtitleProject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtitleProjectDao {
    @Query("SELECT * FROM subtitle_projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SubtitleProject>>

    @Query("SELECT * FROM subtitle_projects WHERE id = :id")
    fun observeById(id: Long): Flow<SubtitleProject?>

    @Query("SELECT * FROM subtitle_projects WHERE id = :id")
    suspend fun getById(id: Long): SubtitleProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: SubtitleProject): Long

    @Update
    suspend fun update(project: SubtitleProject)

    @Delete
    suspend fun delete(project: SubtitleProject)
}
