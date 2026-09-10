package com.indoone.accounts.addaccount.scanqr

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns scanner status without keeping camera resources inside UI state.
 */
class ScanQrViewModel : ViewModel() {
    private val _state = MutableStateFlow(ScanQrState())

    val state: StateFlow<ScanQrState> = _state.asStateFlow()

    fun onCameraPermissionChanged(granted: Boolean) {
        _state.value = _state.value.copy(
            hasCameraPermission = granted,
            isScanning = granted,
            statusMessage = if (granted) {
                "Scanning for a TOTP QR code…"
            } else {
                "Camera permission is required to scan a QR code."
            },
            errorMessage = null,
        )
    }

    fun onCameraStarting() {
        _state.value = _state.value.copy(
            statusMessage = "Opening camera…",
            isScanning = false,
            errorMessage = null,
        )
    }

    fun onCameraReady() {
        _state.value = _state.value.copy(
            statusMessage = "Scanning for a TOTP QR code…",
            isScanning = true,
            errorMessage = null,
        )
    }

    fun onCameraError(message: String) {
        _state.value = _state.value.copy(
            statusMessage = message,
            isScanning = false,
            errorMessage = message,
        )
    }

    fun onQrDetected() {
        _state.value = _state.value.copy(
            statusMessage = "QR code detected.",
            isScanning = false,
        )
    }

    fun onScanCancelled() {
        _state.value = _state.value.copy(
            statusMessage = "Scanning cancelled.",
            isScanning = false,
        )
    }
}