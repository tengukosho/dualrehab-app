package com.rehab.platform.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rehab.platform.data.model.Schedule
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onVideoClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // FIXED: Show upcoming schedules (today onwards)
    val schedulesForDay = remember(uiState.selectedDate, uiState.schedules) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val selected = Calendar.getInstance().apply {
            time = uiState.selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If selected date is today or future, show schedules for that day
        // If selected date is past, show all upcoming schedules
        if (selected.before(today)) {
            viewModel.getUpcomingSchedules()
        } else {
            viewModel.getSchedulesForSelectedDate()
        }
    }
    
    // Auto-select today if selected date is in the past
    LaunchedEffect(uiState.selectedDate) {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val selected = Calendar.getInstance()
        selected.time = uiState.selectedDate
        selected.set(Calendar.HOUR_OF_DAY, 0)
        selected.set(Calendar.MINUTE, 0)
        selected.set(Calendar.SECOND, 0)
        selected.set(Calendar.MILLISECOND, 0)
        
        if (selected.before(today)) {
            viewModel.selectDate(today.time)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.schedules.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Week Calendar (only show today onwards)
                WeekCalendar(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.selectDate(it) },
                    schedules = uiState.schedules
                )
                
                Divider()
                
                // Schedules for selected day
                if (schedulesForDay.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No exercises scheduled",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "for ${formatDate(uiState.selectedDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                formatDate(uiState.selectedDate),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        
                        items(schedulesForDay) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                onVideoClick = onVideoClick,
                                onMarkCompleted = { viewModel.markScheduleCompleted(schedule.id) },
                                onDelete = { viewModel.deleteSchedule(schedule.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekCalendar(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    schedules: List<Schedule>
) {
    // Generate dates starting from today for the next 7 days
    val weekDates = remember {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        (0..6).map { dayOffset ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.DAY_OF_MONTH, dayOffset)
            calendar.time
        }
    }
    
    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(weekDates) { date ->
            DayCell(
                date = date,
                isSelected = isSameDay(date, selectedDate),
                isToday = isSameDay(date, Date()),
                hasSchedule = schedules.any { schedule ->
                    schedule.scheduledDate.startsWith(formatDateForComparison(date))
                },
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DayCell(
    date: Date,
    isSelected: Boolean,
    isToday: Boolean,
    hasSchedule: Boolean,
    onClick: () -> Unit
) {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .width(56.dp)
    ) {
        Text(
            dayFormat.format(date),
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Spacer(Modifier.height(4.dp))
        Text(
            dateFormat.format(date),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        
        // Show "Today" label
        if (isToday && !isSelected) {
            Spacer(Modifier.height(2.dp))
            Text(
                "Today",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        if (hasSchedule) {
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: Schedule,
    onVideoClick: (Int) -> Unit,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable { onVideoClick(schedule.videoId) }
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (schedule.completed) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (schedule.completed)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    schedule.video?.title ?: "Exercise",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            formatTime(schedule.scheduledDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (schedule.video?.duration != null) {
                        Text(
                            "${schedule.video.duration / 60}:${
                                String.format(
                                    "%02d",
                                    schedule.video.duration % 60
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (!schedule.completed) {
                IconButton(onClick = onMarkCompleted) {
                    Icon(Icons.Default.CheckCircle, "Mark completed")
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun formatDate(date: Date): String {
    val format = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    return format.format(date)
}

fun formatDateForComparison(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}

fun formatTime(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = parser.parse(dateString)
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
