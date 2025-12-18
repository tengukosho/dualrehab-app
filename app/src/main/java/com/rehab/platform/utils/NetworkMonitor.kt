package com.rehab.platform.utils

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.rehab.platform.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NetworkMonitor(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Checking)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus

    sealed class ServerStatus {
        object Checking : ServerStatus()
        object Connected : ServerStatus()
        data class Disconnected(val message: String = "Cannot connect to server") : ServerStatus()
    }

    fun startMonitoring() {
        lifecycleScope.launch {
            while (true) {
                checkServerConnection()
                delay(10000) // Check every 10 seconds
            }
        }
    }

    private suspend fun checkServerConnection() {
        try {
            val response = RetrofitClient.apiService.getCategories()
            // FIXED: 401 means server is running but not authenticated - that's OK!
            // We just want to know if server is reachable, not if we're logged in
            if (response.isSuccessful || response.code() == 401) {
                _serverStatus.value = ServerStatus.Connected
            } else {
                _serverStatus.value = ServerStatus.Disconnected("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            _serverStatus.value = ServerStatus.Disconnected(
                when {
                    e.message?.contains("Unable to resolve host") == true -> "Server is offline"
                    e.message?.contains("timeout") == true -> "Connection timeout"
                    e.message?.contains("Failed to connect") == true -> "Cannot reach server"
                    else -> "Connection error: ${e.message}"
                }
            )
        }
    }

    suspend fun checkOnce(): Boolean {
        return try {
            val response = RetrofitClient.apiService.getCategories()
            // FIXED: Server is reachable if we get ANY response (including 401)
            // 401 just means not authenticated, but server is running
            response.isSuccessful || response.code() == 401
        } catch (e: Exception) {
            false
        }
    }
}