package com.rehab.platform.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var scheduleReminders by remember { mutableStateOf(true) }
    var messageNotifications by remember { mutableStateOf(true) }
    var systemAnnouncements by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Master Switch
            ListItem(
                headlineContent = { Text("Enable Notifications") },
                supportingContent = { Text("Receive all app notifications") },
                leadingContent = {
                    Icon(Icons.Default.Notifications, null)
                },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            )
            
            Divider()
            
            // Schedule Reminders
            ListItem(
                headlineContent = { Text("Schedule Reminders") },
                supportingContent = { Text("Get notified for scheduled exercises") },
                leadingContent = {
                    Icon(Icons.Default.CalendarToday, null)
                },
                trailingContent = {
                    Switch(
                        checked = scheduleReminders,
                        onCheckedChange = { scheduleReminders = it },
                        enabled = notificationsEnabled
                    )
                }
            )
            
            // Message Notifications
            ListItem(
                headlineContent = { Text("Messages") },
                supportingContent = { Text("New messages from your expert") },
                leadingContent = {
                    Icon(Icons.Default.Message, null)
                },
                trailingContent = {
                    Switch(
                        checked = messageNotifications,
                        onCheckedChange = { messageNotifications = it },
                        enabled = notificationsEnabled
                    )
                }
            )
            
            // System Announcements
            ListItem(
                headlineContent = { Text("System Announcements") },
                supportingContent = { Text("Important updates and news") },
                leadingContent = {
                    Icon(Icons.Default.Campaign, null)
                },
                trailingContent = {
                    Switch(
                        checked = systemAnnouncements,
                        onCheckedChange = { systemAnnouncements = it },
                        enabled = notificationsEnabled
                    )
                }
            )
        }
    }
}
