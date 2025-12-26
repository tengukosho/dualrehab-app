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
    val admins: List<com.rehab.platform.data.model.ExpertInfo> = emptyList(),
    val contacts: List<com.rehab.platform.data.model.ExpertInfo> = emptyList(),
    val selectedContactId: Int? = null,
    val selectedContactName: String? = null,
    val selectedContactRole: String? = null,
    val conversation: List<Message> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
    val sendingMessage: Boolean = false
)

class MessagesViewModel(private val repository: RehabRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()
    
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()
    
    
    private var hasLoadedData = false

    fun loadDataIfNeeded() {
        if (!hasLoadedData) {
            loadCurrentUser()
            loadContacts()
            loadUnreadCount()
            hasLoadedData = true
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            result.getOrNull()?.let { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
    }
    
    private fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.getAllContacts()
            
            if (result.isSuccess) {
                val allContacts = result.getOrNull() ?: emptyList()
                val experts = allContacts.filter { it.role == "expert" }
                val admins = allContacts.filter { it.role == "admin" }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    experts = experts,
                    admins = admins,
                    contacts = allContacts,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
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
    
    fun selectContact(contact: com.rehab.platform.data.model.ExpertInfo) {
        _uiState.value = _uiState.value.copy(
            selectedContactId = contact.id,
            selectedContactName = contact.name,
            selectedContactRole = contact.role
        )
        loadConversation(contact.id)
    }
    
    fun clearSelectedContact() {
        _uiState.value = _uiState.value.copy(
            selectedContactId = null,
            selectedContactName = null,
            selectedContactRole = null,
            conversation = emptyList()
        )
        loadContacts()
    }
    
    private fun loadConversation(contactId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.getConversation(contactId)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                conversation = result.getOrNull() ?: emptyList(),
                error = result.exceptionOrNull()?.message
            )
            
            // Mark messages as read
            if (result.isSuccess) {
                markConversationAsRead(contactId)
            }
        }
    }
    
    private fun markConversationAsRead(contactId: Int) {
        viewModelScope.launch {
            repository.sendMessage(contactId, "") // Placeholder, actual mark read happens in backend
            loadUnreadCount()
        }
    }
    
    fun updateMessageText(text: String) {
        _messageText.value = text
    }
    
    fun sendMessage() {
        val contactId = _uiState.value.selectedContactId ?: return
        val text = _messageText.value.ifBlank { return }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sendingMessage = true)
            
            val result = repository.sendMessage(contactId, text)
            
            if (result.isSuccess) {
                _messageText.value = ""
                loadConversation(contactId)
                loadUnreadCount()
            }
            
            _uiState.value = _uiState.value.copy(sendingMessage = false)
        }
    }
    
    fun shareVideo(videoId: Int, contactId: Int, message: String) {
        viewModelScope.launch {
            val videoMessage = """ðŸ“¹ Video Lesson #$videoId

$message"""
            
            val result = repository.sendMessage(contactId, videoMessage)
            
            if (result.isSuccess) {
                loadUnreadCount()
            }
        }
    }
    
    fun refresh() {
        loadCurrentUser()
        loadContacts()
        loadUnreadCount()
        _uiState.value.selectedContactId?.let { loadConversation(it) }
    }
}
