package com.rehab.platform.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rehab.platform.data.model.User
import com.rehab.platform.repository.RehabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val updateSuccess: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(private val repository: RehabRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    fun updateProfile(name: String, email: String, phoneNumber: String, hospital: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.updateProfile(
                name = name.takeIf { it.isNotBlank() },
                email = email.takeIf { it.isNotBlank() },
                phoneNumber = phoneNumber.takeIf { it.isNotBlank() },
                hospital = hospital.takeIf { it.isNotBlank() }
            )
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    user = result.getOrNull(),
                    updateSuccess = true
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }
}
