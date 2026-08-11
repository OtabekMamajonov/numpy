package com.uzcaptions.app.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzcaptions.app.data.local.entity.SubtitleProject
import com.uzcaptions.app.data.repository.SubtitleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(private val repository: SubtitleRepository) : ViewModel() {

    val projects: StateFlow<List<SubtitleProject>> = repository.observeProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createProject(title: String, videoUri: String, durationMs: Long, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createProject(title, videoUri, durationMs)
            onCreated(id)
        }
    }

    fun deleteProject(project: SubtitleProject) {
        viewModelScope.launch { repository.deleteProject(project) }
    }
}
