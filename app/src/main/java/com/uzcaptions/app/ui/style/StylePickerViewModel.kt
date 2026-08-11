package com.uzcaptions.app.ui.style

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzcaptions.app.data.local.entity.SubtitleProject
import com.uzcaptions.app.data.repository.SubtitleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StylePickerViewModel(
    private val projectId: Long,
    private val repository: SubtitleRepository
) : ViewModel() {

    val project: StateFlow<SubtitleProject?> = repository.observeProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectStyle(styleId: String) {
        viewModelScope.launch {
            val current = repository.getProject(projectId) ?: return@launch
            repository.updateProject(current.copy(styleId = styleId))
        }
    }
}
