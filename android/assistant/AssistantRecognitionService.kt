package com.indoone.assistant

import android.content.Intent
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/**
 * The Vibe assistant captures its own audio inside AssistantSession, so this
 * service never transcribes anything itself. It still has to answer the system:
 * a listening callback that is never answered leaves the voice interaction
 * session waiting until it fails.
 */
class AssistantRecognitionService : RecognitionService() {
    override fun onStartListening(intent: Intent?, listener: Callback) {
        listener.error(SpeechRecognizer.ERROR_CLIENT)
    }

    override fun onStopListening(listener: Callback) = Unit

    override fun onCancel(listener: Callback) = Unit
}
