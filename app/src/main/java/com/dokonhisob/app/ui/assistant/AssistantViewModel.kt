package com.dokonhisob.app.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokonhisob.app.data.remote.ChatMessage
import com.dokonhisob.app.data.repository.AiAssistantRepository
import com.dokonhisob.app.data.repository.AiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiMessage(
    val role: String,
    val content: String,
    val isError: Boolean = false
)

data class AssistantUiState(
    val messages: List<ChatUiMessage> = listOf(
        ChatUiMessage(
            role = "assistant",
            content = "Assalomu alaykum! Men sizning do'koningiz uchun AI yordamchiman. " +
                "Bugungi savdo, foyda, xarajat yoki qarzdorlar haqida savol bering."
        )
    ),
    val isSending: Boolean = false
)

class AssistantViewModel(private val repository: AiAssistantRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.isSending) return

        val history = _uiState.value.messages
            .filter { !it.isError }
            .takeLast(10)
            .map { ChatMessage(role = it.role, content = it.content) }

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + ChatUiMessage(role = "user", content = text),
            isSending = true
        )

        viewModelScope.launch {
            when (val result = repository.ask(text, history)) {
                is AiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + ChatUiMessage(role = "assistant", content = result.reply),
                        isSending = false
                    )
                }
                is AiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + ChatUiMessage(role = "assistant", content = result.message, isError = true),
                        isSending = false
                    )
                }
            }
        }
    }
}
