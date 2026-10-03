package com.indoone.home.vibe

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Process
import android.util.Base64
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class VibeAudioEngine(
    private val onCapturedPcm: (ByteArray) -> Unit,
    private val onPlaybackEnabled: () -> Boolean,
) {
    companion object {
        private const val INPUT_RATE_HZ = 16_000
        private const val OUTPUT_RATE_HZ = 24_000
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val AUDIO_CHUNK_BYTES = 3_200
    }

    private val recording = AtomicBoolean(false)
    private val playbackExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var recorder: AudioRecord? = null
    private var recordThread: Thread? = null
    private var player: AudioTrack? = null

    fun isRecording(): Boolean = recording.get()

    fun startRecording() {
        if (!recording.compareAndSet(false, true)) return

        try {
            val minBuffer = AudioRecord.getMinBufferSize(
                INPUT_RATE_HZ,
                CHANNEL_IN,
                ENCODING,
            )
            if (minBuffer <= 0) {
                recording.set(false)
                return
            }

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                INPUT_RATE_HZ,
                CHANNEL_IN,
                ENCODING,
                minBuffer.coerceAtLeast(AUDIO_CHUNK_BYTES) * 2,
            )
            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord.release()
                recording.set(false)
                return
            }

            recorder = audioRecord
            audioRecord.startRecording()

            recordThread = Thread {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                val buffer = ByteArray(AUDIO_CHUNK_BYTES)
                try {
                    while (recording.get()) {
                        val count = audioRecord.read(
                            buffer,
                            0,
                            buffer.size,
                            AudioRecord.READ_BLOCKING,
                        )
                        if (count > 0) {
                            onCapturedPcm(buffer.copyOf(count))
                        } else if (count < 0) {
                            break
                        }
                    }
                } finally {
                    runCatching { audioRecord.stop() }
                    runCatching { audioRecord.release() }
                    recorder = null
                }
            }.apply {
                name = "Indoone-Vibe-Recorder"
                start()
            }
        } catch (_: SecurityException) {
            recording.set(false)
        } catch (_: IllegalStateException) {
            recording.set(false)
        }
    }

    fun stopRecording() {
        if (!recording.compareAndSet(true, false)) return
        runCatching { recorder?.stop() }
        recordThread?.interrupt()
        recordThread = null
    }

    fun playResponse(audioBase64: String) {
        if (!onPlaybackEnabled() || audioBase64.isBlank()) return

        val pcm = runCatching {
            Base64.decode(audioBase64, Base64.DEFAULT)
        }.getOrNull() ?: return

        playbackExecutor.execute {
            if (!onPlaybackEnabled()) return@execute
            try {
                ensurePlayer().write(pcm, 0, pcm.size, AudioTrack.WRITE_BLOCKING)
            } catch (_: IllegalStateException) {
                flushPlaybackInternal()
            }
        }
    }

    fun flushPlayback() {
        playbackExecutor.execute { flushPlaybackInternal() }
    }

    private fun ensurePlayer(): AudioTrack {
        player?.takeIf { it.state == AudioTrack.STATE_INITIALIZED }?.let { return it }

        val minBuffer = AudioTrack.getMinBufferSize(
            OUTPUT_RATE_HZ,
            CHANNEL_OUT,
            ENCODING,
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(OUTPUT_RATE_HZ)
                    .setEncoding(ENCODING)
                    .setChannelMask(CHANNEL_OUT)
                    .build(),
            )
            .setBufferSizeInBytes(minBuffer.coerceAtLeast(9_600) * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        if (track.state != AudioTrack.STATE_INITIALIZED) {
            track.release()
            throw IllegalStateException("Audio playback could not be initialized.")
        }

        track.play()
        player = track
        return track
    }

    private fun flushPlaybackInternal() {
        player?.let { track ->
            runCatching {
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.pause()
                    track.flush()
                    track.play()
                }
            }
        }
    }

    fun stop() {
        stopRecording()
        playbackExecutor.execute {
            player?.let {
                runCatching { it.stop() }
                runCatching { it.release() }
            }
            player = null
        }
        playbackExecutor.shutdownNow()
    }
}
