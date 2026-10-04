package com.indoone.assistant

import android.os.Build
import android.service.voice.VoiceInteractionService

class AssistantService : VoiceInteractionService() {
    override fun onReady() {
        super.onReady()
        if (Build.VERSION.SDK_INT >= 36) {
            runCatching { setInvocationEffectEnabled(true) }
        }
    }
}
