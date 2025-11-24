package com.rehab.platform.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.rehab.platform.auth.AuthViewModel
import com.rehab.platform.home.HomeScreen
import com.rehab.platform.home.HomeViewModel
import com.rehab.platform.messages.MessagesScreen
import com.rehab.platform.messages.MessagesViewModel
import com.rehab.platform.profile.EditProfileScreen
import com.rehab.platform.profile.NotificationsScreen
import com.rehab.platform.profile.PrivacyScreen
import com.rehab.platform.profile.ProfileScreen
import com.rehab.platform.profile.ProfileViewModel
import com.rehab.platform.progress.ProgressScreen
import com.rehab.platform.progress.ProgressViewModel
import com.rehab.platform.repository.RehabRepository
import com.rehab.platform.schedule.ScheduleScreen
import com.rehab.platform.schedule.ScheduleViewModel
import com.rehab.platform.video.VideoPlayerScreen
import com.rehab.platform.video.VideoPlayerViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Schedule : Screen("schedule")
    object Progress : Screen("progress")
    object Messages : Screen("messages")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Notifications : Screen("notifications")
    object Privacy : Screen("privacy")
    object VideoDetail : Screen("video/{videoId}") {
        fun createRoute(videoId: Int) = "video/$videoId"
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home)
    object Schedule : BottomNavItem(Screen.Schedule.route, "Schedule", Icons.Default.CalendarToday)
    object Progress : BottomNavItem(Screen.Progress.route, "Progress", Icons.Default.TrendingUp)
    object Messages : BottomNavItem(Screen.Messages.route, "Messages", Icons.Default.Message)
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    progressViewModel: ProgressViewModel,
    messagesViewModel: MessagesViewModel,
    scheduleViewModel: ScheduleViewModel,
    profileViewModel: ProfileViewModel,
    repository: RehabRepository
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by remember {
        derivedStateOf {
            runCatching {
                homeViewModel.uiState.value.categories.isNotEmpty()
            }.getOrNull()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
    ) {
        // Auth Screens (Login/Register remain the same)
        composable(Screen.Login.route) {
            com.rehab.platform.auth.LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            com.rehab.platform.auth.RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Main App with Bottom Nav
        composable(Screen.Home.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                progressViewModel = progressViewModel,
                messagesViewModel = messagesViewModel,
                scheduleViewModel = scheduleViewModel,
                repository = repository
            )
        }
        
        composable(Screen.Schedule.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                progressViewModel = progressViewModel,
                messagesViewModel = messagesViewModel,
                scheduleViewModel = scheduleViewModel,
                repository = repository
            )
        }
        
        composable(Screen.Progress.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                progressViewModel = progressViewModel,
                messagesViewModel = messagesViewModel,
                scheduleViewModel = scheduleViewModel,
                repository = repository
            )
        }
        
        composable(Screen.Messages.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                progressViewModel = progressViewModel,
                messagesViewModel = messagesViewModel,
                scheduleViewModel = scheduleViewModel,
                repository = repository
            )
        }
        
        composable(Screen.Profile.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                progressViewModel = progressViewModel,
                messagesViewModel = messagesViewModel,
                scheduleViewModel = scheduleViewModel,
                repository = repository
            )
        }
        
        // Video Detail
        composable(
            route = Screen.VideoDetail.route,
            arguments = listOf(navArgument("videoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getInt("videoId") ?: 0
            val videoViewModel = remember {
                VideoPlayerViewModel(repository, videoId)
            }
            VideoPlayerScreen(
                viewModel = videoViewModel,
                messagesViewModel = messagesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Profile Settings Screens
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                user = messagesViewModel.uiState.value.currentUser,
                viewModel = profileViewModel,
                onSave = { name, email, phone, hospital ->
                    // Handled by viewModel
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Privacy.route) {
            PrivacyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    progressViewModel: ProgressViewModel,
    messagesViewModel: MessagesViewModel,
    scheduleViewModel: ScheduleViewModel,
    repository: RehabRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val messagesUiState by messagesViewModel.uiState.collectAsState()
    
    // Load current user
    LaunchedEffect(Unit) {
        messagesViewModel.loadCurrentUser()
    }
    
    // Get current user for profile
    val currentUser = remember {
        derivedStateOf {
            messagesUiState.currentUser
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Schedule,
                    BottomNavItem.Progress,
                    BottomNavItem.Messages,
                    BottomNavItem.Profile
                )
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            if (item == BottomNavItem.Messages && messagesUiState.unreadCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("${messagesUiState.unreadCount}")
                                        }
                                    }
                                ) {
                                    Icon(item.icon, contentDescription = item.title)
                                }
                            } else {
                                Icon(item.icon, contentDescription = item.title)
                            }
                        },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Auto-refresh when tab is selected
        LaunchedEffect(currentRoute) {
            when (currentRoute) {
                Screen.Home.route -> homeViewModel.refresh()
                Screen.Schedule.route -> scheduleViewModel.refresh()
                Screen.Progress.route -> progressViewModel.refresh()
                Screen.Messages.route -> messagesViewModel.refresh()
                Screen.Profile.route -> messagesViewModel.loadCurrentUser()
            }
        }
        
        Box(Modifier.padding(paddingValues)) {
            when (currentRoute) {
                Screen.Home.route -> {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        onVideoClick = { video ->
                            navController.navigate(Screen.VideoDetail.createRoute(video.id))
                        },
                        onNavigateToSchedule = { },
                        onNavigateToProgress = { },
                        onNavigateToMessages = { },
                        onLogout = { }
                    )
                }
                
                Screen.Schedule.route -> {
                    ScheduleScreen(
                        viewModel = scheduleViewModel,
                        onVideoClick = { videoId ->
                            navController.navigate(Screen.VideoDetail.createRoute(videoId))
                        }
                    )
                }
                
                Screen.Progress.route -> {
                    ProgressScreen(viewModel = progressViewModel)
                }
                
                Screen.Messages.route -> {
                    MessagesScreen(
                        viewModel = messagesViewModel,
                        onShareVideo = { videoId ->
                            navController.navigate(Screen.VideoDetail.createRoute(videoId))
                        }
                    )
                }
                
                Screen.Profile.route -> {
                    ProfileScreen(
                        user = currentUser.value,
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToEditProfile = {
                            navController.navigate(Screen.EditProfile.route)
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Screen.Notifications.route)
                        },
                        onNavigateToPrivacy = {
                            navController.navigate(Screen.Privacy.route)
                        }
                    )
                }
            }
        }
    }
}
