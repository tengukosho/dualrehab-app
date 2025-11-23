package com.rehab.platform.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rehab.platform.data.model.UserProgress
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
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
            LazyColumn(
                Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Cards
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.stats != null) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Main stats card
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                CompactStatItem(
                                    icon = Icons.Default.CheckCircle,
                                    value = "${uiState.stats?.totalCompleted ?: 0}",
                                    label = "Completed"
                                )
                                VerticalDivider()
                                CompactStatItem(
                                    icon = Icons.Default.LocalFireDepartment,
                                    value = "${uiState.stats?.currentStreak ?: 0}",
                                    label = "Day Streak"
                                )
                            }
                            
                            Divider(Modifier.padding(vertical = 16.dp))
                            
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                CompactStatItem(
                                    icon = Icons.Default.CalendarToday,
                                    value = "${uiState.stats?.completedLast7Days ?: 0}",
                                    label = "This Week"
                                )
                                VerticalDivider()
                                CompactStatItem(
                                    icon = Icons.Default.VideoLibrary,
                                    value = "${uiState.stats?.uniqueVideosCompleted ?: 0}",
                                    label = "Unique Videos"
                                )
                            }
                        }
                    }
                }
                
                // Weekly Activity Chart
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Weekly Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                            WeeklyActivityChart(uiState.recentProgress)
                        }
                    }
                }
                
                // Progress Distribution
                if (uiState.stats != null && uiState.stats!!.totalCompleted > 0) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Progress Overview",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(16.dp))
                                ProgressPieChart(uiState.stats!!)
                            }
                        }
                    }
                }
                
                // Recent Progress
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(uiState.recentProgress.take(10)) { progress ->
                    ProgressCard(progress)
                }
                
                if (uiState.recentProgress.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Box(
                                Modifier.padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "No progress yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Complete your first exercise!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(80.dp)
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
    )
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklyActivityChart(recentProgress: List<UserProgress>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    // Get last 7 days data
    val calendar = Calendar.getInstance()
    val last7Days = (0..6).map {
        calendar.add(Calendar.DAY_OF_YEAR, if (it == 0) 0 else -1)
        calendar.time
    }.reversed()
    
    // Count exercises per day
    val exercisesPerDay = last7Days.map { date ->
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        recentProgress.count { progress ->
            progress.completionDate.startsWith(dateStr)
        }
    }
    
    val maxExercises = exercisesPerDay.maxOrNull() ?: 1
    val dayLabels = last7Days.map { 
        SimpleDateFormat("EEE", Locale.getDefault()).format(it)
    }
    
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val barWidth = size.width / (exercisesPerDay.size * 2f)
            val maxBarHeight = size.height - 40f
            
            exercisesPerDay.forEachIndexed { index, count ->
                val barHeight = if (maxExercises > 0) {
                    (count.toFloat() / maxExercises) * maxBarHeight
                } else 0f
                
                val x = index * (barWidth * 2) + barWidth
                val y = size.height - barHeight
                
                // Draw bar
                drawRect(
                    color = if (count > 0) primaryColor else surfaceVariant,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
            }
        }
        
        // Day labels
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Text(
            "Exercises completed per day",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProgressPieChart(stats: com.rehab.platform.data.model.ProgressStats) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    val last7Days = stats.completedLast7Days.toFloat()
    val last30Days = (stats.completedLast30Days - stats.completedLast7Days).toFloat()
    val older = (stats.totalCompleted - stats.completedLast30Days).toFloat()
    val total = stats.totalCompleted.toFloat()
    
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pie chart
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
        ) {
            var startAngle = -90f
            
            if (total > 0) {
                // Last 7 days
                val angle7Days = (last7Days / total) * 360f
                drawArc(
                    color = primaryColor,
                    startAngle = startAngle,
                    sweepAngle = angle7Days,
                    useCenter = true,
                    size = size
                )
                startAngle += angle7Days
                
                // Last 30 days (excluding 7 days)
                if (last30Days > 0) {
                    val angle30Days = (last30Days / total) * 360f
                    drawArc(
                        color = secondaryColor,
                        startAngle = startAngle,
                        sweepAngle = angle30Days,
                        useCenter = true,
                        size = size
                    )
                    startAngle += angle30Days
                }
                
                // Older
                if (older > 0) {
                    val angleOlder = (older / total) * 360f
                    drawArc(
                        color = tertiaryColor,
                        startAngle = startAngle,
                        sweepAngle = angleOlder,
                        useCenter = true,
                        size = size
                    )
                }
                
                // Center circle (donut style)
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension / 3f,
                    center = center
                )
            }
        }
        
        // Legend
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LegendItem(primaryColor, "Last 7 days", stats.completedLast7Days)
            LegendItem(secondaryColor, "Last 30 days", stats.completedLast30Days - stats.completedLast7Days)
            LegendItem(tertiaryColor, "Older", stats.totalCompleted - stats.completedLast30Days)
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: Int) {
    if (value > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = color)
            }
            Text(
                "$label: $value",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ProgressCard(progress: UserProgress) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    progress.video?.title ?: "Exercise",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatDate(progress.completionDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (progress.notes != null) {
                    Text(
                        progress.notes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (progress.rating != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "${progress.rating}/5",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val date = parser.parse(dateString)
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
