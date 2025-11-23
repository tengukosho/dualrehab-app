package com.rehab.platform.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rehab.platform.data.model.Message
import com.rehab.platform.data.model.User
import com.rehab.platform.repository.RehabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MessagesUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val experts: List<com.rehab.platform.data.model.ExpertInfo> = emptyList(),
    val selectedExpertId: Int? = null,
    val selectedExpertName: String? = null,
    val conversation: List<Message> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)

class MessagesViewModel(private val repository: RehabRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()
    
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()
    
    init {
        loadCurrentUser()
        loadExperts()
        loadUnreadCount()
    }
    
    fun loadCurrentUser() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            result.getOrNull()?.let { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
    }
    
    private fun loadExperts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getAssignedExperts()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                experts = result.getOrNull() ?: emptyList()
            )
        }
    }
    
    private fun loadUnreadCount() {
        viewModelScope.launch {
            val result = repository.getUnreadCount()
            _uiState.value = _uiState.value.copy(
                unreadCount = result.getOrNull()?.count ?: 0
            )
        }
    }
    
    fun selectExpert(expert: com.rehab.platform.data.model.ExpertInfo) {
        _uiState.value = _uiState.value.copy(
            selectedExpertId = expert.id,
            selectedExpertName = expert.name
        )
        loadConversation(expert.id)
    }
    
    fun clearSelectedExpert() {
        _uiState.value = _uiState.value.copy(
            selectedExpertId = null,
            selectedExpertName = null,
            conversation = emptyList()
        )
        loadExperts()
    }
    
    fun loadConversation(expertId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.getConversation(expertId)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                conversation = result.getOrNull() ?: emptyList(),
                error = result.exceptionOrNull()?.message
            )
        }
    }
    
    fun updateMessageText(text: String) {
        _messageText.value = text
    }
    
    fun sendMessage(videoId: Int? = null) {
        val expertId = _uiState.value.selectedExpertId ?: return
        val text = _messageText.value.ifBlank { return }
        
        viewModelScope.launch {
            val messageContent = if (videoId != null) {
                """📹 Video lesson #$videoId

$text"""
            } else {
                text
            }
            
            val result = repository.sendMessage(expertId, messageContent)
            
            if (result.isSuccess) {
                _messageText.value = ""
                loadConversation(expertId)
            }
        }
    }
    
    fun refresh() {
        loadCurrentUser()
        loadExperts()
        loadUnreadCount()
        _uiState.value.selectedExpertId?.let { loadConversation(it) }
    }
}
