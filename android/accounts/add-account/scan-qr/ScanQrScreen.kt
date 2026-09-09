package com.indoone.accounts.addaccount.scanqr

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun ScanQrScreen(
    state: ScanQrState,
    onCameraPermissionChanged: (Boolean) -> Unit = {},
    onCameraStarting: () -> Unit = {},
    onCameraReady: () -> Unit = {},
    onCameraError: (String) -> Unit = {},
    onQrDetected: (String) -> Unit = {},
    onCancelScan: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> onCameraPermissionChanged(granted) }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) onCameraPermissionChanged(true) else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppTopBar(onMenuClick = onMenuClick, onSearchClick = onSearchClick)
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹", color = Color(0xFF242129), fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.TextButton(onClick = onCancelScan, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    Text("Back", color = Color(0xFF242129), fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text("ADD ACCOUNT", color = Color(0xFF7650D8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                Text("Scan QR Code", Modifier.padding(top = 2.dp), color = Color(0xFF17151D), fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Place the TOTP QR code inside the frame.", Modifier.padding(top = 8.dp), color = Color(0xFF2E2A33), fontSize = 14.sp, lineHeight = 20.sp)
            }
            Spacer(Modifier.height(22.dp))
            CameraScannerSurface(
                hasPermission = state.hasCameraPermission,
                onCameraStarting = onCameraStarting,
                onCameraReady = onCameraReady,
                onCameraError = onCameraError,
                onQrDetected = onQrDetected,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)),
            )
            Text(state.statusMessage, Modifier.fillMaxWidth().padding(top = 10.dp), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color(0xFF77717F))
            OutlinedButton(onClick = onCancelScan, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Cancel Scan", color = Color(0xFF5F566B), fontWeight = FontWeight.Bold)
            }
        }
        AppBottomNav(
            activeTab = AppTab.ACCOUNTS,
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun CameraScannerSurface(
    hasPermission: Boolean,
    onCameraStarting: () -> Unit,
    onCameraReady: () -> Unit,
    onCameraError: (String) -> Unit,
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().background(Color(0xFF111111)).aspectRatio(3f / 4f), contentAlignment = Alignment.Center) {
        if (hasPermission) {
            CameraPreview(onCameraStarting, onCameraReady, onCameraError, onQrDetected, Modifier.fillMaxSize())
        } else {
            Text("Camera permission is required.", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 28.dp))
        }
        Box(
            modifier = Modifier.fillMaxWidth(0.66f).fillMaxHeight(0.64f).align(Alignment.Center)
                .border(1.5.dp, Color.White, RoundedCornerShape(15.dp)),
        )
    }
}

@Composable
private fun CameraPreview(
    onCameraStarting: () -> Unit,
    onCameraReady: () -> Unit,
    onCameraError: (String) -> Unit,
    onQrDetected: (String) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestQrDetected by rememberUpdatedState(onQrDetected)
    val latestCameraStarting by rememberUpdatedState(onCameraStarting)
    val latestCameraReady by rememberUpdatedState(onCameraReady)
    val latestCameraError by rememberUpdatedState(onCameraError)
    val previewView = remember(context) {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val analysisExecutor = ContextCompat.getMainExecutor(context)
        var scanner: BarcodeScanner? = null
        latestCameraStarting()
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply { surfaceProvider = previewView.surfaceProvider }
                scanner = BarcodeScanning.getClient()
                val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().apply {
                    setAnalyzer(analysisExecutor) { imageProxy ->
                        analyzeFrame(scanner, imageProxy, latestQrDetected, latestCameraError)
                    }
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                latestCameraReady()
            } catch (error: Exception) {
                latestCameraError(error.message ?: "Unable to open the camera.")
            }
        }, analysisExecutor)
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
            scanner?.close()
        }
    }
    AndroidView(factory = { previewView }, modifier = modifier)
}

private fun analyzeFrame(scanner: BarcodeScanner?, imageProxy: ImageProxy, onQrDetected: (String) -> Unit, onError: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (scanner == null || mediaImage == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val rawValue = barcodes.asSequence().mapNotNull { it.rawValue?.trim() }.firstOrNull { it.startsWith("otpauth://", ignoreCase = true) }
            if (!rawValue.isNullOrBlank()) onQrDetected(rawValue)
        }
        .addOnFailureListener { error -> onError(error.message ?: "Unable to read this QR code.") }
        .addOnCompleteListener { imageProxy.close() }
}
