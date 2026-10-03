package com.indoone.home.vibe

import com.indoone.menu.IndooneLogo

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

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

        IndooneLogo(
            modifier = Modifier.size(16.dp),
        )
    }
}
