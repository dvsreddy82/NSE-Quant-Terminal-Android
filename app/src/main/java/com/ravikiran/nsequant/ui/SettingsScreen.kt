package com.ravikiran.nsequant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravikiran.nsequant.data.Api
import com.ravikiran.nsequant.data.Prefs
import com.ravikiran.nsequant.data.obj
import com.ravikiran.nsequant.data.s
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var server by remember { mutableStateOf(Prefs.server(ctx).ifBlank { "http://192.168.1.10:8787" }) }
    var key by remember { mutableStateOf(Prefs.key(ctx)) }
    var result by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Connect to your Mac", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("On the Mac app: Sync ▾ → Connect phone (Android app)… shows the address and key. Phone and Mac must " +
            "be on the same Wi-Fi, or both on Tailscale (use the 100.x address) to connect from anywhere.",
            color = Pal.muted, fontSize = 13.sp)
        OutlinedTextField(server, { server = it }, Modifier.fillMaxWidth(), label = { Text("Server address") },
            singleLine = true)
        OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth(), label = { Text("API key") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                Prefs.save(ctx, server, key)
                result = "Testing…"
                scope.launch {
                    result = try {
                        val h = Api.get(ctx, "/api/health")
                        "✅ Connected · Shoonya: ${h.obj("shoonya")?.s("text")} · market " +
                            (if (h.optBoolean("market_open")) "open" else "closed")
                    } catch (e: Exception) {
                        "❌ ${e.message}"
                    }
                }
            }) { Text("Save & test") }
            OutlinedButton(onClick = { Prefs.save(ctx, server, key); onDone() }) { Text("Done") }
        }
        result?.let { Text(it, fontSize = 13.sp) }
        Text("Read-only: this app can view data from your Mac, never place orders.", color = Pal.faint, fontSize = 11.sp)
    }
}
