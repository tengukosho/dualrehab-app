package com.rehab.platform.profile

import androidx.compose.foundation.clickable
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
fun PrivacyScreen(
    onNavigateBack: () -> Unit
) {
    var shareProgress by remember { mutableStateOf(true) }
    var allowExpertMessages by remember { mutableStateOf(true) }
    var showChangePassword by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security") },
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
            // Privacy Settings
            Text(
                "Privacy",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            ListItem(
                headlineContent = { Text("Share Progress with Expert") },
                supportingContent = { Text("Allow expert to view your exercise progress") },
                leadingContent = {
                    Icon(Icons.Default.Share, null)
                },
                trailingContent = {
                    Switch(
                        checked = shareProgress,
                        onCheckedChange = { shareProgress = it }
                    )
                }
            )
            
            ListItem(
                headlineContent = { Text("Allow Expert Messages") },
                supportingContent = { Text("Receive messages from assigned expert") },
                leadingContent = {
                    Icon(Icons.Default.Message, null)
                },
                trailingContent = {
                    Switch(
                        checked = allowExpertMessages,
                        onCheckedChange = { allowExpertMessages = it }
                    )
                }
            )
            
            Divider(Modifier.padding(vertical = 8.dp))
            
            // Security Settings
            Text(
                "Security",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            ListItem(
                headlineContent = { Text("Change Password") },
                supportingContent = { Text("Update your account password") },
                leadingContent = {
                    Icon(Icons.Default.Lock, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                },
                modifier = Modifier.clickable { showChangePassword = true }
            )
            
            ListItem(
                headlineContent = { Text("Two-Factor Authentication") },
                supportingContent = { Text("Coming soon") },
                leadingContent = {
                    Icon(Icons.Default.Security, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                }
            )
            
            Divider(Modifier.padding(vertical = 8.dp))
            
            // Data Management
            Text(
                "Data Management",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            ListItem(
                headlineContent = { Text("Download My Data") },
                supportingContent = { Text("Get a copy of your data") },
                leadingContent = {
                    Icon(Icons.Default.Download, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                }
            )
            
            ListItem(
                headlineContent = { Text("Delete Account") },
                supportingContent = { Text("Permanently delete your account") },
                leadingContent = {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                }
            )
        }
    }
    
    // Change Password Dialog
    if (showChangePassword) {
        ChangePasswordDialog(
            onDismiss = { showChangePassword = false },
            onConfirm = { oldPassword, newPassword ->
                // Handle password change
                showChangePassword = false
            }
        )
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, null) },
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(oldPassword, newPassword) },
                enabled = newPassword.isNotEmpty() && newPassword == confirmPassword
            ) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
