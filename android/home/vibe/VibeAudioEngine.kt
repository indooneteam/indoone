package com.indoone.home.vibe

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Process
import android.util.Base64
import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class VibeAudioEngine(
    private val context: Context,
    private val onCapturedPcm: (ByteArray) -> Unit,
    private val onPlaybackEnabled: () -> Boolean,
) {
    companion object {
        private const val TAG = "IndooneVibeAudio"
        private const val INPUT_RATE_HZ = 16_000
        private const val OUTPUT_RATE_HZ = 24_000
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val AUDIO_CHUNK_BYTES = 3_200
    }

    private val recording = AtomicBoolean(false)
    private val stopped = AtomicBoolean(false)
    private val playbackGeneration = AtomicInteger(0)
    private val playbackExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var previousAudioMode = AudioManager.MODE_NORMAL
    private var previousSpeakerphoneOn = false
    private var routeConfigured = false
    private var recorder: AudioRecord? = null
    private var recordThread: Thread? = null
    private var player: AudioTrack? = null
    private var lastUnderrunCount = 0

    fun isRecording(): Boolean = recording.get()

    private fun routeAudioToLoudspeaker() {
        if (routeConfigured) return

        previousAudioMode = audioManager.mode
        previousSpeakerphoneOn = audioManager.isSpeakerphoneOn

        runCatching {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val speaker = audioManager.availableCommunicationDevices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }
            if (speaker != null) {
                runCatching { audioManager.setCommunicationDevice(speaker) }
            }
        } else {
            runCatching { audioManager.isSpeakerphoneOn = true }
        }

        // Keep the legacy routing path explicitly on loudspeaker as a fallback.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            audioManager.communicationDevice?.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        ) {
            runCatching { audioManager.isSpeakerphoneOn = true }
        }

        routeConfigured = true
    }

    private fun restoreAudioRoute() {
        if (!routeConfigured) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { audioManager.clearCommunicationDevice() }
        }
        runCatching { audioManager.isSpeakerphoneOn = previousSpeakerphoneOn }
        runCatching { audioManager.mode = previousAudioMode }
        routeConfigured = false
    }

    fun startRecording() {
        if (stopped.get() || !recording.compareAndSet(false, true)) return

        try {
            routeAudioToLoudspeaker()
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
                    while (recording.get() && !stopped.get()) {
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
        if (stopped.get() || !onPlaybackEnabled() || audioBase64.isBlank()) return

        // Capture the generation immediately. Interruption increments it so audio
        // queued before the interruption cannot leak into the next user turn.
        val generation = playbackGeneration.get()
        try {
            playbackExecutor.execute {
                if (stopped.get() ||
                    generation != playbackGeneration.get() ||
                    !onPlaybackEnabled()
                ) {
                    return@execute
                }

                // Decode off the Android UI/WebSocket callback thread. The same
                // single worker preserves chunk order through decode and playback.
                val pcm = runCatching {
                    Base64.decode(audioBase64, Base64.DEFAULT)
                }.getOrElse {
                    Log.w(TAG, "Dropping invalid Gemini Live audio chunk")
                    return@execute
                }
                if (pcm.isEmpty() ||
                    stopped.get() ||
                    generation != playbackGeneration.get() ||
                    !onPlaybackEnabled()
                ) {
                    return@execute
                }

                try {
                    val track = ensurePlayer()
                    var offset = 0
                    // AudioTrack can accept a partial write even in blocking mode.
                    // Loop until every sample is written, unless the turn is
                    // interrupted or the audio engine is stopping.
                    while (offset < pcm.size &&
                        !stopped.get() &&
                        generation == playbackGeneration.get() &&
                        onPlaybackEnabled()
                    ) {
                        val written = track.write(
                            pcm,
                            offset,
                            pcm.size - offset,
                            AudioTrack.WRITE_BLOCKING,
                        )
                        when {
                            written < 0 -> throw IllegalStateException(
                                "AudioTrack write failed with code $written",
                            )
                            written == 0 -> Thread.yield()
                            else -> offset += written
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val underruns = track.underrunCount
                        if (underruns > lastUnderrunCount) {
                            Log.w(
                                TAG,
                                "AudioTrack underruns increased from " +
                                    "$lastUnderrunCount to $underruns; Live audio may be arriving with gaps",
                            )
                            lastUnderrunCount = underruns
                        }
                    }
                } catch (error: IllegalStateException) {
                    if (generation == playbackGeneration.get() && !stopped.get()) {
                        Log.w(TAG, "Live audio playback write failed; flushing the player", error)
                        flushPlaybackInternal()
                    }
                }
            }
        } catch (_: RejectedExecutionException) {
            // The Vibe screen was closed while a final WebSocket audio event arrived.
        }
    }

    fun flushPlayback() {
        if (stopped.get()) return
        playbackGeneration.incrementAndGet()
        try {
            playbackExecutor.execute { flushPlaybackInternal() }
        } catch (_: RejectedExecutionException) {
            // Screen disposal can race a provider interruption event.
        }
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
        lastUnderrunCount = 0
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
        if (!stopped.compareAndSet(false, true)) return
        stopRecording()
        playbackGeneration.incrementAndGet()
        restoreAudioRoute()
        try {
            playbackExecutor.execute {
                player?.let {
                    runCatching { it.stop() }
                    runCatching { it.release() }
                }
                player = null
            }
            playbackExecutor.shutdown()
        } catch (_: RejectedExecutionException) {
            playbackExecutor.shutdownNow()
        }
    }
}
