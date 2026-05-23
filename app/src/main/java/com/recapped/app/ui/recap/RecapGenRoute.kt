package com.recapped.app.ui.recap

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recapped.app.R
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors
import kotlinx.coroutines.delay

private enum class RecapPeriod(
    val title: String,
    val subtitle: String
) {
    Week("Semana", "últimos 7 días"),
    Month("Mes", "últimos 30 días"),
    Quarter("Trimestre", "últimos 3 meses"),
    Year("Año", "últimos 12 meses")
}

@Composable
fun RecapGenRoute() {
    var selectedPeriod by remember { mutableStateOf(RecapPeriod.Month) }
    var isGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            delay(2500)

            // Próximo paso:
            // cuando exista la pantalla de resultados, reemplazar esto por:
            // navController.navigate("recap_result")

            isGenerating = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
    ) {
        RecapBackgroundGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            RecapTopBar()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 20.dp)
            ) {
                VinylHero(
                    isRotating = isGenerating
                )

                Text(
                    text = "PERIODO",
                    color = RecappedColors.Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RecapPeriod.entries.forEach { period ->
                        PeriodOptionCard(
                            period = period,
                            selected = period == selectedPeriod,
                            enabled = !isGenerating,
                            onClick = { selectedPeriod = period }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                GenerateRecapButton(
                    text = if (isGenerating) "Generando..." else "Generar Recap",
                    enabled = !isGenerating,
                    onClick = {
                        isGenerating = true
                    }
                )
            }
        }
    }
}

@Composable
private fun RecapTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f))
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Volver",
                tint = RecappedColors.Muted,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            text = "Tu recap",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.3).sp
        )
    }
}

@Composable
private fun VinylHero(
    isRotating: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_loader_animation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vinyl_glow_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.14f,
        targetValue = if (isRotating) 0.32f else 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vinyl_glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(326.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(248.dp)
                .graphicsLayer {
                    scaleX = if (isRotating) glowScale else 1f
                    scaleY = if (isRotating) glowScale else 1f
                }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RecappedColors.BrandOrange.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension * 0.56f
                        )
                    )
                }
        )

        Image(
            painter = painterResource(id = R.drawable.vinilo_separado),
            contentDescription = "Vinilo Recapped",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(292.dp)
                .graphicsLayer {
                    rotationZ = if (isRotating) rotation else 0f
                    shadowElevation = 18.dp.toPx()
                    shape = CircleShape
                    clip = false
                }
        )

        Image(
            painter = painterResource(id = R.drawable.pua),
            contentDescription = "Púa",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(292.dp)
                .graphicsLayer {
                    shadowElevation = 14.dp.toPx()
                    clip = false
                }
        )
    }
}

@Composable
private fun PeriodOptionCard(
    period: RecapPeriod,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val interaction = remember { MutableInteractionSource() }

    val cardModifier = if (selected) {
        Modifier
            .border(
                width = 0.8.dp,
                brush = BrandGradient,
                shape = shape
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        RecappedColors.BrandOrange.copy(alpha = 0.16f),
                        RecappedColors.BrandOrange.copy(alpha = 0.04f),
                        Color.White.copy(alpha = 0.03f)
                    )
                ),
                shape = shape
            )
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = RecappedColors.BrandOrange.copy(alpha = 0.22f),
                spotColor = RecappedColors.BrandOrange.copy(alpha = 0.22f)
            )
    } else {
        Modifier
            .border(
                width = 0.5.dp,
                color = RecappedColors.Border,
                shape = shape
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.055f),
                        Color.White.copy(alpha = 0.025f)
                    )
                ),
                shape = shape
            )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(cardModifier)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = period.title,
                color = when {
                    selected -> Color.White
                    enabled -> Color.White.copy(alpha = 0.42f)
                    else -> Color.White.copy(alpha = 0.22f)
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = period.subtitle,
                color = when {
                    selected -> RecappedColors.Muted
                    enabled -> Color.White.copy(alpha = 0.24f)
                    else -> Color.White.copy(alpha = 0.14f)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }

        SelectionDot(selected = selected)
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(BrandGradient)
                } else {
                    Modifier
                        .background(Color.Transparent)
                        .border(
                            width = 1.2.dp,
                            color = Color.White.copy(alpha = 0.22f),
                            shape = CircleShape
                        )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f))
            )
        }
    }
}

@Composable
private fun GenerateRecapButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = if (enabled) 18.dp else 6.dp,
                shape = CircleShape,
                ambientColor = RecappedColors.BrandOrange.copy(alpha = 0.28f),
                spotColor = RecappedColors.BrandOrange.copy(alpha = 0.34f)
            )
            .clip(CircleShape)
            .background(
                brush = if (enabled) {
                    BrandGradient
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp
        )
    }
}

@Composable
private fun RecapBackgroundGlow() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RecappedColors.BrandOrange.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = size.width * 0.5f,
                            y = size.height * 0.22f
                        ),
                        radius = size.width * 0.58f
                    )
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RecappedColors.BrandRed.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = size.width * 0.18f,
                            y = size.height * 0.82f
                        ),
                        radius = size.width * 0.46f
                    )
                )
            }
    )
}