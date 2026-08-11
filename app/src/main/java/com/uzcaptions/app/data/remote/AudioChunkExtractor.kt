package com.uzcaptions.app.data.remote

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class AudioChunk(val file: File, val startMs: Long, val endMs: Long)

private data class DecodedPcm(
    val file: File,
    val sampleRate: Int,
    val channelCount: Int,
    val dataSize: Long
)

/**
 * Splits a video's audio track into short standalone .wav chunks so they
 * can be uploaded to the Muxlisa STT API (which caps requests at 5 MB /
 * 60 seconds). The audio is fully decoded to raw 16-bit PCM once via
 * MediaCodec, then sliced and wrapped with a WAV header per chunk.
 *
 * A plain MediaExtractor/MediaMuxer sample copy into an .m4a/mp4
 * container was tried first, but Muxlisa's server identifies any generic
 * MP4 container as "video/mp4" (MP4 doesn't have an audio-only flavor
 * without a special ftyp brand Android's MediaMuxer doesn't let us set)
 * and rejects it — hence decoding to WAV, which is unambiguously audio.
 */
object AudioChunkExtractor {
    private const val CHUNK_DURATION_MS = 8_000L
    private const val TIMEOUT_US = 10_000L
    private const val BYTES_PER_SAMPLE = 2 // MediaCodec audio decoders output 16-bit PCM

    fun extractChunks(context: Context, videoUri: Uri, durationMs: Long): List<AudioChunk> {
        val pcm = decodeToPcm(context, videoUri) ?: return emptyList()
        val chunks = mutableListOf<AudioChunk>()
        try {
            val blockAlign = pcm.channelCount * BYTES_PER_SAMPLE
            if (blockAlign <= 0 || pcm.sampleRate <= 0 || pcm.dataSize <= 0) return emptyList()
            val bytesPerMs = (pcm.sampleRate.toDouble() * blockAlign) / 1000.0

            var start = 0L
            var index = 0L
            while (start < durationMs) {
                val end = (start + CHUNK_DURATION_MS).coerceAtMost(durationMs)
                val startByte = alignDown((start * bytesPerMs).toLong(), blockAlign)
                val endByte = alignDown((end * bytesPerMs).toLong(), blockAlign).coerceAtMost(pcm.dataSize)
                if (endByte > startByte) {
                    val file = writeWavChunk(context, pcm, startByte, endByte, index)
                    if (file != null) chunks.add(AudioChunk(file, start, end))
                }
                start = end
                index++
            }
        } finally {
            pcm.file.delete()
        }
        return chunks
    }

    private fun alignDown(value: Long, align: Int): Long =
        if (align <= 0) value else value - (value % align)

    private fun decodeToPcm(context: Context, videoUri: Uri): DecodedPcm? {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        var out: RandomAccessFile? = null
        var outFile: File? = null
        return try {
            extractor.setDataSource(context, videoUri, null)

            var trackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    trackIndex = i
                    format = trackFormat
                    break
                }
            }
            if (trackIndex == -1 || format == null) return null
            extractor.selectTrack(trackIndex)

            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            codec = MediaCodec.createDecoderByType(mime).apply {
                configure(format, null, null, 0)
                start()
            }

            outFile = File.createTempFile("uzcap_pcm_", ".raw", context.cacheDir)
            out = RandomAccessFile(outFile, "rw")

            val bufferInfo = MediaCodec.BufferInfo()
            var sawInputEOS = false
            var sawOutputEOS = false
            var totalBytes = 0L

            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    val inIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inIndex)
                        val sampleSize = inputBuffer?.let { extractor.readSampleData(it, 0) } ?: -1
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEOS = true
                        } else {
                            codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                if (outIndex >= 0) {
                    if (bufferInfo.size > 0) {
                        val outputBuffer = codec.getOutputBuffer(outIndex)
                        if (outputBuffer != null) {
                            val chunkBytes = ByteArray(bufferInfo.size)
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            outputBuffer.get(chunkBytes)
                            out.write(chunkBytes)
                            totalBytes += chunkBytes.size
                        }
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true
                    }
                }
            }

            out.close()
            out = null
            codec.stop()
            codec.release()
            codec = null

            if (totalBytes <= 0) {
                outFile.delete()
                null
            } else {
                DecodedPcm(outFile, sampleRate, channelCount, totalBytes)
            }
        } catch (e: Exception) {
            outFile?.delete()
            null
        } finally {
            try {
                out?.close()
            } catch (_: Exception) {
            }
            try {
                codec?.stop()
            } catch (_: Exception) {
            }
            try {
                codec?.release()
            } catch (_: Exception) {
            }
            extractor.release()
        }
    }

    private fun writeWavChunk(context: Context, pcm: DecodedPcm, startByte: Long, endByte: Long, chunkIndex: Long): File? {
        val dataSize = (endByte - startByte).toInt()
        if (dataSize <= 0) return null

        return try {
            val outFile = File.createTempFile("uzcap_chunk_${chunkIndex}_", ".wav", context.cacheDir)
            val buffer = ByteArray(dataSize)
            RandomAccessFile(pcm.file, "r").use { input ->
                input.seek(startByte)
                input.readFully(buffer)
            }
            outFile.outputStream().use { output ->
                output.write(buildWavHeader(dataSize, pcm.sampleRate, pcm.channelCount))
                output.write(buffer)
            }
            outFile
        } catch (e: Exception) {
            null
        }
    }

    private fun buildWavHeader(dataSize: Int, sampleRate: Int, channelCount: Int): ByteArray {
        val byteRate = sampleRate * channelCount * BYTES_PER_SAMPLE
        val blockAlign = channelCount * BYTES_PER_SAMPLE
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16)
        header.putShort(1) // PCM
        header.putShort(channelCount.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign.toShort())
        header.putShort((BYTES_PER_SAMPLE * 8).toShort())
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(dataSize)
        return header.array()
    }
}
