package com.recapped.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.domain.model.RecapPeriod
import com.recapped.app.domain.model.RecapResult
import com.recapped.app.domain.model.StoredRecap
import com.recapped.app.ui.theme.Unbounded
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecapHistoryRoute(
    onBack: () -> Unit = {},
    onRecapClick: (RecapResult) -> Unit = {},
    viewModel: RecapHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecapHistoryScreen(
        state = state,
        onBack = onBack,
        onRecapClick = onRecapClick
    )
}

@Composable
private fun RecapHistoryScreen(
    state: RecapHistoryUiState,
    onBack: () -> Unit,
    onRecapClick: (RecapResult) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF431500).copy(alpha = 0.55f),
                        Color.Transparent
                    ),
                    center = Offset(260f, 70f),
                    radius = 520f
                )
            )
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    color = Color(0xFFFF4B16),
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.Center)
                )
            }

            state.error != null && state.recaps.isEmpty() -> {
                HistoryMessage(
                    title = "No pudimos cargar el historial",
                    message = state.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                RecapHistoryList(
                    recaps = state.recaps,
                    onBack = onBack,
                    onRecapClick = onRecapClick
                )
            }
        }

        if (state.isLoading || state.error != null && state.recaps.isEmpty()) {
            RecapHistoryHeader(
                onBack = onBack,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun RecapHistoryList(
    recaps: List<StoredRecap>,
    onBack: () -> Unit,
    onRecapClick: (RecapResult) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 108.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            RecapHistoryHeader(onBack = onBack)
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (recaps.isEmpty()) {
            item {
                HistoryMessage(
                    title = "Todavía no hay recaps",
                    message = "Los recaps que generes aparecerán en este historial.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp)
                )
            }
        } else {
            items(
                items = recaps,
                key = { it.id }
            ) { storedRecap ->
                RecapHistoryCard(
                    recap = storedRecap.recap,
                    onClick = {
                        onRecapClick(storedRecap.recap)
                    }
                )
            }
        }
    }
}

@Composable
private fun RecapHistoryHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF070707))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.10f),
                    shape = CircleShape
                )
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Historial de recaps",
            color = Color.White,
            fontSize = 21.sp,
            fontFamily = Unbounded,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.7).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RecapHistoryCard(
    recap: RecapResult,
    onClick: () -> Unit
) {
    val glowColor = recapGlowColor(recap.period)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111).copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .size(74.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.32f),
                            glowColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                PeriodPill(text = recapPeriodLabel(recap.period))

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Abrir recap",
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatRecapDate(recap.generatedAt),
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = Unbounded,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.7).sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.78f),
                                glowColor.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recap.totalScrobbles.toString(),
                        color = Color.White,
                        fontSize = 23.sp,
                        fontFamily = Unbounded,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.8).sp
                    )

                    Text(
                        text = "REPRODUCCIONES",
                        color = Color.White.copy(alpha = 0.24f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        lineHeight = 9.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Top artista",
                        color = Color.White.copy(alpha = 0.28f),
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = recap.topArtists
                            .firstOrNull()
                            ?.name
                            ?: "Sin datos",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            fontFamily = Unbounded,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = message,
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PeriodPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(99.dp)
            )
            .padding(horizontal = 15.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.46f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun recapPeriodLabel(period: RecapPeriod): String {
    return when (period) {
        RecapPeriod.Week -> "Semanal"
        RecapPeriod.Month -> "Mensual"
        RecapPeriod.Quarter -> "Trimestral"
        RecapPeriod.Year -> "Anual"
    }
}

private fun recapGlowColor(period: RecapPeriod): Color {
    return when (period) {
        RecapPeriod.Week -> Color(0xFFFF2A00)
        RecapPeriod.Month -> Color(0xFFFF6A00)
        RecapPeriod.Quarter -> Color(0xFF7A35FF)
        RecapPeriod.Year -> Color(0xFFFFC400)
    }
}

private fun formatRecapDate(timestamp: Long): String {
    val formatter = SimpleDateFormat(
        "d 'de' MMMM 'de' yyyy",
        Locale("es", "AR")
    )

    return formatter.format(Date(timestamp))
        .replaceFirstChar { character ->
            character.uppercase()
        }
}