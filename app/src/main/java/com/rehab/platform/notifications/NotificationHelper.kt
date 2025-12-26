package com.rehab.platform.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rehab.platform.MainActivity
import com.rehab.platform.R

class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID_SCHEDULE = "rehab_schedule"
        const val CHANNEL_ID_PROGRESS = "rehab_progress"
        const val CHANNEL_ID_MESSAGES = "rehab_messages"
        
        const val CHANNEL_NAME_SCHEDULE = "Exercise Reminders"
        const val CHANNEL_NAME_PROGRESS = "Progress Updates"
        const val CHANNEL_NAME_MESSAGES = "Messages"
        
        const val NOTIFICATION_ID_SCHEDULE = 1001
        const val NOTIFICATION_ID_PROGRESS = 1002
        const val NOTIFICATION_ID_MESSAGE = 1003
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_SCHEDULE,
                    CHANNEL_NAME_SCHEDULE,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for scheduled exercises"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        null
                    )
                },
                
                NotificationChannel(
                    CHANNEL_ID_PROGRESS,
                    CHANNEL_NAME_PROGRESS,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Progress milestones and achievements"
                    enableVibration(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_MESSAGES,
                    CHANNEL_NAME_MESSAGES,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "New messages from experts"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                }
            )
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }
    
    /**
     * Schedule exercise reminder notification
     */
    fun scheduleExerciseReminder(
        scheduleId: Int,
        title: String,
        videoTitle: String,
        time: Long
    ) {
        val intent = Intent(context, ExerciseReminderReceiver::class.java).apply {
            putExtra("scheduleId", scheduleId)
            putExtra("title", title)
            putExtra("videoTitle", videoTitle)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }
    
    /**
     * Cancel scheduled notification
     */
    fun cancelScheduledNotification(scheduleId: Int) {
        val intent = Intent(context, ExerciseReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * Show progress milestone notification
     */
    fun showProgressMilestone(milestone: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "progress")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_PROGRESS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROGRESS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ðŸŽ‰ $milestone")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_PROGRESS,
            notification
        )
    }
    
    /**
     * Show new message notification
     */
    fun showNewMessage(senderId: Int, senderName: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "messages")
            putExtra("sender_id", senderId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_MESSAGE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New message from $senderName")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_MESSAGE + senderId, // Unique ID per sender
            notification
        )
    }
    
    /**
     * Show daily motivation notification
     */
    fun showDailyMotivation() {
        val motivationalMessages = listOf(
            "Time for your daily exercise! Your body will thank you! ðŸ’ª",
            "Don't forget your rehabilitation exercises today!",
            "Consistency is key! Let's complete today's exercises!",
            "Your progress is amazing! Keep it up!",
            "A little progress each day adds up to big results!"
        )
        
        val message = motivationalMessages.random()
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "home")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SCHEDULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(9999, notification)
    }
}

/**
 * Receiver for exercise reminder notifications
 */
class ExerciseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("scheduleId", 0)
        val title = intent.getStringExtra("title") ?: "Exercise Reminder"
        val videoTitle = intent.getStringExtra("videoTitle") ?: "Your scheduled exercise"
        
        // Create intent to open video
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "video")
            putExtra("schedule_id", scheduleId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create "Mark Complete" action
        val completeIntent = Intent(context, MarkCompleteReceiver::class.java).apply {
            putExtra("schedule_id", scheduleId)
        }
        
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId + 10000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create "Snooze" action
        val snoozeIntent = Intent(context, SnoozeReminderReceiver::class.java).apply {
            putExtra("schedule_id", scheduleId)
            putExtra("title", title)
            putExtra("videoTitle", videoTitle)
        }
        
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId + 20000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_SCHEDULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(videoTitle)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time for: $videoTitle\n\nTap to start your exercise!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Mark Complete",
                completePendingIntent
            )
            .addAction(
                R.drawable.ic_snooze,
                "Snooze 15min",
                snoozePendingIntent
            )
            .build()
        
        NotificationManagerCompat.from(context).notify(
            NotificationHelper.NOTIFICATION_ID_SCHEDULE + scheduleId,
            notification
        )
    }
}

/**
 * Receiver for "Mark Complete" action
 */
class MarkCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("schedule_id", 0)
        
        // Cancel the notification
        NotificationManagerCompat.from(context).cancel(
            NotificationHelper.NOTIFICATION_ID_SCHEDULE + scheduleId
        )
        
        // TODO: Call API to mark schedule as completed
        // This would require injecting repository or using WorkManager
    }
}

/**
 * Receiver for "Snooze" action
 */
class SnoozeReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("schedule_id", 0)
        val title = intent.getStringExtra("title") ?: "Exercise Reminder"
        val videoTitle = intent.getStringExtra("videoTitle") ?: "Your scheduled exercise"
        
        // Cancel current notification
        NotificationManagerCompat.from(context).cancel(
            NotificationHelper.NOTIFICATION_ID_SCHEDULE + scheduleId
        )
        
        // Reschedule for 15 minutes later
        val helper = NotificationHelper(context)
        helper.scheduleExerciseReminder(
            scheduleId,
            title,
            videoTitle,
            System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes
        )
    }
}
