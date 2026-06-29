package com.cropwise.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.Scheme
import com.cropwise.app.ui.components.LoadingBox
import com.cropwise.app.ui.components.ScreenScaffold
import com.cropwise.app.ui.components.SectionCard

@Composable
fun SchemesScreen(vm: MainViewModel) {
    var schemes by remember { mutableStateOf<List<Scheme>?>(null) }
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(Unit) { runCatching { vm.repo.schemes() }.onSuccess { schemes = it } }

    ScreenScaffold {
        Text("Government Schemes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        val list = schemes
        if (list == null) LoadingBox()
        else list.forEach { s ->
            SectionCard(s.name) {
                Text(s.ministry, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(s.description)
                if (s.benefits.isNotEmpty()) {
                    Text("Benefits", fontWeight = FontWeight.SemiBold)
                    s.benefits.forEach { Text("• $it") }
                }
                if (s.eligibility.isNotEmpty()) {
                    Text("Eligibility", fontWeight = FontWeight.SemiBold)
                    s.eligibility.forEach { Text("• $it") }
                }
                Button(onClick = { runCatching { uriHandler.openUri(s.applicationUrl) } }) { Text("Apply / Learn More") }
            }
        }
    }
}
