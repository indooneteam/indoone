package com.indoone.assistant

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class AssistantWakeDetector(
    private val context: Context,
    private val onDetected: () -> Unit,
    private val onError: (String) -> Unit,
) {
    companion object {
        private const val TAG = "IndooneWake"
        private const val SAMPLE_RATE_HZ = 16_000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_BYTES = 3_200
        private const val MODEL_ASSET = "model-en-us"
        private const val MODEL_DIR = "indoone-wake-model"
        private const val GRAMMAR =
            "[\"hey indoone\",\"hey indo one\",\"hey indone\",\"hey andoone\",\"hey endoone\",\"hey into one\",\"hey in to one\",\"[unk]\"]"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val running = AtomicBoolean(false)

    @Volatile
    private var model: Model? = null

    @Volatile
    private var audioRecord: AudioRecord? = null

    private var worker: Thread? = null

    fun initialize(onReady: () -> Unit) {
        model?.let {
            onReady()
            return
        }

        LibVosk.setLogLevel(LogLevel.WARNINGS)

        StorageService.unpack(
            context.applicationContext,
            MODEL_ASSET,
            MODEL_DIR,
            { loaded ->
                model = loaded
                mainHandler.post(onReady)
            },
            { error ->
                val message = "Offline wake model failed to load: " + (error.message ?: "unknown error")
                Log.e(TAG, message, error)
                mainHandler.post { onError(message) }
            },
        )
    }

    fun start() {
        if (!running.compareAndSet(false, true)) return

        val loadedModel = model
        if (loadedModel == null) {
            running.set(false)
            onError("Offline wake model is not ready")
            return
        }

        worker = Thread {
            val minBuffer = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_HZ,
                CHANNEL_CONFIG,
                ENCODING,
            )
            if (minBuffer <= 0) {
                running.set(false)
                mainHandler.post { onError("Microphone buffer initialization failed") }
                return@Thread
            }

            val record = runCatching {
                AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE_HZ,
                    CHANNEL_CONFIG,
                    ENCODING,
                    minBuffer.coerceAtLeast(BUFFER_BYTES) * 2,
                )
            }.getOrElse { error ->
                running.set(false)
                mainHandler.post {
                    onError("Offline wake microphone failed: " + (error.message ?: "unknown error"))
                }
                return@Thread
            }

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                running.set(false)
                mainHandler.post { onError("Offline wake microphone could not be initialized") }
                return@Thread
            }

            val recognizer = runCatching {
                Recognizer(loadedModel, SAMPLE_RATE_HZ.toFloat(), GRAMMAR).apply {
                    setMaxAlternatives(1)
                    setWords(false)
                    setPartialWords(false)
                }
            }.getOrElse { error ->
                record.release()
                running.set(false)
                mainHandler.post {
                    onError("Offline wake recognizer failed: " + (error.message ?: "unknown error"))
                }
                return@Thread
            }

            audioRecord = record

            try {
                record.startRecording()

                val buffer = ByteArray(BUFFER_BYTES)
                while (running.get()) {
                    val count = record.read(
                        buffer,
                        0,
                        buffer.size,
                        AudioRecord.READ_BLOCKING,
                    )
                    if (count <= 0) continue

                    val accepted = runCatching {
                        recognizer.acceptWaveForm(buffer, count)
                    }.getOrDefault(false)

                    val resultJson = if (accepted) {
                        recognizer.getResult()
                    } else {
                        recognizer.getPartialResult()
                    }

                    if (containsWakePhrase(resultJson)) {
                        Log.i(TAG, "Wake phrase detected")
                        mainHandler.post {
                            if (running.compareAndSet(true, false)) {
                                onDetected()
                            }
                        }
                        break
                    }
                }
            } catch (security: SecurityException) {
                if (running.get()) {
                    mainHandler.post {
                        onError("Microphone permission was lost")
                    }
                }
            } catch (error: Exception) {
                if (running.get()) {
                    Log.e(TAG, "Wake detection loop failed", error)
                    mainHandler.post {
                        onError("Wake detection failed: " + (error.message ?: "unknown error"))
                    }
                }
            } finally {
                runCatching { record.stop() }
                runCatching { record.release() }
                runCatching { recognizer.close() }
                audioRecord = null
            }
        }.apply {
            name = "Indoone-Offline-Wake"
            isDaemon = true
            start()
        }
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        runCatching { audioRecord?.stop() }
        worker?.interrupt()
        worker = null
    }

    fun release() {
        stop()
        model?.let {
            runCatching { it.close() }
        }
        model = null
    }

    private fun containsWakePhrase(resultJson: String): Boolean {
        val text = runCatching {
            JSONObject(resultJson).optString("text").ifBlank {
                JSONObject(resultJson).optString("partial")
            }
        }.getOrDefault("")

        val normalized = normalize(text)
        return normalized.contains("hey indoone") ||
            normalized.contains("hey indo one") ||
            normalized.contains("hey indone") ||
            normalized.contains("hey andoone") ||
            normalized.contains("hey endoone") ||
            normalized.contains("hey into one") ||
            normalized.contains("hey in to one") ||
            normalized == "indoone" ||
            normalized == "indo one" ||
            normalized == "indone"
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
