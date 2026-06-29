package com.cropwise.app.data.repository

import com.cropwise.app.data.local.SessionStore
import com.cropwise.app.data.model.*
import com.cropwise.app.data.remote.ApiService
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Single entry point to the backend. Holds the JWT in memory (mirrored to
 * SessionStore) and exposes suspend functions for every feature.
 */
class CropWiseRepository(
    private val api: ApiService,
    private val session: SessionStore,
) {
    @Volatile var token: String? = null
        private set

    suspend fun bootstrap() { token = session.token.first() }

    val isLoggedIn: Boolean get() = !token.isNullOrBlank()

    // ── Auth ──
    suspend fun register(email: String, password: String, name: String?): AuthResponse {
        val res = api.register(RegisterRequest(email, password, name))
        token = res.token
        session.saveSession(res.token, res.user.id, res.user.preferredLanguage)
        return res
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val res = api.login(LoginRequest(email, password))
        token = res.token
        session.saveSession(res.token, res.user.id, res.user.preferredLanguage)
        return res
    }

    suspend fun logout() { token = null; session.clear() }

    suspend fun setLanguage(lang: String) = session.setLanguage(lang)

    // ── Profile ──
    suspend fun profile(): UserDto = api.getProfile()
    suspend fun updateProfile(req: ProfileUpdateRequest): UserDto = api.updateProfile(req)

    // ── AI ──
    suspend fun gemini(req: GeminiRequest): GeminiResponse = api.gemini(req)

    // ── Weather ──
    suspend fun weather(state: String?, lat: Double?, lng: Double?, label: String?): WeatherData? =
        api.weather(state, lat, lng, label).data

    // ── Market ──
    suspend fun market(state: String?, district: String?): List<MarketPrice> =
        api.market(state = state, district = district).data
    suspend fun marketHistory(crop: String): List<HistoricalPoint> = api.marketHistory(crop = crop).data

    // ── Schemes ──
    suspend fun schemes(): List<Scheme> = api.schemes()

    // ── Chat persistence ──
    suspend fun chatHistory(): List<ChatMessageDto> = api.chatHistory()
    suspend fun saveMessage(role: String, content: String, sessionId: String?) =
        api.saveMessage(SaveMessageRequest(role, content, sessionId))
    suspend fun clearChat() = api.clearChat()

    // ── Activity ──
    suspend fun activities(): List<ActivityDto> = api.activities()
    suspend fun createActivity(req: CreateActivityRequest): ActivityDto = api.createActivity(req)
    suspend fun deleteActivity(id: String) = api.deleteActivity(id)
    suspend fun ecoSummary(): EcoSummary = api.ecoSummary()

    // ── Sarvam audio ──
    suspend fun tts(text: String, langCode: String?): TtsResponse =
        api.tts(TtsRequest(text, langCode, "priya"))

    suspend fun stt(audio: ByteArray, languageCode: String): SttResponse {
        val part = MultipartBody.Part.createFormData(
            "file", "recording.wav",
            audio.toRequestBody("audio/wav".toMediaTypeOrNull())
        )
        val lang: RequestBody = languageCode.toRequestBody("text/plain".toMediaTypeOrNull())
        return api.stt(part, lang)
    }
}
