package com.safezone.app.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chunked AAC recorder. Each call to [startChunk] creates a new m4a file; [stopChunk] returns it.
 * The SOSService calls these in a loop to upload short chunks.
 */
@Singleton
class ChunkedAudioRecorder @Inject constructor() {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private val recording = AtomicBoolean(false)

    fun startChunk(context: Context, eventId: String, index: Int): File {
        check(!recording.get()) { "Chunk already in progress" }
        val dir = File(context.cacheDir, "sos/$eventId").apply { mkdirs() }
        val file = File(dir, "chunk_${index.toString().padStart(4, '0')}.m4a")

        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }

        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(96_000)
        r.setAudioSamplingRate(44_100)
        r.setOutputFile(file.absolutePath)
        r.prepare()
        r.start()

        recorder = r
        currentFile = file
        recording.set(true)
        return file
    }

    fun stopChunk(): File? {
        if (!recording.get()) return null
        val r = recorder ?: return null
        runCatching { r.stop() }
        runCatching { r.release() }
        recorder = null
        val f = currentFile
        currentFile = null
        recording.set(false)
        return f
    }

    fun isRecording(): Boolean = recording.get()
}
