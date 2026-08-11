package com.uzcaptions.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.uzcaptions.app.data.local.entity.CaptionBackground
import com.uzcaptions.app.data.local.entity.CaptionPosition
import com.uzcaptions.app.data.local.entity.CaptionStyle
import com.uzcaptions.app.data.local.entity.SubtitleSegment
import com.uzcaptions.app.data.local.entity.SubtitleWord

@Composable
fun CaptionOverlay(
    segment: SubtitleSegment?,
    words: List<SubtitleWord>,
    style: CaptionStyle,
    playbackPositionMs: Long,
    modifier: Modifier = Modifier
) {
    if (segment == null || segment.text.isBlank()) return

    val alignment = when (style.position) {
        CaptionPosition.TOP -> Alignment.TopCenter
        CaptionPosition.MIDDLE -> Alignment.Center
        CaptionPosition.BOTTOM -> Alignment.BottomCenter
    }

    val shape = RoundedCornerShape(if (style.background == CaptionBackground.PILL) 20.dp else 6.dp)
    val bgModifier = if (style.background != CaptionBackground.NONE && style.backgroundColor != null) {
        Modifier.background(Color(style.backgroundColor), shape).padding(horizontal = 14.dp, vertical = 8.dp)
    } else {
        Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
    }

    Box(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Box(
            modifier = Modifier
                .align(alignment)
                .wrapContentWidth()
                .then(bgModifier)
        ) {
            if (style.wordHighlight && words.isNotEmpty()) {
                KaraokeText(words, style, playbackPositionMs)
            } else {
                Text(
                    text = if (style.uppercase) segment.text.uppercase() else segment.text,
                    color = Color(style.textColor),
                    fontWeight = if (style.bold) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KaraokeText(words: List<SubtitleWord>, style: CaptionStyle, playbackPositionMs: Long) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        words.forEach { word ->
            val isActive = playbackPositionMs in word.startMs..word.endMs
            Text(
                text = if (style.uppercase) word.text.uppercase() else word.text,
                color = if (isActive) Color(style.highlightColor) else Color(style.textColor),
                fontWeight = if (style.bold || isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
