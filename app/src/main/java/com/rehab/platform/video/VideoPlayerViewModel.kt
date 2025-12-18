package com.rehab.platform.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rehab.platform.BuildConfig
import com.rehab.platform.data.model.Video
import com.rehab.platform.download.VideoDownloadManager
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
    val error: String? = null,
    // Download states
    val isDownloaded: Boolean = false,
    val downloadProgress: Int = 0,
    val isDownloading: Boolean = false
)

class VideoPlayerViewModel(
    private val repository: RehabRepository,
    private val videoId: Int,
    private val downloadManager: VideoDownloadManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()
    
    init {
        loadVideo()
        checkDownloadStatus()
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
    
    fun refresh() {
        loadVideo()
        checkDownloadStatus()
    }
    
    private fun checkDownloadStatus() {
        _uiState.value = _uiState.value.copy(
            isDownloaded = downloadManager.isVideoDownloaded(videoId)
        )
    }
    
    fun downloadVideo() {
        viewModelScope.launch {
            val video = _uiState.value.video ?: return@launch
            
            if (downloadManager.isVideoDownloaded(videoId)) {
                // Already downloaded, delete it
                val deleted = downloadManager.deleteDownloadedVideo(videoId)
                _uiState.value = _uiState.value.copy(
                    isDownloaded = !deleted,
                    isDownloading = false,
                    downloadProgress = 0
                )
            } else {
                // Download it
                val videoUrl = "${BuildConfig.BASE_URL.replace("/api/", "")}${video.videoUrl}"
                downloadManager.downloadVideo(video, videoUrl)
                _uiState.value = _uiState.value.copy(
                    isDownloading = true,
                    downloadProgress = 0
                )
                
                // Monitor download progress
                monitorDownloadProgress()
            }
        }
    }
    
    private fun monitorDownloadProgress() {
        viewModelScope.launch {
            downloadManager.downloads.collect { downloads ->
                val download = downloads[videoId]
                if (download != null) {
                    _uiState.value = _uiState.value.copy(
                        downloadProgress = download.progress,
                        isDownloading = download.status == com.rehab.platform.download.DownloadStatus.DOWNLOADING,
                        isDownloaded = download.status == com.rehab.platform.download.DownloadStatus.COMPLETED
                    )
                }
            }
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
                        
                        // This will be called from the activity/fragment context
                        // notificationHelper.scheduleExerciseReminder(
                        //     schedule.id,
                        //     "Exercise Reminder",
                        //     _uiState.value.video?.title ?: "Exercise",
                        //     scheduleTime
                        // )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                _uiState.value = _uiState.value.copy(scheduleCreated = true)
            }
        }
    }
}
