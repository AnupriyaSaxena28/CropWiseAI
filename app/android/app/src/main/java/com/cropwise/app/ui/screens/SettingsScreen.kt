package com.cropwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cropwise.app.MainViewModel
import com.cropwise.app.ui.components.ScreenScaffold
import com.cropwise.app.ui.components.SectionCard
import com.cropwise.app.util.Constants

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val language by vm.language.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()

    ScreenScaffold {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        SectionCard("Language") {
            Text("AI responses and voice use this language.")
            Constants.LANGUAGES.forEach { (code, label) ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = language == code, onClick = { vm.setLanguage(code) })
                    Text(label)
                }
            }
        }

        SectionCard("Account") {
            Text(profile?.email ?: "", fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) { Text("Sign Out") }
        }

        SectionCard("About") {
            Text("CropWise AI · v1.0")
            Text("AI-powered digital agronomist for Indian farmers. Backend: Spring Boot. App: Jetpack Compose.")
        }
    }
}
