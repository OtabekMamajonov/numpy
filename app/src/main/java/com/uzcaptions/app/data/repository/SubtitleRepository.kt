package com.uzcaptions.app.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.uzcaptions.app.data.local.AppDatabase
import com.uzcaptions.app.data.local.entity.SubtitleProject
import com.uzcaptions.app.data.local.entity.SubtitleSegment
import com.uzcaptions.app.data.local.entity.SubtitleWord
import kotlinx.coroutines.flow.Flow

class SubtitleRepository(private val db: AppDatabase) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val wordsListType = Types.newParameterizedType(List::class.java, SubtitleWord::class.java)
    private val wordsAdapter = moshi.adapter<List<SubtitleWord>>(wordsListType)

    fun observeProjects(): Flow<List<SubtitleProject>> = db.subtitleProjectDao().observeAll()
    fun observeProject(id: Long): Flow<SubtitleProject?> = db.subtitleProjectDao().observeById(id)
    suspend fun getProject(id: Long): SubtitleProject? = db.subtitleProjectDao().getById(id)

    suspend fun createProject(title: String, videoUri: String, durationMs: Long): Long =
        db.subtitleProjectDao().upsert(
            SubtitleProject(title = title, videoUri = videoUri, durationMs = durationMs)
        )

    suspend fun updateProject(project: SubtitleProject) =
        db.subtitleProjectDao().update(project.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteProject(project: SubtitleProject) {
        db.subtitleSegmentDao().deleteAllForProject(project.id)
        db.subtitleProjectDao().delete(project)
    }

    fun observeSegments(projectId: Long): Flow<List<SubtitleSegment>> =
        db.subtitleSegmentDao().observeForProject(projectId)

    suspend fun getSegments(projectId: Long): List<SubtitleSegment> =
        db.subtitleSegmentDao().getForProject(projectId)

    suspend fun replaceSegments(projectId: Long, segments: List<SubtitleSegment>) {
        db.subtitleSegmentDao().deleteAllForProject(projectId)
        db.subtitleSegmentDao().insertAll(segments.mapIndexed { index, segment ->
            segment.copy(id = 0, projectId = projectId, orderIndex = index)
        })
    }

    suspend fun updateSegment(segment: SubtitleSegment) = db.subtitleSegmentDao().update(segment)
    suspend fun deleteSegment(segment: SubtitleSegment) = db.subtitleSegmentDao().delete(segment)

    suspend fun addSegment(projectId: Long, afterOrderIndex: Int, startMs: Long, endMs: Long, text: String) {
        val current = getSegments(projectId)
        val reindexed = current.map { if (it.orderIndex > afterOrderIndex) it.copy(orderIndex = it.orderIndex + 1) else it }
        reindexed.forEach { db.subtitleSegmentDao().update(it) }
        db.subtitleSegmentDao().insert(
            SubtitleSegment(
                projectId = projectId,
                orderIndex = afterOrderIndex + 1,
                startMs = startMs,
                endMs = endMs,
                text = text
            )
        )
    }

    fun encodeWords(words: List<SubtitleWord>): String = wordsAdapter.toJson(words)

    fun decodeWords(json: String?): List<SubtitleWord> {
        if (json.isNullOrBlank()) return emptyList()
        return wordsAdapter.fromJson(json) ?: emptyList()
    }

    /**
     * Fallback word timings when the STT provider doesn't return per-word
     * timestamps: splits the segment's duration evenly across its words so
     * karaoke-style highlight styles still have something to animate.
     */
    fun evenlySpacedWords(text: String, startMs: Long, endMs: Long): List<SubtitleWord> {
        val tokens = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()
        val duration = (endMs - startMs).coerceAtLeast(tokens.size.toLong())
        val perWord = duration / tokens.size
        return tokens.mapIndexed { index, word ->
            val wordStart = startMs + perWord * index
            val wordEnd = if (index == tokens.lastIndex) endMs else wordStart + perWord
            SubtitleWord(word, wordStart, wordEnd)
        }
    }
}
