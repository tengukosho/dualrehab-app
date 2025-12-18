package com.rehab.platform.video

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var showContactExpertDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    // Playback states
    var playbackSpeed by remember { mutableStateOf(1f) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = false

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        duration = this@apply.duration
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }

    // Update current position
    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            kotlinx.coroutines.delay(100)
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

    // Change playback speed
    LaunchedEffect(playbackSpeed) {
        exoPlayer.setPlaybackSpeed(playbackSpeed)
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
                    // Download button
                    IconButton(onClick = { viewModel.downloadVideo() }) {
                        Icon(
                            if (uiState.isDownloaded) Icons.Default.CloudDone else Icons.Default.Download,
                            "Download for offline"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Video Player
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Download progress overlay
                    if (uiState.isDownloading) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = uiState.downloadProgress / 100f,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    "Downloading ${uiState.downloadProgress}%",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // Play/Pause overlay
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) exoPlayer.pause()
                                else exoPlayer.play()
                            },
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Player controls at bottom
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skip backward
                        IconButton(onClick = {
                            exoPlayer.seekTo(maxOf(0, exoPlayer.currentPosition - 10000))
                        }) {
                            Icon(Icons.Default.Replay10, "Backward 10s", tint = Color.White)
                        }

                        // Skip forward
                        IconButton(onClick = {
                            exoPlayer.seekTo(minOf(duration, exoPlayer.currentPosition + 10000))
                        }) {
                            Icon(Icons.Default.Forward10, "Forward 10s", tint = Color.White)
                        }

                        // Playback speed
                        OutlinedButton(
                            onClick = { showSpeedDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("${playbackSpeed}x")
                        }
                    }
                }

                // Progress indicator
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatTime(currentPosition))
                            Text(formatTime(duration))
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Video Details
                Column(Modifier.padding(16.dp)) {
                    Text(
                        uiState.video?.title ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Chip(
                            onClick = { },
                            label = { Text(uiState.video?.category?.name ?: "") }
                        )
                        Text(
                            "${uiState.video?.duration ?: 0} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Chip(
                            onClick = { },
                            label = { Text(uiState.video?.difficultyLevel ?: "beginner") }
                        )
                    }

                    if (uiState.video?.description != null) {
                        Spacer(Modifier.height(16.dp))
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
                    }

                    if (uiState.video?.instructions != null) {
                        Spacer(Modifier.height(16.dp))
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
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action Buttons
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

                    OutlinedButton(
                        onClick = { showScheduleDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add to Schedule")
                    }

                    Spacer(Modifier.height(8.dp))

                    // Contact Expert Button
                    OutlinedButton(
                        onClick = { showContactExpertDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Contact Expert")
                    }
                }
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed") },
            text = {
                Column {
                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                        TextButton(
                            onClick = {
                                playbackSpeed = speed
                                showSpeedDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "${speed}x",
                                fontWeight = if (speed == playbackSpeed) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Mark as Completed Dialog
    if (showCompletedDialog) {
        AlertDialog(
            onDismissRequest = { showCompletedDialog = false },
            title = { Text("Mark as Completed") },
            text = {
                Column {
                    Text("Add any notes about this exercise (optional):")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = completionNotes,
                        onValueChange = { completionNotes = it },
                        placeholder = { Text("Notes...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.markAsCompleted(completionNotes)
                    showCompletedDialog = false
                }) {
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

    // Contact Expert Dialog
    if (showContactExpertDialog) {
        var messageText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showContactExpertDialog = false },
            icon = { Icon(Icons.Default.Message, "Contact Expert") },
            title = { Text("Contact Expert About This Video") },
            text = {
                Column {
                    Text("Send a message about: ${uiState.video?.title}")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type your question or concern...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Get expert ID from current user and send using shareVideo
                        messagesUiState.currentUser?.assignedExpertId?.let { expertId ->
                            val videoMessage = "Question about video '${uiState.video?.title}': $messageText"
                            messagesViewModel.shareVideo(
                                videoId = uiState.video?.id ?: 0,
                                contactId = expertId,
                                message = messageText
                            )
                        }
                        showContactExpertDialog = false
                        messageText = ""
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showContactExpertDialog = false
                    messageText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun Chip(onClick: () -> Unit, label: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Box(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                label()
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}