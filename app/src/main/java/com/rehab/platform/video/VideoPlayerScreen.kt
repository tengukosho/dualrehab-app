package com.rehab.platform.video

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.rehab.platform.BuildConfig
import com.rehab.platform.components.AddToScheduleDialog
import com.rehab.platform.messages.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    viewModel: VideoPlayerViewModel,
    messagesViewModel: MessagesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val messagesUiState by messagesViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showCompletedDialog by remember { mutableStateOf(false) }
    var completionNotes by remember { mutableStateOf("") }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = false
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    LaunchedEffect(uiState.video) {
        uiState.video?.let { video ->
            val videoUrl = "${BuildConfig.BASE_URL.replace("/api/", "")}${video.videoUrl}"
            val mediaItem = MediaItem.fromUri(videoUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.video?.title ?: "Video Player") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, "Share to Expert/Admin")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.video == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Video not found", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Video Player
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = exoPlayer
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            useController = true
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black)
                )
                
                // Video Info
                Column(Modifier.padding(16.dp)) {
                    Text(
                        uiState.video!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    uiState.video!!.difficultyLevel.replaceFirstChar { it.uppercase() }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        
                        Text(
                            "${uiState.video!!.duration / 60}:${
                                String.format(
                                    "%02d",
                                    uiState.video!!.duration % 60
                                )
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    if (uiState.video!!.description != null) {
                        Text(
                            "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.video!!.description!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    if (uiState.video!!.instructions != null) {
                        Text(
                            "Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.video!!.instructions!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    // Mark as Completed Button
                    Button(
                        onClick = { showCompletedDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isCompleted
                    ) {
                        Icon(
                            if (uiState.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (uiState.isCompleted) "Completed" else "Mark as Completed")
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Add to Schedule Button
                    OutlinedButton(
                        onClick = { showScheduleDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add to Schedule")
                    }
                }
            }
        }
    }
    
    // Schedule Dialog
    if (showScheduleDialog) {
        AddToScheduleDialog(
            onDismiss = { showScheduleDialog = false },
            onConfirm = { scheduledDateTime, notes ->
                viewModel.addToSchedule(scheduledDateTime, notes)
                showScheduleDialog = false
            },
            videoTitle = uiState.video?.title ?: ""
        )
    }
    
    // Completion Dialog
    if (showCompletedDialog) {
        AlertDialog(
            onDismissRequest = { showCompletedDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            title = { Text("Mark as Completed") },
            text = {
                Column {
                    Text("Add any notes about this exercise:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = completionNotes,
                        onValueChange = { completionNotes = it },
                        placeholder = { Text("Optional notes...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAsCompleted(completionNotes)
                        showCompletedDialog = false
                    }
                ) {
                    Text("Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Rating Dialog
    if (uiState.showRatingDialog) {
        RatingDialog(
            onDismiss = { viewModel.dismissRatingDialog() },
            onSubmit = { rating, notes ->
                viewModel.submitRating(rating, notes)
            }
        )
    }
    
    // Share Dialog
    if (showShareDialog) {
        ShareToContactDialog(
            onDismiss = { showShareDialog = false },
            contacts = messagesUiState.contacts,
            onShare = { contactId, message ->
                messagesViewModel.shareVideo(uiState.video!!.id, contactId, message)
                showShareDialog = false
            },
            videoTitle = uiState.video?.title ?: ""
        )
    }
}

@Composable
fun ShareToContactDialog(
    onDismiss: () -> Unit,
    contacts: List<com.rehab.platform.data.model.ExpertInfo>,
    onShare: (Int, String) -> Unit,
    videoTitle: String
) {
    var selectedContactId by remember { mutableIntStateOf(0) }
    var shareMessage by remember { mutableStateOf("Check out this exercise!") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Share, null) },
        title = { Text("Share Video") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Share: $videoTitle",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (contacts.isEmpty()) {
                    Text(
                        "No contacts available. Please contact your hospital to get assigned.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Contact selection
                    Text(
                        "Send to:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        contacts.forEach { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { selectedContactId = contact.id }
                                    .background(
                                        if (selectedContactId == contact.id)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedContactId == contact.id,
                                    onClick = { selectedContactId = contact.id }
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    if (contact.role == "admin") Icons.Default.AdminPanelSettings 
                                    else Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (contact.role == "admin") 
                                        MaterialTheme.colorScheme.tertiary 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        contact.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        if (contact.role == "admin") "Administrator" else "Rehabilitation Expert",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Message input
                    OutlinedTextField(
                        value = shareMessage,
                        onValueChange = { shareMessage = it },
                        label = { Text("Add a message") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedContactId > 0) {
                        onShare(selectedContactId, shareMessage)
                    }
                },
                enabled = selectedContactId > 0 && contacts.isNotEmpty()
            ) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Star, contentDescription = null) },
        title = { Text("Rate this Exercise") },
        text = {
            Column {
                Text("How was this exercise?")
                Spacer(Modifier.height(16.dp))
                
                // Star Rating
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i }
                        ) {
                            Icon(
                                if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate $i stars",
                                tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Additional feedback...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, notes) },
                enabled = rating > 0
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}
