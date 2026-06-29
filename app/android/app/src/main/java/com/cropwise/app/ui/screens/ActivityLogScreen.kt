package com.cropwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.ActivityDto
import com.cropwise.app.data.model.CreateActivityRequest
import com.cropwise.app.data.model.EcoSummary
import com.cropwise.app.ui.components.ScreenScaffold
import com.cropwise.app.ui.components.SectionCard
import com.cropwise.app.util.Constants
import kotlinx.coroutines.launch

@Composable
fun ActivityLogScreen(vm: MainViewModel) {
    var logs by remember { mutableStateOf<List<ActivityDto>>(emptyList()) }
    var eco by remember { mutableStateOf<EcoSummary?>(null) }
    var type by remember { mutableStateOf(Constants.ACTIVITY_TYPES.first()) }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        runCatching { vm.repo.activities() }.onSuccess { logs = it }
        runCatching { vm.repo.ecoSummary() }.onSuccess { eco = it }
    }
    LaunchedEffect(Unit) { refresh() }

    ScreenScaffold {
        Text("Activity Log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        eco?.let { e ->
            SectionCard("Sustainability") {
                Text("Eco-Score: ${e.ecoScore}/100", fontWeight = FontWeight.SemiBold)
                Text("Carbon saved: ${e.carbonSaved} kg CO₂e · Water saved: ${e.waterSaved} L")
                Text("Monthly spend: ₹${e.monthlySpend.toInt()}")
            }
        }

        SectionCard("Log an Operation") {
            Dropdown("Type", Constants.ACTIVITY_TYPES, type) { type = it }
            OutlinedTextField(title, { title = it }, label = { Text("Title") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it },
                label = { Text("Notes (e.g. 'organic', 'drip', 'manual')") },
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(cost, { cost = it.filter { c -> c.isDigit() } },
                label = { Text("Cost (₹)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    scope.launch {
                        runCatching {
                            vm.repo.createActivity(CreateActivityRequest(
                                type = type, title = title.ifBlank { type },
                                notes = notes.ifBlank { null }, cost = cost.toDoubleOrNull()))
                        }.onSuccess { title = ""; notes = ""; cost = ""; refresh() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Add Entry") }
        }

        Text("History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        logs.forEach { log ->
            SectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${log.type.replaceFirstChar { it.uppercase() }} — ${log.title ?: ""}", fontWeight = FontWeight.SemiBold)
                        log.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        Text("${log.date}${log.cost?.let { " · ₹${it.toInt()}" } ?: ""}",
                            style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { scope.launch { runCatching { vm.repo.deleteActivity(log.id) }; refresh() } }) {
                        Icon(Icons.Filled.Delete, "Delete")
                    }
                }
            }
        }
    }
}
