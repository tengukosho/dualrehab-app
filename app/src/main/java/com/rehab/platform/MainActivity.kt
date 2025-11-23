package com.rehab.platform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.rehab.platform.api.RetrofitClient
import com.rehab.platform.auth.AuthViewModel
import com.rehab.platform.home.HomeViewModel
import com.rehab.platform.local.AuthDataStore
import com.rehab.platform.messages.MessagesViewModel
import com.rehab.platform.navigation.AppNavigation
import com.rehab.platform.progress.ProgressViewModel
import com.rehab.platform.profile.ProfileViewModel
import com.rehab.platform.repository.RehabRepository
import com.rehab.platform.schedule.ScheduleViewModel
import com.rehab.platform.ui.RehabPlatformTheme
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
        
        setContent {
            RehabPlatformTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
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
}
