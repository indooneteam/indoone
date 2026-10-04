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
import java.io.File
import java.io.IOException
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
        private const val MODEL_UUID = "indoone-vosk-en-0.3.75"
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

        Thread {
            runCatching {
                val targetRoot = prepareModelFiles()
                Log.i(TAG, "Wake model files ready at \${targetRoot.absolutePath}")
                Model(targetRoot.absolutePath)
            }.onSuccess { loaded ->
                model = loaded
                mainHandler.post(onReady)
            }.onFailure { error ->
                val message =
                    "Offline wake model failed: \${error.message ?: error::class.java.simpleName}"
                Log.e(TAG, message, error)
                mainHandler.post { onError(message) }
            }
        }.apply {
            name = "Indoone-Wake-Model"
            isDaemon = true
            start()
        }
    }

    private fun prepareModelFiles(): File {
        val externalRoot = context.getExternalFilesDir(null)
            ?: throw IOException("External app storage is unavailable")

        val targetRoot = File(externalRoot, MODEL_DIR)
        val targetModel = File(targetRoot, MODEL_ASSET)
        val targetUuid = File(targetModel, "uuid")

        if (
            targetUuid.exists() &&
            File(targetModel, "am/final.mdl").exists()
        ) {
            return targetRoot
        }

        if (targetRoot.exists()) {
            targetRoot.deleteRecursively()
        }
        targetModel.mkdirs()

        copyAssetTree(MODEL_ASSET, targetModel)

        if (!targetUuid.exists()) {
            targetUuid.writeText(MODEL_UUID)
        }

        verifyModelFiles(targetModel)
        return targetRoot
    }

    private fun copyAssetTree(assetPath: String, targetDir: File) {
        val children = context.assets.list(assetPath)
            ?: throw IOException("Wake model asset is missing: \${assetPath}")

        if (children.isEmpty()) {
            targetDir.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input ->
                targetDir.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return
        }

        targetDir.mkdirs()

        for (child in children) {
            val childAsset = "\$assetPath/\$child"
            val target = File(targetDir, child)
            val nested = context.assets.list(childAsset) ?: emptyArray()

            if (nested.isEmpty()) {
                context.assets.open(childAsset).use { input ->
                    target.parentFile?.mkdirs()
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                copyAssetTree(childAsset, target)
            }
        }
    }

    private fun verifyModelFiles(targetModel: File) {
        val required = listOf(
            File(targetModel, "uuid"),
            File(targetModel, "am/final.mdl"),
            File(targetModel, "conf/model.conf"),
            File(targetModel, "graph/Gr.fst"),
            File(targetModel, "graph/HCLr.fst"),
        )

        val missing = required.filterNot(File::exists)
        if (missing.isNotEmpty()) {
            throw IOException(
                "Wake model is incomplete; missing \${missing.joinToString { it.relativeTo(targetModel).path }}",
            )
        }
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
                Log.i(TAG, "Silent offline wake detector is listening")

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
                    mainHandler.post { onError("Microphone permission was lost") }
                }
            } catch (error: Exception) {
                if (running.get()) {
                    Log.e(TAG, "Wake detection loop failed", error)
                    mainHandler.post {
                        onError("Wake detection failed: \${error.message ?: "unknown error"}")
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
            normalized.contains("hey in to one")
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
