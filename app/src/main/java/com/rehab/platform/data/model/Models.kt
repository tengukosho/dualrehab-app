package com.rehab.platform.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Auth Models
@Parcelize
data class LoginRequest(
    val email: String,
    val password: String
) : Parcelable

@Parcelize
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: String = "patient",
    val hospital: String? = null,
    val phoneNumber: String? = null
) : Parcelable

@Parcelize
data class AuthResponse(
    val user: User,
    val token: String
) : Parcelable

// User Model
@Parcelize
data class User(
    val id: Int,
    val email: String,
    val name: String,
    val role: String,
    val hospital: String? = null,
    val medicalRecordNo: String? = null,
    val phoneNumber: String? = null,
    val assignedExpertId: Int? = null,
    val createdAt: String? = null
) : Parcelable

// Category Model
@Parcelize
data class Category(
    val id: Int,
    val name: String,
    val description: String? = null,
    val order: Int = 0,
    val _count: CategoryCount? = null
) : Parcelable

@Parcelize
data class CategoryCount(
    val videos: Int
) : Parcelable

// Video Model
@Parcelize
data class Video(
    val id: Int,
    val title: String,
    val description: String? = null,
    val categoryId: Int,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val duration: Int,
    val difficultyLevel: String = "beginner",
    val instructions: String? = null,
    val uploadDate: String? = null,
    val category: CategoryBasic? = null
) : Parcelable

@Parcelize
data class CategoryBasic(
    val id: Int,
    val name: String
) : Parcelable

// Schedule Model
@Parcelize
data class Schedule(
    val id: Int,
    val userId: Int,
    val videoId: Int,
    val scheduledDate: String,
    val completed: Boolean = false,
    val completedAt: String? = null,
    val video: Video? = null
) : Parcelable

@Parcelize
data class CreateScheduleRequest(
    val videoId: Int,
    val scheduledDate: String
) : Parcelable

// Progress Model
@Parcelize
data class UserProgress(
    val id: Int,
    val userId: Int,
    val videoId: Int,
    val completionDate: String,
    val notes: String? = null,
    val rating: Int? = null,
    val video: Video? = null
) : Parcelable

@Parcelize
data class CreateProgressRequest(
    val videoId: Int,
    val notes: String? = null,
    val rating: Int? = null
) : Parcelable

@Parcelize
data class ProgressStats(
    val totalCompleted: Int,
    val completedLast7Days: Int,
    val completedLast30Days: Int,
    val uniqueVideosCompleted: Int,
    val currentStreak: Int
) : Parcelable

// Message Model
@Parcelize
data class Message(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: String,
    val sender: UserBasic? = null,
    val receiver: UserBasic? = null
) : Parcelable

@Parcelize
data class UserBasic(
    val id: Int,
    val name: String,
    val role: String
) : Parcelable

@Parcelize
data class SendMessageRequest(
    val receiverId: Int,
    val message: String
) : Parcelable

@Parcelize
data class UnreadCount(
    val count: Int
) : Parcelable

// Paginated Response
@Parcelize
data class PaginatedResponse<T : Parcelable>(
    val items: List<T>,
    val pagination: Pagination
) : Parcelable

@Parcelize
data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
) : Parcelable

// Video List Response
data class VideoListResponse(
    val videos: List<Video>,
    val pagination: Pagination
)

// Schedule List Response
data class ScheduleListResponse(
    val schedules: List<Schedule>
)

// Progress List Response
data class ProgressListResponse(
    val progress: List<UserProgress>,
    val pagination: Pagination
)

// Message List Response
data class MessageListResponse(
    val messages: List<Message>,
    val pagination: Pagination
)

// Error Response
data class ErrorResponse(
    val error: String
)

// Progress Update Request
@Parcelize
data class ProgressUpdateRequest(
    val rating: Int? = null,
    val notes: String? = null
) : Parcelable

// Unread Count Response
@Parcelize
data class UnreadCountResponse(
    val count: Int
) : Parcelable

// Notification Settings
@Parcelize
data class NotificationSettings(
    val enabled: Boolean = true,
    val scheduleReminders: Boolean = true,
    val messageNotifications: Boolean = true,
    val systemAnnouncements: Boolean = true
) : Parcelable

// Privacy Settings
@Parcelize
data class PrivacySettings(
    val shareProgressWithExpert: Boolean = true,
    val allowExpertMessages: Boolean = true
) : Parcelable

// System Message
@Parcelize
data class SystemMessage(
    val id: Int,
    val title: String,
    val message: String,
    val createdAt: String,
    val isRead: Boolean = false
) : Parcelable

// Expert Info for messaging
data class ExpertInfo(
    val id: Int,
    val name: String,
    val email: String,
    val role: String = "expert",
    val hospital: String? = null,
    val unreadCount: Int = 0
)
