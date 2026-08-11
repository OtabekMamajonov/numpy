package com.uzcaptions.app.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzcaptions.app.data.local.entity.SubtitleProject
import com.uzcaptions.app.data.local.entity.SubtitleSegment
import com.uzcaptions.app.data.remote.SttRepository
import com.uzcaptions.app.data.remote.SttResult
import com.uzcaptions.app.data.repository.SubtitleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class EditorUiState(
    val project: SubtitleProject? = null,
    val segments: List<SubtitleSegment> = emptyList(),
    val isGenerating: Boolean = false,
    val generateError: String? = null
)

class EditorViewModel(
    private val projectId: Long,
    private val repository: SubtitleRepository,
    private val sttRepository: SttRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeProject(projectId),
                repository.observeSegments(projectId)
            ) { project, segments -> project to segments }
                .collect { (project, segments) ->
                    _uiState.value = _uiState.value.copy(project = project, segments = segments)
                }
        }
    }

    fun generateCaptions(context: Context) {
        val project = _uiState.value.project ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, generateError = null)
            when (val result = sttRepository.transcribe(context, Uri.parse(project.videoUri))) {
                is SttResult.Success -> {
                    repository.replaceSegments(projectId, result.segments)
                    _uiState.value = _uiState.value.copy(isGenerating = false)
                }
                is SttResult.Error -> {
                    _uiState.value = _uiState.value.copy(isGenerating = false, generateError = result.message)
                }
            }
        }
    }

    fun addSegmentAt(positionMs: Long) {
        viewModelScope.launch {
            val segments = _uiState.value.segments
            val afterIndex = segments.count { it.startMs <= positionMs } - 1
            val end = (positionMs + 2000).coerceAtMost(_uiState.value.project?.durationMs ?: (positionMs + 2000))
            repository.addSegment(projectId, afterIndex, positionMs, end, "")
        }
    }

    fun updateSegment(segment: SubtitleSegment) {
        viewModelScope.launch { repository.updateSegment(segment) }
    }

    fun deleteSegment(segment: SubtitleSegment) {
        viewModelScope.launch { repository.deleteSegment(segment) }
    }

    fun updateStyle(styleId: String) {
        val project = _uiState.value.project ?: return
        viewModelScope.launch { repository.updateProject(project.copy(styleId = styleId)) }
    }

    fun currentSegmentAt(positionMs: Long): SubtitleSegment? =
        _uiState.value.segments.firstOrNull { positionMs in it.startMs..it.endMs }
}
