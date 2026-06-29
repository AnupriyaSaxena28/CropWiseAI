package com.cropwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cropwise.app.MainViewModel
import com.cropwise.app.data.model.GeminiRequest
import com.cropwise.app.data.model.HistoryItem
import kotlinx.coroutines.launch

private data class Msg(val role: String, val text: String)

@Composable
fun ChatScreen(vm: MainViewModel) {
    val language by vm.language.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val messages = remember { mutableStateListOf<Msg>() }
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        runCatching { vm.repo.chatHistory() }.onSuccess { hist ->
            messages.clear()
            hist.forEach { messages.add(Msg(it.role, it.content)) }
            if (messages.isEmpty())
                messages.add(Msg("model", "Namaste! I'm your CropWise AI advisor. Ask me anything about your crops, soil, weather or market prices."))
        }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || sending) return
        input = ""
        messages.add(Msg("user", text))
        sending = true
        scope.launch {
            runCatching { vm.repo.saveMessage("user", text, null) }
            val history = messages.dropLast(1).takeLast(10).map { HistoryItem(it.role, it.text) }
            val ctx = profile?.let {
                "State: ${it.state ?: "?"}, District: ${it.district ?: "?"}, Soil: ${it.soilType ?: "?"}, " +
                "Primary crop: ${it.primaryCrop ?: "?"}, Land: ${it.landAreaAcres ?: "?"} acres"
            }
            runCatching {
                vm.repo.gemini(GeminiRequest(prompt = text, mode = "chat", language = language, context = ctx, history = history))
            }.onSuccess { res ->
                val reply = res.text ?: res.error ?: "Sorry, I couldn't answer that."
                messages.add(Msg("model", reply))
                runCatching { vm.repo.saveMessage("model", reply, null) }
            }.onFailure {
                messages.add(Msg("model", "Network error: ${it.message}"))
            }
            sending = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(messages) { Bubble(it) }
            if (sending) item {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp)); Text("Thinking…")
                }
            }
        }
        HorizontalDivider()
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                placeholder = { Text("Ask about your farm…") },
                modifier = Modifier.weight(1f), maxLines = 4,
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = { send() }, enabled = !sending && input.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send")
            }
        }
    }
}

@Composable
private fun Bubble(msg: Msg) {
    val isUser = msg.role == "user"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Text(
                msg.text,
                Modifier.padding(12.dp),
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
