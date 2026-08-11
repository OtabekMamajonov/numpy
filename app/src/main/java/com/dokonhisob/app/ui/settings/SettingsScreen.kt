package com.dokonhisob.app.ui.settings

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    var apiKey by remember { mutableStateOf(viewModel.getApiKey()) }
    var shopName by remember { mutableStateOf(viewModel.getShopName()) }
    var currency by remember { mutableStateOf(viewModel.getCurrency()) }
    var model by remember { mutableStateOf(viewModel.getModel()) }
    var showKey by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    val availableModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-4.1-mini", "gpt-3.5-turbo")

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Do'kon ma'lumotlari", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Do'kon nomi") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Valyuta (masalan: so'm)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("OpenAI sozlamalari", style = MaterialTheme.typography.titleMedium)
            Text(
                "AI Yordamchi bo'limi ishlashi uchun OpenAI API key kerak. Key faqat shu qurilmada shifrlangan holda saqlanadi.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("OpenAI API key") },
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

            ExposedDropdownMenuBox(expanded = modelExpanded, onExpandedChange = { modelExpanded = it }) {
                OutlinedTextField(
                    value = model,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = modelExpanded, onDismissRequest = { modelExpanded = false }) {
                    availableModels.forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = {
                            model = it
                            modelExpanded = false
                        })
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.setShopName(shopName.trim().ifBlank { "Mening do'konim" })
                    viewModel.setCurrency(currency.trim().ifBlank { "so'm" })
                    viewModel.setApiKey(apiKey.trim())
                    viewModel.setModel(model)
                    savedMessage = "Sozlamalar saqlandi"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Saqlash")
            }

            savedMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Text(
                "OpenAI API key'ni platform.openai.com saytidagi API Keys bo'limidan olishingiz mumkin.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
