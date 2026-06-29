package com.cropwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.ProfileUpdateRequest
import com.cropwise.app.ui.components.ScreenScaffold
import com.cropwise.app.ui.components.SectionCard
import com.cropwise.app.util.Constants
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(vm: MainViewModel) {
    val profile by vm.profile.collectAsStateWithLifecycle()
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var state by remember(profile) { mutableStateOf(profile?.state ?: Constants.STATES.first()) }
    var district by remember(profile) { mutableStateOf(profile?.district ?: "") }
    var soil by remember(profile) { mutableStateOf(profile?.soilType ?: Constants.SOIL_TYPES.first()) }
    var water by remember(profile) { mutableStateOf(profile?.waterSource ?: Constants.WATER_SOURCES.first()) }
    var crop by remember(profile) { mutableStateOf(profile?.primaryCrop ?: "") }
    var land by remember(profile) { mutableStateOf(profile?.landAreaAcres?.toString() ?: "") }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ScreenScaffold {
        Text("Farm Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("This profile is sent as live context so the AI gives advice specific to your farm.")

        SectionCard {
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
            Dropdown("State", Constants.STATES, state) { state = it }
            OutlinedTextField(district, { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
            Dropdown("Soil type", Constants.SOIL_TYPES, soil) { soil = it }
            Dropdown("Water source", Constants.WATER_SOURCES, water) { water = it }
            OutlinedTextField(crop, { crop = it }, label = { Text("Primary crop") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(land, { land = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Land area (acres)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }

        Button(
            onClick = {
                scope.launch {
                    runCatching {
                        vm.repo.updateProfile(ProfileUpdateRequest(
                            name = name, phone = phone, state = state, district = district,
                            soilType = soil, waterSource = water, primaryCrop = crop,
                            landAreaAcres = land.toDoubleOrNull()))
                    }.onSuccess { vm.loadProfile(); saved = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Save Profile") }
        if (saved) Text("Saved ✓", color = MaterialTheme.colorScheme.primary)
    }
}
