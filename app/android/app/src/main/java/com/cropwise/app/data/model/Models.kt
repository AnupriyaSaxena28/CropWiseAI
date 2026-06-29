package com.cropwise.app.data.model

import com.google.gson.annotations.SerializedName

// ── Auth / User ──
data class RegisterRequest(val email: String, val password: String, val name: String? = null)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val user: UserDto)

data class UserDto(
    val id: String,
    val email: String,
    val name: String? = null,
    val phone: String? = null,
    val state: String? = null,
    val district: String? = null,
    val soilType: String? = null,
    val preferredLanguage: String? = "en",
    val landAreaAcres: Double? = null,
    val waterSource: String? = null,
    val primaryCrop: String? = null,
)

data class ProfileUpdateRequest(
    val name: String? = null,
    val phone: String? = null,
    val state: String? = null,
    val district: String? = null,
    val soilType: String? = null,
    val preferredLanguage: String? = null,
    val landAreaAcres: Double? = null,
    val waterSource: String? = null,
    val primaryCrop: String? = null,
)

// ── Gemini AI ──
data class HistoryItem(val role: String, val content: String)
data class GeminiRequest(
    val prompt: String,
    val mode: String,                 // chat | pest_diagnosis | crop_advisor
    val language: String? = "en",
    val imageBase64: String? = null,
    val imageMimeType: String? = null,
    val context: String? = null,
    val history: List<HistoryItem>? = null,
)
data class GeminiResponse(
    val success: Boolean,
    val text: String? = null,
    val structured: Map<String, Any?>? = null,
    val error: String? = null,
)

// ── Weather ──
data class WeatherEnvelope(val success: Boolean, val data: WeatherData? = null, val error: String? = null)
data class WeatherData(
    val location: String,
    val temperature: Int,
    val feelsLike: Int,
    val humidity: Int,
    val windSpeed: Int,
    val condition: String,
    val conditionCode: String,
    val precipitation: Double,
    val uvIndex: Double,
    val visibility: Int,
    val forecast: List<ForecastDay> = emptyList(),
    val updatedAt: String,
    val soilMoisture: Int,
    val soilMoistureRaw: Double,
    val evapotranspiration: Double,
    val solarRadiation: Double,
    val soilTemp: Int,
    val hourlyTemp: List<Double> = emptyList(),
    val hourlyRain: List<Double> = emptyList(),
    val hourlyTime: List<String> = emptyList(),
)
data class ForecastDay(
    val date: String,
    val dayName: String,
    val high: Int,
    val low: Int,
    val condition: String,
    val conditionCode: String,
    val precipitationChance: Int,
)

// ── Market ──
data class MarketEnvelope(val success: Boolean, val data: List<MarketPrice> = emptyList())
data class HistoricalEnvelope(val success: Boolean, val data: List<HistoricalPoint> = emptyList())
data class MarketPrice(
    val cropName: String,
    val cropNameHi: String? = null,
    val msp: Int,
    val currentPrice: Int,
    val priceChange: Int,
    val priceChangePercent: Double,
    val unit: String,
    val market: String,
    val lastUpdated: String,
)
data class HistoricalPoint(val date: String, val price: Int, val msp: Int)

// ── Schemes ──
data class Scheme(
    val id: String,
    val name: String,
    val ministry: String,
    val description: String,
    val benefits: List<String> = emptyList(),
    val eligibility: List<String> = emptyList(),
    val applicationUrl: String,
    val deadline: String? = null,
    val isActive: Boolean = true,
)

// ── Chat persistence ──
data class SaveMessageRequest(val role: String, val content: String, val sessionId: String? = null)
data class ChatMessageDto(
    val id: String,
    val role: String,
    val content: String,
    val sessionId: String? = null,
    val createdAt: String,
)

// ── Activity log ──
data class CreateActivityRequest(
    val type: String,
    val title: String? = null,
    val notes: String? = null,
    val cost: Double? = null,
    val date: String? = null,
)
data class ActivityDto(
    val id: String,
    val type: String,
    val title: String? = null,
    val notes: String? = null,
    val cost: Double? = null,
    val date: String,
    val createdAt: String,
)
data class EcoSummary(
    val ecoScore: Int,
    val carbonSaved: Int,
    val waterSaved: Int,
    val monthlySpend: Double,
)

// ── Sarvam ──
data class TtsRequest(val text: String, val targetLanguageCode: String? = null, val speaker: String? = null)
data class TtsResponse(val audioBase64: String? = null, val error: String? = null)
data class SttResponse(val transcript: String? = null, val languageCode: String? = null)
