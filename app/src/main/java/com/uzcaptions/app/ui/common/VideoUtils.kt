package com.uzcaptions.app.ui.common

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns

fun getVideoDurationMs(context: Context, uri: Uri): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}

fun getDisplayName(context: Context, uri: Uri): String {
    var name = "Video"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && it.moveToFirst()) {
            name = it.getString(nameIndex) ?: name
        }
    }
    return name
}
