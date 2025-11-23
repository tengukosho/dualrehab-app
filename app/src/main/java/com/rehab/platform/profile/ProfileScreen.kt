package com.rehab.platform.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rehab.platform.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // User Info Card
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        user?.name ?: "Loading...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        user?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (user?.hospital != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalHospital,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                user.hospital,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (user?.phoneNumber != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                user.phoneNumber,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
            
            // Settings Sections
            Text(
                "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            
            // Notifications
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text("Manage notification preferences") },
                leadingContent = {
                    Icon(Icons.Default.Notifications, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                },
                modifier = Modifier.clickable { onNavigateToNotifications() }
            )
            
            // Privacy & Security
            ListItem(
                headlineContent = { Text("Privacy & Security") },
                supportingContent = { Text("Control your privacy settings") },
                leadingContent = {
                    Icon(Icons.Default.Security, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                },
                modifier = Modifier.clickable { onNavigateToPrivacy() }
            )
            
            Divider(Modifier.padding(vertical = 8.dp))
            
            // About Section
            Text(
                "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            
            ListItem(
                headlineContent = { Text("About RehabPlatform") },
                supportingContent = { Text("Version 1.0.0") },
                leadingContent = {
                    Icon(Icons.Default.Info, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                },
                modifier = Modifier.clickable { onNavigateToAbout() }
            )
            
            ListItem(
                headlineContent = { Text("Help & Support") },
                supportingContent = { Text("Get help with the app") },
                leadingContent = {
                    Icon(Icons.Default.Help, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                }
            )
            
            ListItem(
                headlineContent = { Text("Terms of Service") },
                leadingContent = {
                    Icon(Icons.Default.Description, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                }
            )
            
            Divider(Modifier.padding(vertical = 8.dp))
            
            // Logout Button
            ListItem(
                headlineContent = {
                    Text(
                        "Logout",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Logout,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { onLogout() }
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
