package com.rehab.platform.schedule

import android.content.Context
import com.rehab.platform.data.model.Schedule
import com.rehab.platform.notifications.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

object ScheduleNotificationManager {
    
    fun scheduleNotifications(context: Context, schedules: List<Schedule>) {
        val notificationHelper = NotificationHelper(context)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val now = System.currentTimeMillis()
        
        schedules.forEach { schedule ->
            if (!schedule.completed) {
                try {
                    val scheduledTime = dateFormat.parse(schedule.scheduledDate)?.time ?: 0
                    
                    if (scheduledTime > now) {
                        notificationHelper.scheduleExerciseReminder(
                            scheduleId = schedule.id,
                            title = "Exercise Reminder",
                            videoTitle = schedule.video?.title ?: "Your scheduled exercise",
                            time = scheduledTime
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun cancelNotification(context: Context, scheduleId: Int) {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.cancelScheduledNotification(scheduleId)
    }
}
