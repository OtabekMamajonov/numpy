package com.uzcaptions.app.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.uzcaptions.app.data.local.entity.SubtitleSegment
import java.io.File

fun buildSrtContent(segments: List<SubtitleSegment>): String {
    val sb = StringBuilder()
    segments.filter { it.text.isNotBlank() }.forEachIndexed { index, segment ->
        sb.appendLine(index + 1)
        sb.appendLine("${formatTimeSrt(segment.startMs)} --> ${formatTimeSrt(segment.endMs)}")
        sb.appendLine(segment.text.trim())
        sb.appendLine()
    }
    return sb.toString()
}

fun exportAndShareSrt(context: Context, projectTitle: String, segments: List<SubtitleSegment>) {
    val dir = File(context.getExternalFilesDir(null), "subtitles").apply { mkdirs() }
    val safeName = projectTitle.replace(Regex("[^a-zA-Z0-9а-яА-ЯўЎқҚғҒҳҲ_-]"), "_").ifBlank { "subtitr" }
    val file = File(dir, "$safeName.srt")
    file.writeText(buildSrtContent(segments))

    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Subtitrni ulashish (.srt)"))
}
