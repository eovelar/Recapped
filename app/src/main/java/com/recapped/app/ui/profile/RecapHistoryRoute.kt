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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.Unbounded

data class RecapHistoryItem(
    val periodType: String,
    val title: String,
    val songs: Int,
    val topArtist: String,
    val glowColor: Color
)

private val mockRecaps = listOf(
    RecapHistoryItem(
        periodType = "Mensual",
        title = "Marzo 2026",
        songs = 847,
        topArtist = "Tame Impala",
        glowColor = Color(0xFFFF2A00)
    ),
    RecapHistoryItem(
        periodType = "Mensual",
        title = "Febrero 2026",
        songs = 723,
        topArtist = "Khruangbin",
        glowColor = Color(0xFFFF6A00)
    ),
    RecapHistoryItem(
        periodType = "Mensual",
        title = "Enero 2026",
        songs = 654,
        topArtist = "Stereolab",
        glowColor = Color(0xFFFFC400)
    ),
    RecapHistoryItem(
        periodType = "Trimestral",
        title = "Diciembre 2025",
        songs = 2341,
        topArtist = "Floating Points",
        glowColor = Color(0xFF7A35FF)
    ),
    RecapHistoryItem(
        periodType = "Trimestral",
        title = "Octubre 2025",
        songs = 2341,
        topArtist = "Floating Points",
        glowColor = Color(0xFF7A35FF)
    )
)

@Composable
fun RecapHistoryRoute(
    onBack: () -> Unit = {},
    onRecapClick: (RecapHistoryItem) -> Unit = {}
) {
    RecapHistoryScreen(
        recaps = mockRecaps,
        onBack = onBack,
        onRecapClick = onRecapClick
    )
}

@Composable
private fun RecapHistoryScreen(
    recaps: List<RecapHistoryItem>,
    onBack: () -> Unit,
    onRecapClick: (RecapHistoryItem) -> Unit
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

            items(recaps) { recap ->
                RecapHistoryCard(
                    recap = recap,
                    onClick = { onRecapClick(recap) }
                )
            }
        }
    }
}

@Composable
private fun RecapHistoryHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
    recap: RecapHistoryItem,
    onClick: () -> Unit
) {
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
                            recap.glowColor.copy(alpha = 0.32f),
                            recap.glowColor.copy(alpha = 0.10f),
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
                PeriodPill(text = recap.periodType)

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = recap.title,
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
                                recap.glowColor.copy(alpha = 0.78f),
                                recap.glowColor.copy(alpha = 0.22f),
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = recap.songs.toString(),
                        color = Color.White,
                        fontSize = 23.sp,
                        fontFamily = Unbounded,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.8).sp
                    )

                    Text(
                        text = "CANCIONES",
                        color = Color.White.copy(alpha = 0.24f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        lineHeight = 9.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Top artista",
                        color = Color.White.copy(alpha = 0.28f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = recap.topArtist,
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
private fun PeriodPill(
    text: String
) {
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