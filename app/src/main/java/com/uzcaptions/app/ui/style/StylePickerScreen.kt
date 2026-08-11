package com.uzcaptions.app.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uzcaptions.app.data.local.entity.CaptionBackground
import com.uzcaptions.app.data.local.entity.CaptionStyle
import com.uzcaptions.app.data.local.entity.CaptionStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylePickerScreen(
    viewModel: StylePickerViewModel,
    onBack: () -> Unit
) {
    val project by viewModel.project.collectAsState()
    val selectedId = project?.styleId ?: CaptionStyles.DEFAULT_ID

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subtitr dizayni") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(CaptionStyles.ALL) { style ->
                StylePreviewCard(
                    style = style,
                    selected = style.id == selectedId,
                    onClick = {
                        viewModel.selectStyle(style.id)
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun StylePreviewCard(style: CaptionStyle, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = if (selected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2B2B2E))
                .padding(24.dp)
        ) {
            val sample = if (style.uppercase) "SALOM, DO'STLAR!" else "Salom, do'stlar!"
            val shape = RoundedCornerShape(if (style.background == CaptionBackground.PILL) 20.dp else 6.dp)
            val bgModifier = if (style.background != CaptionBackground.NONE && style.backgroundColor != null) {
                Modifier.background(Color(style.backgroundColor), shape).padding(horizontal = 14.dp, vertical = 8.dp)
            } else {
                Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            }
            Box(modifier = Modifier.align(Alignment.Center).then(bgModifier)) {
                Text(
                    text = sample,
                    color = Color(style.textColor),
                    fontWeight = if (style.bold) FontWeight.Bold else FontWeight.Normal
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
        Text(
            style.displayName,
            modifier = Modifier.padding(12.dp),
            fontWeight = FontWeight.Medium
        )
    }
}
