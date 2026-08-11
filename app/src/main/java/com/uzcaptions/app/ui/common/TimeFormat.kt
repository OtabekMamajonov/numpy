package com.uzcaptions.app.ui.common

import java.util.Locale

/** mm:ss for UI display (timeline, player position). */
fun formatTimeShort(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

/** hh:mm:ss,mmm for SRT export. */
fun formatTimeSrt(ms: Long): String {
    val h = ms / 3_600_000
    val m = (ms % 3_600_000) / 60_000
    val s = (ms % 60_000) / 1000
    val millis = ms % 1000
    return String.format(Locale.US, "%02d:%02d:%02d,%03d", h, m, s, millis)
}
