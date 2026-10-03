package com.indoone.home.vibe


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
private fun IndooneVibeLogo(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / 48f
        scale(scaleFactor) {
            rotate(
                45f,
                pivot = androidx.compose.ui.geometry.Offset(24f, 24f),
            ) {
                drawRoundRect(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFFC15CFF),
                            androidx.compose.ui.graphics.Color(0xFF7C3AED),
                            androidx.compose.ui.graphics.Color(0xFF22C7FF),
                        ),
                        start = androidx.compose.ui.geometry.Offset(11f, 11f),
                        end = androidx.compose.ui.geometry.Offset(37f, 37f),
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(11f, 11f),
                    size = androidx.compose.ui.geometry.Size(26f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                )
            }
            val outer = androidx.compose.ui.graphics.Path().apply {
                moveTo(24f, 14f); lineTo(27.2f, 20.8f); lineTo(34f, 24f); lineTo(27.2f, 27.2f)
                lineTo(24f, 34f); lineTo(20.8f, 27.2f); lineTo(14f, 24f); lineTo(20.8f, 20.8f); close()
            }
            drawPath(outer, color = androidx.compose.ui.graphics.Color(0xFF0A0A18))
            val inner = androidx.compose.ui.graphics.Path().apply {
                moveTo(24f, 20.8f); lineTo(25.2f, 22.8f); lineTo(27.2f, 24f); lineTo(25.2f, 25.2f)
                lineTo(24f, 27.2f); lineTo(22.8f, 25.2f); lineTo(20.8f, 24f); lineTo(22.8f, 22.8f); close()
            }
            drawPath(inner, color = androidx.compose.ui.graphics.Color(0xFF60A5FA))
        }
    }
}

@Composable
fun VibeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "indoone-vibe")
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "vibe-orbit",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "vibe-pulse",
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF21143B))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val coreRadius = (size.minDimension * 0.17f) * pulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE3C8FF).copy(alpha = 0.96f),
                        Color(0xFF9A5CFF).copy(alpha = 0.78f),
                        Color(0xFF6F35DA).copy(alpha = 0.26f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.minDimension * 0.49f,
                ),
                radius = size.minDimension * 0.49f,
                center = center,
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.94f),
                radius = coreRadius,
                center = center,
            )

            rotate(orbit, pivot = center) {
                val orbitAngle = Math.toRadians(orbit.toDouble())
                val orbitCenter = Offset(
                    center.x + (size.minDimension * 0.25f * cos(orbitAngle)).toFloat(),
                    center.y + (size.minDimension * 0.25f * sin(orbitAngle)).toFloat(),
                )
                drawCircle(
                    color = Color(0xFFD6B7FF),
                    radius = size.minDimension * 0.055f,
                    center = orbitCenter,
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = size.minDimension * 0.025f,
                    center = orbitCenter,
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.24f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.28f, size.height * 0.24f),
                    radius = size.minDimension * 0.20f,
                ),
                radius = size.minDimension * 0.20f,
                center = Offset(size.width * 0.28f, size.height * 0.24f),
            )
        }

        IndooneVibeLogo(
            modifier = Modifier.size(16.dp),
        )
    }
}
