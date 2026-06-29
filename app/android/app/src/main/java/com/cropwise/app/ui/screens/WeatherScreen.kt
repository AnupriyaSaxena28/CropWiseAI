package com.cropwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.WeatherData
import com.cropwise.app.ui.components.LoadingBox
import com.cropwise.app.ui.components.ScreenScaffold
import com.cropwise.app.ui.components.SectionCard

@Composable
fun WeatherScreen(vm: MainViewModel) {
    val profile by vm.profile.collectAsStateWithLifecycle()
    var weather by remember { mutableStateOf<WeatherData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profile?.state) {
        loading = true; error = null
        runCatching { vm.repo.weather(profile?.state, null, null, null) }
            .onSuccess { weather = it }.onFailure { error = it.message }
        loading = false
    }

    if (loading) { LoadingBox(); return }
    error?.let { ScreenScaffold { Text(it, color = MaterialTheme.colorScheme.error) }; return }
    val w = weather ?: return

    // Climate resilience alert (mirrors website logic)
    val maxHigh = w.forecast.maxOfOrNull { it.high } ?: w.temperature
    val maxRain = w.forecast.maxOfOrNull { it.precipitationChance } ?: 0
    val alert = when {
        maxHigh > 38 -> "🔥 Heatwave warning: highs above 38°C expected. Irrigate early morning/evening, mulch soil, and provide shade to sensitive crops."
        maxRain > 60 -> "🌧️ Heavy rain likely (>60%). Delay spraying and fertiliser, ensure field drainage, and harvest mature produce early."
        else -> null
    }

    ScreenScaffold {
        Text(w.location, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("${w.temperature}°C", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("${w.condition} · feels like ${w.feelsLike}°C")

        alert?.let {
            Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                Text(it, Modifier.padding(12.dp))
            }
        }

        SectionCard("Conditions") {
            Grid2("Humidity", "${w.humidity}%", "Wind", "${w.windSpeed} km/h")
            Grid2("UV Index", "${w.uvIndex}", "Visibility", "${w.visibility} km")
            Grid2("Soil Moisture", "${w.soilMoisture}%", "Soil Temp", "${w.soilTemp}°C")
            Grid2("Evapotranspiration", "${w.evapotranspiration} mm", "Solar", "${w.solarRadiation} kWh/m²")
        }

        SectionCard("7-Day Forecast") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(w.forecast) { d ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(64.dp)) {
                        Text(d.dayName, fontWeight = FontWeight.SemiBold)
                        Text("${d.high}°", fontWeight = FontWeight.Bold)
                        Text("${d.low}°", style = MaterialTheme.typography.bodySmall)
                        Text("${d.precipitationChance}%", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun Grid2(l1: String, v1: String, l2: String, v2: String) {
    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) { Text(v1, fontWeight = FontWeight.SemiBold); Text(l1, style = MaterialTheme.typography.bodySmall) }
        Column(Modifier.weight(1f)) { Text(v2, fontWeight = FontWeight.SemiBold); Text(l2, style = MaterialTheme.typography.bodySmall) }
    }
}
