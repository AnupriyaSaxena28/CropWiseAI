package com.cropwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.GeminiRequest
import com.cropwise.app.ui.components.ScreenScaffold
import com.cropwise.app.ui.components.SectionCard
import com.cropwise.app.util.Constants
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
@Composable
fun CropAdvisorScreen(vm: MainViewModel) {
    val language by vm.language.collectAsStateWithLifecycle()
    var soil by remember { mutableStateOf(Constants.SOIL_TYPES.first()) }
    var season by remember { mutableStateOf(Constants.SEASONS.first()) }
    var state by remember { mutableStateOf(Constants.STATES.first()) }
    var water by remember { mutableStateOf(Constants.WATER_SOURCES.first()) }
    var land by remember { mutableStateOf("2") }
    var loading by remember { mutableStateOf(false) }
    var recs by remember { mutableStateOf<List<Map<String, Any?>>?>(null) }
    var advice by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ScreenScaffold {
        Text("AI Crop Advisor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Get crop recommendations with full ROI and climate-risk breakdown.")

        Dropdown("Soil type", Constants.SOIL_TYPES, soil) { soil = it }
        Dropdown("Season", Constants.SEASONS, season) { season = it }
        Dropdown("State", Constants.STATES, state) { state = it }
        Dropdown("Water source", Constants.WATER_SOURCES, water) { water = it }
        OutlinedTextField(land, { land = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Land area (acres)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                loading = true; error = null; recs = null; advice = null
                val prompt = "Recommend the best crops for: soil=$soil, season=$season, state=$state, " +
                        "water source=$water, land area=$land acres. Return top 3 recommendations."
                scope.launch {
                    runCatching { vm.repo.gemini(GeminiRequest(prompt = prompt, mode = "crop_advisor", language = language, context = prompt)) }
                        .onSuccess {
                            if (it.success) {
                                recs = it.structured?.get("recommendations") as? List<Map<String, Any?>>
                                advice = it.structured?.get("generalAdvice") as? String
                            } else error = it.error
                        }.onFailure { error = it.message }
                    loading = false
                }
            },
            enabled = !loading, modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Analyzing…" else "Get Recommendations") }

        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        advice?.let { Text(it) }

        recs?.forEach { rec -> CropCard(rec) }
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
private fun CropCard(rec: Map<String, Any?>) {
    SectionCard("${rec["cropName"]}${(rec["localName"] as? String)?.let { " ($it)" } ?: ""}") {
        (rec["suitabilityScore"] as? Number)?.let { Text("Suitability: ${it.toInt()}/100") }
        Text("Climate risk: ${rec["climateRiskLevel"] ?: "—"} · Water: ${rec["waterRequirement"] ?: "—"}")
        (rec["carbonFootprint"] as? String)?.let { Text("Carbon footprint: $it") }
        (rec["expectedYield"] as? String)?.let { Text("Expected yield: $it") }
        (rec["estimatedROI"] as? Map<String, Any?>)?.let { roi ->
            Text("ROI / acre", fontWeight = FontWeight.SemiBold)
            Text("Investment ₹${(roi["investmentPerAcre"] as? Number)?.toInt() ?: 0} · " +
                 "Revenue ₹${(roi["expectedRevenuePerAcre"] as? Number)?.toInt() ?: 0}")
            Text("Profit ₹${(roi["profitPerAcre"] as? Number)?.toInt() ?: 0} · " +
                 "Payback ${(roi["paybackMonths"] as? Number)?.toInt() ?: 0} months")
        }
        (rec["sustainabilityTags"] as? List<*>)?.takeIf { it.isNotEmpty() }?.let {
            Text("Tags: ${it.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
        }
        (rec["reasonsForRecommendation"] as? List<*>)?.let { l ->
            if (l.isNotEmpty()) { Text("Why", fontWeight = FontWeight.SemiBold); l.forEach { Text("• $it") } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); expanded = false })
            }
        }
    }
}
