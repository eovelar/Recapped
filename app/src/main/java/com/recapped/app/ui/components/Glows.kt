package com.recapped.app.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * "Glows" — manchas radiales suaves de color que decoran el fondo de las pantallas.
 * Replican el efecto `radial-gradient + blur + drift` de la mock HTML pero en Compose,
 * sin necesidad de blur (que en Compose costaba mucho hasta API 31+).
 *
 * Cada spot se anima sutilmente con un float infinito (efecto "respira").
 */
data class GlowSpot(
    val xFraction: Float,   // 0f..1f del ancho
    val yFraction: Float,   // 0f..1f del alto
    val radiusFraction: Float = 0.35f, // tamaño relativo al min(w,h)
    val color: Color,
    val drift: Boolean = true
)

@Composable
fun Glows(spots: List<GlowSpot>, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "glow-drift")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val minDim = minOf(w, h)
            spots.forEach { spot ->
                val dx = if (spot.drift) (t - 0.5f) * 16f else 0f
                val dy = if (spot.drift) (0.5f - t) * 12f else 0f
                val cx = spot.xFraction * w + dx
                val cy = spot.yFraction * h + dy
                val r = spot.radiusFraction * minDim
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(spot.color, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
