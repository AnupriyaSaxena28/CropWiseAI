package com.cropwise.app.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.GeminiRequest
import com.cropwise.app.ui.components.ScreenScaffold
import com.cropwise.app.ui.components.SectionCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PestDiagnosisScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val language by vm.language.collectAsStateWithLifecycle()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Map<String, Any?>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri; result = null; error = null
    }

    ScreenScaffold {
        Text("Pest & Disease Diagnosis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Upload a crop photo. AI identifies the disease and recommends sustainable, organic-first treatments.")

        imageUri?.let {
            AsyncImage(model = it, contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(220.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { picker.launch("image/*") }) { Text("Choose Photo") }
            Button(
                onClick = {
                    val uri = imageUri ?: return@Button
                    loading = true; error = null; result = null
                    scope.launch {
                        val b64 = withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                val bytes = input.readBytes()
                                // Re-encode to JPEG to normalise mime type
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                val out = java.io.ByteArrayOutputStream()
                                bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
                                Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                            }
                        }
                        if (b64 == null) { error = "Could not read image"; loading = false; return@launch }
                        runCatching {
                            vm.repo.gemini(GeminiRequest(
                                prompt = "Diagnose any disease or pest in this crop image.",
                                mode = "pest_diagnosis", language = language,
                                imageBase64 = b64, imageMimeType = "image/jpeg"))
                        }.onSuccess {
                            if (it.success) result = it.structured else error = it.error
                        }.onFailure { error = it.message }
                        loading = false
                    }
                },
                enabled = imageUri != null && !loading,
            ) { Text(if (loading) "Analyzing…" else "Diagnose") }
        }

        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        result?.let { r -> DiagnosisResult(r) }
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
private fun DiagnosisResult(r: Map<String, Any?>) {
    SectionCard(r["diseaseName"]?.toString() ?: "Result") {
        (r["scientificName"] as? String)?.takeIf { it.isNotBlank() }?.let { Text("Scientific name: $it") }
        Text("Severity: ${r["severity"] ?: "—"}")
        (r["confidencePercent"] as? Number)?.let { Text("Confidence: ${it.toInt()}%") }
        (r["affectedArea"] as? String)?.let { Text(it) }

        (r["symptoms"] as? List<*>)?.let { list ->
            if (list.isNotEmpty()) {
                Text("Symptoms", fontWeight = FontWeight.SemiBold)
                list.forEach { Text("• $it") }
            }
        }

        (r["treatment"] as? Map<String, Any?>)?.let { t ->
            (t["sustainableAlternativesFromGraph"] as? List<Map<String, Any?>>)?.let { alts ->
                if (alts.isNotEmpty()) {
                    Text("🌱 Sustainable alternatives (Knowledge Graph)", fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary)
                    alts.forEach { a -> Text("• ${a["name"]} — ${a["soilHealthBenefit"]}") }
                }
            }
            (t["immediate"] as? List<*>)?.let { l ->
                if (l.isNotEmpty()) { Text("Immediate", fontWeight = FontWeight.SemiBold); l.forEach { Text("• $it") } }
            }
            (t["preventive"] as? List<*>)?.let { l ->
                if (l.isNotEmpty()) { Text("Preventive", fontWeight = FontWeight.SemiBold); l.forEach { Text("• $it") } }
            }
            (t["recommendedPesticides"] as? List<*>)?.let { l ->
                if (l.isNotEmpty()) { Text("Chemical (last resort)", fontWeight = FontWeight.SemiBold); l.forEach { Text("• $it") } }
            }
        }
        (r["disclaimer"] as? String)?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}
