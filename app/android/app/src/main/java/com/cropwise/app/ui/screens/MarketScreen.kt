package com.cropwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.MarketPrice
import com.cropwise.app.ui.components.LoadingBox
import com.cropwise.app.ui.components.ScreenScaffold
import com.cropwise.app.ui.components.SectionCard
import com.cropwise.app.util.Constants

@Composable
fun MarketScreen(vm: MainViewModel) {
    val profile by vm.profile.collectAsStateWithLifecycle()
    var state by remember { mutableStateOf<String?>(null) }
    var prices by remember { mutableStateOf<List<MarketPrice>?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(profile) { if (state == null) state = profile?.state ?: Constants.STATES.first() }
    LaunchedEffect(state) {
        loading = true
        runCatching { vm.repo.market(state, null) }.onSuccess { prices = it }
        loading = false
    }

    ScreenScaffold {
        Text("Mandi Prices", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Live commodity prices vs MSP (2024-25). Source: data.gov.in Agmarknet.")
        Dropdown("State", Constants.STATES, state ?: Constants.STATES.first()) { state = it }

        if (loading) LoadingBox()
        else {
            val list = prices.orEmpty()
            if (list.isEmpty()) Text("No arrivals reported for this location today.")
            list.forEach { p -> PriceRow(p) }
        }
    }
}

@Composable
private fun PriceRow(p: MarketPrice) {
    SectionCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(p.cropName, fontWeight = FontWeight.SemiBold)
                p.cropNameHi?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text(p.market, style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("₹${p.currentPrice}/qtl", fontWeight = FontWeight.Bold)
                if (p.msp > 0) Text("MSP ₹${p.msp}", style = MaterialTheme.typography.bodySmall)
                if (p.msp > 0) {
                    val up = p.priceChange >= 0
                    Text("${if (up) "▲" else "▼"} ${p.priceChangePercent}%",
                        color = if (up) Color(0xFF2EA82E) else Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
