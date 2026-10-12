package com.indoone.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class AssistantPermissionActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startResumeService()
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startResumeService()
            finish()
            return
        }

        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startResumeService() {
        startService(
            Intent(this, AssistantService::class.java).apply {
                action = AssistantService.ACTION_RESUME_WAKE
            },
        )
    }
}
