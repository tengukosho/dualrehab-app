package com.rehab.platform.repository

import com.rehab.platform.api.ApiService
import com.rehab.platform.local.AuthDataStore
import com.rehab.platform.data.model.*
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class RehabRepository(
    private val apiService: ApiService,
    private val authDataStore: AuthDataStore
) {
    
    // ============ Auth ============
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                authDataStore.saveAuthToken(authResponse.token)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        email: String,
        password: String,
        name: String,
        hospital: String?,
        phoneNumber: String?
    ): Result<AuthResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(email, password, name, "patient", hospital, phoneNumber)
            )
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                authDataStore.saveAuthToken(authResponse.token)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        authDataStore.clearAuthToken()
    }
    
    fun getAuthToken(): Flow<String?> = authDataStore.authToken
    
    // ============ User ============
    
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ Categories ============
    
    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ Videos ============
    
    suspend fun getVideos(categoryId: Int? = null): Result<VideoListResponse> {
        return try {
            val response = apiService.getVideos(categoryId = categoryId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load videos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVideoById(videoId: Int): Result<Video> {
        return try {
            val response = apiService.getVideoById(videoId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load video"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ Schedules ============
    
    suspend fun getSchedules(): Result<ScheduleListResponse> {
        return try {
            val response = apiService.getSchedules()
            if (response.isSuccessful) {
                val schedules = response.body() ?: emptyList()
                Result.success(ScheduleListResponse(schedules))
            } else {
                Result.failure(Exception("Failed to load schedules"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateSchedule(scheduleId: Int, status: String): Result<Schedule> {
        return try {
            val response = apiService.completeSchedule(scheduleId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update schedule"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ Progress ============
    
    suspend fun getProgress(): Result<ProgressListResponse> {
        return try {
            val response = apiService.getProgress()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load progress"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProgressStats(): Result<ProgressStats> {
        return try {
            val response = apiService.getProgressStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markProgress(videoId: Int, rating: Int? = null, notes: String? = null): Result<UserProgress> {
        return try {
            val response = apiService.createProgress(
                CreateProgressRequest(videoId, notes, rating)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to mark progress"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProgress(videoId: Int, rating: Int? = null, notes: String? = null): Result<Unit> {
        return try {
            val progressResponse = apiService.getProgress()
            if (progressResponse.isSuccessful) {
                val progress = progressResponse.body()?.progress?.find { it.videoId == videoId }
                if (progress != null) {
                    val response = apiService.updateProgress(
                        progress.id,
                        ProgressUpdateRequest(rating = rating, notes = notes)
                    )
                    if (response.isSuccessful) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Failed to update progress"))
                    }
                } else {
                    Result.failure(Exception("Progress not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch progress"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ Messages ============
    
    suspend fun getAssignedExperts(): Result<List<com.rehab.platform.data.model.ExpertInfo>> {
        return try {
            val currentUser = apiService.getCurrentUser()
            if (currentUser.isSuccessful && currentUser.body() != null) {
                val user = currentUser.body()!!
                // For now, if user has assignedExpertId, return that expert
                if (user.assignedExpertId != null) {
                    // Get expert details - you may need to add an endpoint for this
                    // For now, create a mock expert
                    val expert = com.rehab.platform.data.model.ExpertInfo(
                        id = user.assignedExpertId,
                        name = "Dr. Expert", // This should come from backend
                        email = "expert@rehab.com",
                        hospital = user.hospital,
                        unreadCount = 0
                    )
                    Result.success(listOf(expert))
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("Failed to get user info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getConversation(expertId: Int): Result<List<Message>> {
        return try {
            val response = apiService.getConversation(expertId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load conversation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(recipientId: Int, message: String): Result<Message> {
        return try {
            val response = apiService.sendMessage(
                SendMessageRequest(recipientId, message)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUnreadCount(): Result<UnreadCount> {
        return try {
            val response = apiService.getUnreadCount()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get unread count"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ============ Schedule Management ============
    
    suspend fun createSchedule(videoId: Int, scheduledDate: String, notes: String? = null): Result<Schedule> {
        return try {
            val response = apiService.createSchedule(
                CreateScheduleRequest(videoId, scheduledDate)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create schedule"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteSchedule(scheduleId: Int): Result<Unit> {
        return try {
            val response = apiService.deleteSchedule(scheduleId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete schedule"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ Share Video ============
    
    suspend fun shareVideoToExpert(videoId: Int, expertId: Int, message: String): Result<Message> {
        return try {
            val videoMessage = "Check out this exercise: Video #$videoId - $message"
            sendMessage(expertId, videoMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ User Profile ============
    
    suspend fun updateProfile(name: String?, email: String?, phoneNumber: String?, hospital: String?): Result<User> {
        return try {
            val updates = mutableMapOf<String, String>()
            name?.let { updates["name"] = it }
            email?.let { updates["email"] = it }
            phoneNumber?.let { updates["phoneNumber"] = it }
            hospital?.let { updates["hospital"] = it }
            
            val response = apiService.updateProfile(updates)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
