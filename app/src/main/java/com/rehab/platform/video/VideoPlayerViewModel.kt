package com.rehab.platform.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rehab.platform.data.model.Video
import com.rehab.platform.repository.RehabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class VideoPlayerUiState(
    val isLoading: Boolean = false,
    val video: Video? = null,
    val isCompleted: Boolean = false,
    val showRatingDialog: Boolean = false,
    val scheduleCreated: Boolean = false,
    val error: String? = null
)

class VideoPlayerViewModel(
    private val repository: RehabRepository,
    private val videoId: Int
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()
    
    init {
        loadVideo()
    }
    
    private fun loadVideo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.getVideoById(videoId)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                video = result.getOrNull(),
                error = result.exceptionOrNull()?.message
            )
        }
    }
    
    fun markAsCompleted(notes: String = "") {
        viewModelScope.launch {
            val result = repository.markProgress(videoId, notes = notes)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isCompleted = true,
                    showRatingDialog = true
                )
            }
        }
    }
    
    fun submitRating(rating: Int, notes: String = "") {
        viewModelScope.launch {
            repository.updateProgress(
                videoId = videoId,
                rating = rating,
                notes = notes
            )
            
            _uiState.value = _uiState.value.copy(
                showRatingDialog = false
            )
        }
    }
    
    fun dismissRatingDialog() {
        _uiState.value = _uiState.value.copy(showRatingDialog = false)
    }
    
    fun addToSchedule(scheduledDate: String, notes: String = "") {
        viewModelScope.launch {
            val result = repository.createSchedule(videoId, scheduledDate, notes)
            if (result.isSuccess) {
                // Schedule notification
                result.getOrNull()?.let { schedule ->
                    try {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val scheduleTime = dateFormat.parse(scheduledDate)?.time ?: return@let
                        
                        // Only schedule if in the future
                        if (scheduleTime > System.currentTimeMillis()) {
                            _uiState.value = _uiState.value.copy(
                                scheduleCreated = true
                            )
                        }
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        }
    }
    
    fun refresh() = loadVideo()
}
