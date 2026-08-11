package com.uzcaptions.app.ui.editor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.uzcaptions.app.data.local.entity.CaptionStyles
import com.uzcaptions.app.data.local.entity.SubtitleSegment
import com.uzcaptions.app.ui.common.formatTimeShort
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onOpenStylePicker: (projectId: Long) -> Unit,
    onExportSrt: (projectId: Long, segments: List<SubtitleSegment>) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var playbackPositionMs by remember { mutableLongStateOf(0L) }
    var editingSegment by remember { mutableStateOf<SubtitleSegment?>(null) }

    val project = state.project

    val exoPlayer = remember(project?.videoUri) {
        ExoPlayer.Builder(context).build().apply {
            if (project != null) {
                setMediaItem(MediaItem.fromUri(Uri.parse(project.videoUri)))
                prepare()
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            playbackPositionMs = exoPlayer.currentPosition
            delay(80)
        }
    }

    val style = CaptionStyles.byId(project?.styleId ?: CaptionStyles.DEFAULT_ID)
    val activeSegment = viewModel.currentSegmentAt(playbackPositionMs)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.title ?: "Loyiha") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
                    }
                },
                actions = {
                    IconButton(onClick = { onOpenStylePicker(project?.id ?: 0L) }) {
                        Icon(Icons.Default.Palette, contentDescription = "Dizayn")
                    }
                    IconButton(onClick = { onExportSrt(project?.id ?: 0L, state.segments) }) {
                        Icon(Icons.Default.Save, contentDescription = "SRT eksport")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .background(androidx.compose.ui.graphics.Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                // Word-level timing isn't provided by the manual editor or the
                // (not-yet-wired) STT result yet, so karaoke styles fall back to
                // an even split across the segment's duration.
                val words = remember(activeSegment?.id, activeSegment?.text) {
                    activeSegment?.let { evenlySplitWords(it.text, it.startMs, it.endMs) } ?: emptyList()
                }

                CaptionOverlay(
                    segment = activeSegment,
                    words = words,
                    style = style,
                    playbackPositionMs = playbackPositionMs,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (state.generateError != null) {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(
                        state.generateError ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { viewModel.generateCaptions(context) }, enabled = !state.isGenerating) {
                    if (state.isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    }
                    Text(" Avtomatik subtitr yaratish")
                }
                TextButton(onClick = {
                    viewModel.addSegmentAt(playbackPositionMs)
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Qo'lda qo'shish")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.segments, key = { it.id }) { segment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (segment.id == activeSegment?.id)
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                                else Modifier
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        Modifier.clickableText { editingSegment = segment }
                                    )
                            ) {
                                Text(
                                    "${formatTimeShort(segment.startMs)} - ${formatTimeShort(segment.endMs)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    segment.text.ifBlank { "(matn yo'q)" },
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { viewModel.deleteSegment(segment) }) {
                                Icon(Icons.Default.Delete, contentDescription = "O'chirish")
                            }
                        }
                    }
                }
            }
        }
    }

    editingSegment?.let { segment ->
        SegmentEditDialog(
            segment = segment,
            onDismiss = { editingSegment = null },
            onSave = {
                viewModel.updateSegment(it)
                editingSegment = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentEditDialog(
    segment: SubtitleSegment,
    onDismiss: () -> Unit,
    onSave: (SubtitleSegment) -> Unit
) {
    var text by remember { mutableStateOf(segment.text) }
    var startSec by remember { mutableStateOf((segment.startMs / 1000).toString()) }
    var endSec by remember { mutableStateOf((segment.endMs / 1000).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Subtitrni tahrirlash") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Matn") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startSec,
                        onValueChange = { startSec = it },
                        label = { Text("Boshlanishi (soniya)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endSec,
                        onValueChange = { endSec = it },
                        label = { Text("Tugashi (soniya)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val start = (startSec.toDoubleOrNull() ?: (segment.startMs / 1000.0)) * 1000
                val end = (endSec.toDoubleOrNull() ?: (segment.endMs / 1000.0)) * 1000
                onSave(segment.copy(text = text, startMs = start.toLong(), endMs = end.toLong()))
            }) {
                Text("Saqlash")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish") }
        }
    )
}

private fun evenlySplitWords(text: String, startMs: Long, endMs: Long): List<com.uzcaptions.app.data.local.entity.SubtitleWord> {
    val tokens = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (tokens.isEmpty()) return emptyList()
    val duration = (endMs - startMs).coerceAtLeast(tokens.size.toLong())
    val perWord = duration / tokens.size
    return tokens.mapIndexed { index, word ->
        val wordStart = startMs + perWord * index
        val wordEnd = if (index == tokens.lastIndex) endMs else wordStart + perWord
        com.uzcaptions.app.data.local.entity.SubtitleWord(word, wordStart, wordEnd)
    }
}

private fun Modifier.clickableText(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
