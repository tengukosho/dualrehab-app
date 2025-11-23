package com.rehab.platform.api

import com.rehab.platform.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ============ Authentication ============
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    // ============ User ============
    
    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>
    
    @PUT("users/me")
    suspend fun updateProfile(@Body user: Map<String, String>): Response<User>
    
    // ============ Categories ============
    
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>
    
    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Int): Response<Category>
    
    // ============ Videos ============
    
    @GET("videos")
    suspend fun getVideos(
        @Query("categoryId") categoryId: Int? = null,
        @Query("difficultyLevel") difficultyLevel: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<VideoListResponse>
    
    @GET("videos/{id}")
    suspend fun getVideoById(@Path("id") id: Int): Response<Video>
    
    // ============ Schedules ============
    
    @GET("schedules")
    suspend fun getSchedules(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("completed") completed: Boolean? = null
    ): Response<List<Schedule>>
    
    @GET("schedules/{id}")
    suspend fun getScheduleById(@Path("id") id: Int): Response<Schedule>
    
    @POST("schedules")
    suspend fun createSchedule(@Body request: CreateScheduleRequest): Response<Schedule>
    
    @PUT("schedules/{id}/complete")
    suspend fun completeSchedule(@Path("id") id: Int): Response<Schedule>
    
    @DELETE("schedules/{id}")
    suspend fun deleteSchedule(@Path("id") id: Int): Response<Map<String, String>>
    
    // ============ Progress ============
    
    @GET("progress")
    suspend fun getProgress(
        @Query("videoId") videoId: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ProgressListResponse>
    
    @GET("progress/stats")
    suspend fun getProgressStats(): Response<ProgressStats>
    
    @POST("progress")
    suspend fun createProgress(@Body request: CreateProgressRequest): Response<UserProgress>
    
    @PUT("progress/{id}")
    suspend fun updateProgress(
        @Path("id") id: Int,
        @Body request: ProgressUpdateRequest
    ): Response<UserProgress>
    
    // ============ Messages ============
    
    @GET("messages")
    suspend fun getMessages(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<MessageListResponse>
    
    @GET("messages/conversation/{userId}")
    suspend fun getConversation(@Path("userId") userId: Int): Response<List<Message>>
    
    @GET("messages/unread/count")
    suspend fun getUnreadCount(): Response<UnreadCount>
    
    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<Message>
    
    @PUT("messages/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): Response<Message>
    
    @PUT("messages/conversation/{userId}/read-all")
    suspend fun markAllAsRead(@Path("userId") userId: Int): Response<Map<String, String>>
    
    @DELETE("messages/{id}")
    suspend fun deleteMessage(@Path("id") id: Int): Response<Map<String, String>>
}
