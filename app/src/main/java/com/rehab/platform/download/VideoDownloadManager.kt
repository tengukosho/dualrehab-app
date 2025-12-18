package com.rehab.platform.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rehab.platform.data.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class DownloadState(
    val videoId: Int,
    val progress: Int,
    val status: DownloadStatus
)

enum class DownloadStatus {
    IDLE, DOWNLOADING, COMPLETED, FAILED, PAUSED
}

class VideoDownloadManager(private val context: Context) {
    
    private val _downloads = MutableStateFlow<Map<Int, DownloadState>>(emptyMap())
    val downloads: StateFlow<Map<Int, DownloadState>> = _downloads.asStateFlow()
    
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val downloadIds = mutableMapOf<Int, Long>() // videoId to downloadId mapping
    
    fun downloadVideo(video: Video, videoUrl: String) {
        val request = DownloadManager.Request(Uri.parse(videoUrl)).apply {
            setTitle("Downloading ${video.title}")
            setDescription("Exercise video for offline viewing")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "rehab_videos/${video.id}.mp4"
            )
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or 
                DownloadManager.Request.NETWORK_MOBILE
            )
        }
        
        val downloadId = downloadManager.enqueue(request)
        downloadIds[video.id] = downloadId
        
        _downloads.value = _downloads.value + (video.id to DownloadState(
            videoId = video.id,
            progress = 0,
            status = DownloadStatus.DOWNLOADING
        ))
        
        // Start monitoring progress
        monitorDownload(video.id, downloadId)
    }
    
    private fun monitorDownload(videoId: Int, downloadId: Long) {
        // This would typically use a coroutine or WorkManager to monitor
        // For now, just update status
        Thread {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    
                    val status = cursor.getInt(statusIndex)
                    val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                    val totalBytes = cursor.getLong(totalBytesIndex)
                    
                    val progress = if (totalBytes > 0) {
                        ((bytesDownloaded * 100) / totalBytes).toInt()
                    } else 0
                    
                    when (status) {
                        DownloadManager.STATUS_RUNNING -> {
                            _downloads.value = _downloads.value + (videoId to DownloadState(
                                videoId = videoId,
                                progress = progress,
                                status = DownloadStatus.DOWNLOADING
                            ))
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            _downloads.value = _downloads.value + (videoId to DownloadState(
                                videoId = videoId,
                                progress = 100,
                                status = DownloadStatus.COMPLETED
                            ))
                            downloading = false
                        }
                        DownloadManager.STATUS_FAILED -> {
                            _downloads.value = _downloads.value + (videoId to DownloadState(
                                videoId = videoId,
                                progress = 0,
                                status = DownloadStatus.FAILED
                            ))
                            downloading = false
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            _downloads.value = _downloads.value + (videoId to DownloadState(
                                videoId = videoId,
                                progress = progress,
                                status = DownloadStatus.PAUSED
                            ))
                        }
                    }
                }
                
                cursor.close()
                Thread.sleep(500) // Update every 500ms
            }
        }.start()
    }
    
    fun cancelDownload(videoId: Int) {
        downloadIds[videoId]?.let { downloadId ->
            downloadManager.remove(downloadId)
            downloadIds.remove(videoId)
            _downloads.value = _downloads.value - videoId
        }
    }
    
    fun isVideoDownloaded(videoId: Int): Boolean {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "rehab_videos/$videoId.mp4"
        )
        return file.exists()
    }
    
    fun getDownloadedVideoPath(videoId: Int): String? {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "rehab_videos/$videoId.mp4"
        )
        return if (file.exists()) file.absolutePath else null
    }
    
    fun deleteDownloadedVideo(videoId: Int): Boolean {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "rehab_videos/$videoId.mp4"
        )
        return file.delete()
    }
    
    fun getDownloadedVideos(): List<Int> {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "rehab_videos"
        )
        
        if (!dir.exists()) return emptyList()
        
        return dir.listFiles()
            ?.filter { it.isFile && it.extension == "mp4" }
            ?.mapNotNull { it.nameWithoutExtension.toIntOrNull() }
            ?: emptyList()
    }
}
