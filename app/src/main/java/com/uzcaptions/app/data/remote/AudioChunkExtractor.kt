package com.uzcaptions.app.data.remote

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

data class AudioChunk(val file: File, val startMs: Long, val endMs: Long)

/**
 * Splits a video's audio track into short, standalone .m4a (audio-only mp4)
 * files by copying encoded samples directly via MediaExtractor/MediaMuxer
 * (no decode/re-encode), so it works for any device without extra codec
 * dependencies. Used because the Muxlisa STT API caps uploads at 5 MB /
 * 60 seconds per request.
 */
object AudioChunkExtractor {
    private const val CHUNK_DURATION_MS = 8_000L

    fun extractChunks(context: Context, videoUri: Uri, durationMs: Long): List<AudioChunk> {
        val chunks = mutableListOf<AudioChunk>()
        var start = 0L
        while (start < durationMs) {
            val end = (start + CHUNK_DURATION_MS).coerceAtMost(durationMs)
            val file = extractAudioChunk(context, videoUri, start, end)
            if (file != null) {
                chunks.add(AudioChunk(file, start, end))
            }
            start = end
        }
        return chunks
    }

    private fun extractAudioChunk(context: Context, videoUri: Uri, startMs: Long, endMs: Long): File? {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        return try {
            extractor.setDataSource(context, videoUri, null)

            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }
            if (audioTrackIndex == -1 || audioFormat == null) return null

            extractor.selectTrack(audioTrackIndex)
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

            val outFile = File.createTempFile("uzcap_chunk_${startMs}_", ".m4a", context.cacheDir)
            muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val buffer = ByteBuffer.allocate(1 shl 20)
            val bufferInfo = MediaCodec.BufferInfo()
            var wroteAnySample = false
            val endUs = endMs * 1000
            val startUs = startMs * 1000

            while (true) {
                val sampleTimeUs = extractor.sampleTime
                if (sampleTimeUs < 0 || sampleTimeUs > endUs) break

                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = (sampleTimeUs - startUs).coerceAtLeast(0)
                bufferInfo.flags = extractor.sampleFlags
                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                wroteAnySample = true
                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            muxer = null

            if (wroteAnySample) outFile else {
                outFile.delete()
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            try {
                muxer?.release()
            } catch (_: Exception) {
            }
            extractor.release()
        }
    }
}
