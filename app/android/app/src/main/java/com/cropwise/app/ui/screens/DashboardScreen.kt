package com.cropwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.EcoSummary
import com.cropwise.app.data.model.WeatherData
import com.cropwise.app.ui.components.SectionCard
import com.cropwise.app.ui.navigation.Dest

@Composable
fun DashboardScreen(vm: MainViewModel, nav: NavController) {
    val profile by vm.profile.collectAsStateWithLifecycle()
    var eco by remember { mutableStateOf<EcoSummary?>(null) }
    var weather by remember { mutableStateOf<WeatherData?>(null) }

    LaunchedEffect(Unit) {
        runCatching { vm.repo.ecoSummary() }.onSuccess { eco = it }
        runCatching { vm.repo.weather(profile?.state, null, null, null) }.onSuccess { weather = it }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Namaste, ${profile?.name ?: "Farmer"} 🙏",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "${profile?.district ?: ""} ${profile?.state ?: "Set your farm location in Profile"}".trim(),
            style = MaterialTheme.typography.bodyMedium,
        )

        // Eco-score row
        val e = eco
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Eco-Score", e?.let { "${it.ecoScore}/100" } ?: "—",
                color = ecoColor(e?.ecoScore ?: 0), modifier = Modifier.weight(1f))
            StatTile("Carbon Saved", e?.let { "${it.carbonSaved} kg" } ?: "—",
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Water Saved", e?.let { "${it.waterSaved} L" } ?: "—",
                color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
            StatTile("Monthly Spend", e?.let { "₹${it.monthlySpend.toInt()}" } ?: "—",
                color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
        }

        // Weather snapshot
        weather?.let { w ->
            SectionCard("Weather · ${w.location}") {
                Text("${w.temperature}°C, ${w.condition} · feels ${w.feelsLike}°C")
                Text("Humidity ${w.humidity}% · Wind ${w.windSpeed} km/h · Soil moisture ${w.soilMoisture}%")
            }
        }

        SectionCard("Quick Actions") {
            QuickAction("Ask the AI Advisor") { nav.navigate(Dest.Chat.route) }
            QuickAction("Diagnose a pest from a photo") { nav.navigate(Dest.Pest.route) }
            QuickAction("Get a crop recommendation") { nav.navigate(Dest.Crop.route) }
            QuickAction("Check today's mandi prices") { nav.navigate(Dest.Market.route) }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun QuickAction(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge)
    }
}

private fun ecoColor(score: Int): Color = when {
    score >= 80 -> Color(0xFF2EA82E)
    score >= 50 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}
