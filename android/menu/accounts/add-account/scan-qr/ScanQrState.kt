package com.indoone.accounts.addaccount.scanqr

/**
 * UI state for the Scan QR Code account-add screen.
 */
data class ScanQrState(
    val statusMessage: String = "Starting camera…",
    val isScanning: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val errorMessage: String? = null,
)