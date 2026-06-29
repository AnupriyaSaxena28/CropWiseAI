package com.cropwise.app.util

/** App-wide constants mirroring the website's supported regions/languages/options. */
object Constants {

    val LANGUAGES = listOf(
        "en" to "English",
        "hi" to "हिंदी (Hindi)",
        "pa" to "ਪੰਜਾਬੀ (Punjabi)",
        "mr" to "मराठी (Marathi)",
        "te" to "తెలుగు (Telugu)",
        "ta" to "தமிழ் (Tamil)",
    )

    /** Maps app language code to Sarvam BCP-47 code. */
    val SARVAM_LANG = mapOf(
        "en" to "en-IN", "hi" to "hi-IN", "pa" to "pa-IN",
        "mr" to "mr-IN", "te" to "te-IN", "ta" to "ta-IN",
    )

    val STATES = listOf(
        "Punjab", "Haryana", "Uttar Pradesh", "Madhya Pradesh", "Rajasthan",
        "Maharashtra", "Gujarat", "Karnataka", "Andhra Pradesh", "Telangana",
        "Tamil Nadu", "Bihar", "Odisha", "West Bengal", "Chhattisgarh",
    )

    val SOIL_TYPES = listOf("Alluvial", "Black (Regur)", "Red", "Laterite", "Sandy", "Clay", "Loamy")
    val SEASONS = listOf("Kharif", "Rabi", "Zaid")
    val WATER_SOURCES = listOf("Canal", "Tube Well", "Borewell", "Rain-fed", "Drip Irrigation", "River")
    val ACTIVITY_TYPES = listOf("irrigation", "fertilizer", "pesticide", "weeding", "sowing", "harvesting")

    val MARKET_CROPS = listOf("Wheat", "Rice (Paddy)", "Maize", "Soybean", "Cotton", "Mustard", "Groundnut", "Tur (Arhar)")
}
