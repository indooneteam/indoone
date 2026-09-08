package com.indoone.accounts.addaccount.scanqr

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

/**
 * Scan QR Code screen matching the current main reference layout.
 *
 * The camera surface is responsive and keeps a 3:4 preview ratio.
 * QR decoding is isolated in the camera analyzer so the Composable remains UI-focused.
 */
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
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onCameraPermissionChanged(granted)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            onCameraPermissionChanged(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        ScanQrTopBar(
            onMenuClick = onMenuClick,
            onSearchClick = onSearchClick,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Scan QR Code",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF242129),
            )

            Text(
                text = "Place the TOTP QR code inside the frame.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                textAlign = TextAlign.Start,
                fontSize = 10.sp,
                color = Color(0xFF3B3741),
            )

            Spacer(modifier = Modifier.height(22.dp))

            CameraScannerSurface(
                hasPermission = state.hasCameraPermission,
                onCameraStarting = onCameraStarting,
                onCameraReady = onCameraReady,
                onCameraError = onCameraError,
                onQrDetected = onQrDetected,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
            )

            Text(
                text = state.statusMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp),
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                color = Color(0xFF77717F),
            )

            TextButton(
                onClick = onCancelScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                border = BorderStroke(1.dp, Color(0xFFE4DDEA)),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "Cancel Scan",
                    color = Color(0xFF5F566B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        ScanQrBottomNav(
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun ScanQrTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0EEF5)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onMenuClick) {
                Text(
                    text = "☰",
                    fontSize = 14.sp,
                    color = Color(0xFF242129),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF703BE2)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✦",
                        color = Color.White,
                        fontSize = 9.sp,
                    )
                }

                Text(
                    text = "Indoone",
                    color = Color(0xFF6B34DF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            TextButton(onClick = onSearchClick) {
                Text(
                    text = "⌕",
                    fontSize = 18.sp,
                    color = Color(0xFF242129),
                )
            }
        }
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF111111))
            .aspectRatio(3f / 4f),
        contentAlignment = Alignment.Center,
    ) {
        if (hasPermission) {
            CameraPreview(
                onCameraStarting = onCameraStarting,
                onCameraReady = onCameraReady,
                onCameraError = onCameraError,
                onQrDetected = onQrDetected,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = "Camera permission is required.",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.66f)
                .fillMaxHeight(0.64f)
                .align(Alignment.Center)
                .background(Color.Transparent)
                .then(ScanFrameBorderModifier()),
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
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var scanner: BarcodeScanner? = null
        var analysisExecutor = ContextCompat.getMainExecutor(context)

        latestCameraStarting()

        val cameraProviderListener = cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }

                scanner = BarcodeScanning.getClient()

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(analysisExecutor) { imageProxy ->
                            analyzeFrame(
                                scanner = scanner,
                                imageProxy = imageProxy,
                                onQrDetected = latestQrDetected,
                                onError = latestCameraError,
                            )
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )

                latestCameraReady()
            } catch (error: Exception) {
                latestCameraError(
                    error.message ?: "Unable to open the camera."
                )
            }
        }, analysisExecutor)

        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (_: Exception) {
                // Camera may already be released by the lifecycle.
            }
            scanner?.close()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}

private fun analyzeFrame(
    scanner: BarcodeScanner?,
    imageProxy: ImageProxy,
    onQrDetected: (String) -> Unit,
    onError: (String) -> Unit,
) {
    val mediaImage = imageProxy.image

    if (scanner == null || mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees,
    )

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val rawValue = barcodes
                .asSequence()
                .mapNotNull { it.rawValue?.trim() }
                .firstOrNull { it.startsWith("otpauth://", ignoreCase = true) }

            if (!rawValue.isNullOrBlank()) {
                onQrDetected(rawValue)
            }
        }
        .addOnFailureListener { error ->
            onError(error.message ?: "Unable to read this QR code.")
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

@Composable
private fun ScanQrBottomNav(
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEAF2)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScanQrBottomNavItem(
                modifier = Modifier.weight(1f),
                icon = "♟",
                label = "Accounts",
                active = true,
                onClick = onAccountsClick,
            )
            ScanQrBottomNavItem(
                modifier = Modifier.weight(1f),
                icon = "◆",
                label = "Lobby",
                active = false,
                onClick = onLobbyClick,
            )
            ScanQrBottomNavItem(
                modifier = Modifier.weight(1f),
                icon = "↔",
                label = "Connect",
                active = false,
                onClick = onConnectClick,
            )
            ScanQrBottomNavItem(
                modifier = Modifier.weight(1f),
                icon = "☷",
                label = "Settings",
                active = false,
                onClick = onSettingsClick,
            )
        }
    }
}

@Composable
private fun ScanQrBottomNavItem(
    modifier: Modifier,
    icon: String,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val color = if (active) Color(0xFF6B34DF) else Color(0xFF99939F)

    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = icon,
                color = color,
                fontSize = 11.sp,
            )
            Text(
                text = label,
                color = color,
                fontSize = 7.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

private fun ScanFrameBorderModifier(): Modifier {
    return Modifier
        .background(Color.Transparent)
        .then(
            Modifier
        )
}
