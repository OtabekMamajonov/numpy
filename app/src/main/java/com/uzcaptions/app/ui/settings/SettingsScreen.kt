package com.uzcaptions.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    var apiKey by remember { mutableStateOf(viewModel.getSttApiKey()) }
    var showKey by remember { mutableStateOf(false) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sozlamalar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nutqni matnga aylantirish (Muxlisa AI)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Avtomatik subtitr yaratish uchun Muxlisa AI (muxlisa.uz) API key kerak. " +
                    "Key faqat shu qurilmada shifrlangan holda saqlanadi. Uzoq videolar avtomatik " +
                    "ravishda ~8 soniyalik bo'laklarga bo'linib yuboriladi (API'ning 5 MB / 60 soniya " +
                    "chegarasi tufayli).",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Muxlisa AI API key") },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Ko'rsatish"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.setSttApiKey(apiKey.trim())
                    savedMessage = "Saqlandi"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Saqlash")
            }

            savedMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
