package com.cropwise.app.data.remote

import com.cropwise.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    // Profile
    @GET("api/profile")
    suspend fun getProfile(): UserDto

    @PUT("api/profile")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): UserDto

    // AI
    @POST("api/gemini")
    suspend fun gemini(@Body body: GeminiRequest): GeminiResponse

    // Weather
    @GET("api/weather")
    suspend fun weather(
        @Query("state") state: String? = null,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("label") label: String? = null,
    ): WeatherEnvelope

    // Market
    @GET("api/market")
    suspend fun market(
        @Query("type") type: String = "current",
        @Query("crop") crop: String = "Wheat",
        @Query("state") state: String? = null,
        @Query("district") district: String? = null,
    ): MarketEnvelope

    @GET("api/market")
    suspend fun marketHistory(
        @Query("type") type: String = "historical",
        @Query("crop") crop: String = "Wheat",
    ): HistoricalEnvelope

    // Schemes
    @GET("api/schemes")
    suspend fun schemes(): List<Scheme>

    // Chat persistence
    @GET("api/chat/history")
    suspend fun chatHistory(): List<ChatMessageDto>

    @POST("api/chat/messages")
    suspend fun saveMessage(@Body body: SaveMessageRequest): ChatMessageDto

    @DELETE("api/chat/history")
    suspend fun clearChat()

    // Activity log
    @GET("api/activities")
    suspend fun activities(): List<ActivityDto>

    @POST("api/activities")
    suspend fun createActivity(@Body body: CreateActivityRequest): ActivityDto

    @DELETE("api/activities/{id}")
    suspend fun deleteActivity(@Path("id") id: String)

    @GET("api/activities/eco-summary")
    suspend fun ecoSummary(): EcoSummary

    // Sarvam audio
    @POST("api/sarvam/tts")
    suspend fun tts(@Body body: TtsRequest): TtsResponse

    @Multipart
    @POST("api/sarvam/stt")
    suspend fun stt(
        @Part file: MultipartBody.Part,
        @Part("language_code") languageCode: okhttp3.RequestBody,
    ): SttResponse
}
