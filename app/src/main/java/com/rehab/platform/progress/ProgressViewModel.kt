package com.rehab.platform.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rehab.platform.data.model.ProgressStats
import com.rehab.platform.data.model.UserProgress
import com.rehab.platform.repository.RehabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProgressUiState(
    val isLoading: Boolean = false,
    val stats: ProgressStats? = null,
    val recentProgress: List<UserProgress> = emptyList(),
    val error: String? = null
)

class ProgressViewModel(private val repository: RehabRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    private var hasLoadedData = false
    
    fun loadProgressIfNeeded() {
        if (!hasLoadedData) {
            loadProgress()
            hasLoadedData = true
        }
    }
    
    fun loadProgress() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val statsResult = repository.getProgressStats()
            val progressResult = repository.getProgress()
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                stats = statsResult.getOrNull(),
                recentProgress = progressResult.getOrNull()?.progress ?: emptyList(),
                error = statsResult.exceptionOrNull()?.message
            )
        }
    }
    
    fun refresh() = loadProgress()
}
