package com.rehab.platform

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.rehab.platform.api.RetrofitClient
import com.rehab.platform.auth.AuthViewModel
import com.rehab.platform.home.HomeViewModel
import com.rehab.platform.local.AuthDataStore
import com.rehab.platform.messages.MessagesViewModel
import com.rehab.platform.navigation.AppNavigation
import com.rehab.platform.notifications.NotificationHelper
import com.rehab.platform.progress.ProgressViewModel
import com.rehab.platform.profile.ProfileViewModel
import com.rehab.platform.repository.RehabRepository
import com.rehab.platform.schedule.ScheduleViewModel
import com.rehab.platform.ui.RehabPlatformTheme
import com.rehab.platform.utils.NetworkMonitor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var authDataStore: AuthDataStore
    private lateinit var repository: RehabRepository
    private lateinit var authViewModel: AuthViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var messagesViewModel: MessagesViewModel
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var notificationHelper: NotificationHelper // NEW
    private lateinit var networkMonitor: NetworkMonitor // NEW - Server monitoring
    
    // NEW: Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, notifications can be shown
        } else {
            // Permission denied - app will still work, just no notifications
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        authDataStore = AuthDataStore(applicationContext)
        
        lifecycleScope.launch {
            authDataStore.authToken.collect { token ->
                RetrofitClient.setAuthToken(token)
            }
        }
        
        repository = RehabRepository(
            apiService = RetrofitClient.apiService,
            authDataStore = authDataStore
        )
        
        authViewModel = AuthViewModel(repository)
        homeViewModel = HomeViewModel(repository)
        progressViewModel = ProgressViewModel(repository)
        messagesViewModel = MessagesViewModel(repository)
        scheduleViewModel = ScheduleViewModel(repository)
        profileViewModel = ProfileViewModel(repository)
        
        // NEW: Initialize notification system
        notificationHelper = NotificationHelper(this)
        
        // NEW: Initialize network monitor
        networkMonitor = NetworkMonitor(applicationContext, lifecycleScope)
        
        // NEW: Check server connection on startup
        lifecycleScope.launch {
            val isConnected = networkMonitor.checkOnce()
            if (!isConnected) {
                // Clear auth data if server is offline
                authDataStore.clearAll()
            }
        }
        
        // NEW: Start monitoring server connection
        networkMonitor.startMonitoring()
        
        // NEW: Request notification permission for Android 13+
        requestNotificationPermission()
        
        setContent {
            RehabPlatformTheme {
                val serverStatus by networkMonitor.serverStatus.collectAsState()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Server disconnection dialog
                    if (serverStatus is NetworkMonitor.ServerStatus.Disconnected) {
                        AlertDialog(
                            onDismissRequest = { },
                            icon = {
                                Icon(
                                    Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            title = { Text("Server Offline") },
                            text = {
                                Column {
                                    Text(
                                        (serverStatus as NetworkMonitor.ServerStatus.Disconnected).message
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Please check your connection and ensure the server is running.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        // Clear auth and force logout
                                        lifecycleScope.launch {
                                            authDataStore.clearAll()
                                            authViewModel.logout()
                                        }
                                    }
                                ) {
                                    Text("Return to Login")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        // Exit app
                                        finish()
                                    }
                                ) {
                                    Text("Exit App")
                                }
                            }
                        )
                    }
                    
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        homeViewModel = homeViewModel,
                        progressViewModel = progressViewModel,
                        messagesViewModel = messagesViewModel,
                        scheduleViewModel = scheduleViewModel,
                        profileViewModel = profileViewModel,
                        repository = repository
                    )
                }
            }
        }
    }
    
    // NEW: Request notification permission
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
