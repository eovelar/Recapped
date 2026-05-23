package com.recapped.app.ui.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.domain.model.Artist
import com.recapped.app.ui.components.GlowSpot
import com.recapped.app.ui.components.Glows
import com.recapped.app.ui.components.ArtistAvatar
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

@Composable
fun HomeRoute(
    onArtistClick: (String) -> Unit,
    onGoToRecap: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onRetry = viewModel::load,
        onArtistClick = onArtistClick,
        onGoToRecap = onGoToRecap
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onArtistClick: (String) -> Unit,
    onGoToRecap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
    ) {
        Glows(
            spots = listOf(
                GlowSpot(
                    xFraction = 0.64f,
                    yFraction = -0.10f,
                    radiusFraction = 0.55f,
                    color = RecappedColors.BrandOrange.copy(alpha = 0.36f),
                    drift = true
                ),
                GlowSpot(
                    xFraction = 0.94f,
                    yFraction = 0.30f,
                    radiusFraction = 0.42f,
                    color = RecappedColors.BrandOrange.copy(alpha = 0.18f),
                    drift = true
                )
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 22.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Header(state.header)
            }

            when (val phase = state.phase) {
                HomePhase.Loading -> {
                    item {
                        LoadingBlock()
                    }
                }

                is HomePhase.Error -> {
                    item {
                        ErrorBlock(
                            message = phase.message,
                            onRetry = onRetry
                        )
                    }
                }

                is HomePhase.Success -> {
                    item {
                        StatsRow(
                            totalScrobbles = phase.totalScrobbles,
                            totalArtists = phase.totalArtists
                        )
                    }

                    item {
                        SectionLabel("Último Recap")
                    }

                    item {
                        LastRecapCard(
                            topArtist = phase.topArtists.firstOrNull(),
                            totalArtists = phase.totalArtists,
                            onClick = onGoToRecap
                        )
                    }

                    item {
                        SectionLabel("Más escuchados")
                    }

                    itemsIndexed(
                        items = phase.topArtists.take(3),
                        key = { _, artist -> artist.mbid.ifBlank { artist.name } }
                    ) { index, artist ->
                        ArtistRow(
                            position = index + 1,
                            artist = artist,
                            onClick = { onArtistClick(artist.name) }
                        )
                    }

                    item {
                        SectionLabel("¿Lo olvidaste?")
                    }

                    item {
                        val forgottenArtist = phase.topArtists.drop(3).firstOrNull()
                            ?: phase.topArtists.firstOrNull()

                        ForgottenArtistCard(
                            artist = forgottenArtist,
                            onClick = {
                                forgottenArtist?.let { artist ->
                                    onArtistClick(artist.name)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun Header(header: HomeHeader) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "BIENVENIDO",
                color = RecappedColors.Dim,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = header.displayName,
                style = TextStyle(
                    brush = BrandGradient,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.1).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(BrandGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = header.initial,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ── Stats ───────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    totalScrobbles: Long,
    totalArtists: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            value = "23",
            label = "Racha",
            highlighted = true,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            value = compact(totalScrobbles),
            label = "Scrobbles",
            highlighted = false,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            value = totalArtists.toString(),
            label = "Artistas",
            highlighted = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (highlighted) {
                    RecappedColors.BrandOrange.copy(alpha = 0.10f)
                } else {
                    RecappedColors.Surface
                }
            )
            .border(
                width = 0.7.dp,
                color = if (highlighted) {
                    RecappedColors.BrandOrange.copy(alpha = 0.65f)
                } else {
                    RecappedColors.Border
                },
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (highlighted) {
            Text(
                text = value,
                style = TextStyle(
                    brush = BrandGradient,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 23.sp,
                    letterSpacing = (-0.8).sp
                ),
                maxLines = 1
            )
        } else {
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 23.sp,
                letterSpacing = (-0.8).sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = label.uppercase(),
            color = if (highlighted) RecappedColors.BrandOrange else RecappedColors.Dim,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            maxLines = 1
        )
    }
}

// ── Último Recap ────────────────────────────────────────────────────────────

@Composable
private fun LastRecapCard(
    topArtist: Artist?,
    totalArtists: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RecappedColors.Surface)
            .border(
                width = 0.7.dp,
                color = RecappedColors.Border,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 34.dp, y = (-18).dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(RecappedColors.BrandOrange.copy(alpha = 0.18f))
        )

        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(
                        width = 0.6.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(99.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Marzo 2025",
                    color = RecappedColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Recap Mensual",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "847 canciones · $totalArtists artistas · 62 horas",
                color = RecappedColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BrandGradient)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RecapMetric(
                    value = "#1",
                    label = topArtist?.name ?: "Top artista"
                )

                RecapMetric(
                    value = "31%",
                    label = "Psych rock"
                )

                RecapMetric(
                    value = "62h",
                    label = "Escuchadas"
                )
            }
        }
    }
}

@Composable
private fun RecapMetric(
    value: String,
    label: String
) {
    Column {
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = label.uppercase(),
            color = RecappedColors.Dim,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(92.dp)
        )
    }
}

// ── Secciones ───────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = RecappedColors.Dim,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(top = 2.dp)
    )
}

// ── Más escuchados ──────────────────────────────────────────────────────────

@Composable
private fun ArtistRow(
    position: Int,
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RecappedColors.Surface)
            .border(
                width = 0.7.dp,
                color = RecappedColors.Border,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(BrandGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position.toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(13.dp))

//        AlbumTile(
//            label = artist.name.firstOrNull()?.uppercase() ?: "?",
//            color = when (position) {
//                1 -> Color(0xFF4A1A87)
//                2 -> Color(0xFF4A1A87)
//                else -> Color(0xFF123A7A)
//            }
//        )
        ArtistAvatar(
            name = artist.name,
            imageUrl = artist.imageUrl,
            size = 46.dp
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = artist.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = "${compact(artist.playcount)} scrobbles",
                color = RecappedColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = RecappedColors.Dim,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── ¿Lo olvidaste? ──────────────────────────────────────────────────────────

@Composable
private fun ForgottenArtistCard(
    artist: Artist?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RecappedColors.Surface)
            .border(
                width = 0.7.dp,
                color = RecappedColors.Border,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = artist != null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumTile(
            label = artist?.name?.firstOrNull()?.uppercase() ?: "S",
            color = Color(0xFF0A5E47)
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = artist?.name ?: "Radiohead",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(1.dp))

            Row {
                Text(
                    text = "Hace ",
                    color = RecappedColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "47 días",
                    color = RecappedColors.BrandOrange,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = " que no lo escuchás",
                    color = RecappedColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        SpotifyBadge()

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = RecappedColors.Dim,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── Visuales auxiliares ─────────────────────────────────────────────────────

@Composable
private fun AlbumTile(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(color.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .offset(x = 10.dp, y = (-8).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SpotifyBadge() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFF1DB954)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val stroke = Stroke(width = 2.1f)

            drawArc(
                color = Color.Black.copy(alpha = 0.85f),
                startAngle = 205f,
                sweepAngle = 130f,
                useCenter = false,
                style = stroke,
                topLeft = Offset(1f, 3f),
                size = Size(size.width - 2f, size.height * 0.55f)
            )

            drawArc(
                color = Color.Black.copy(alpha = 0.85f),
                startAngle = 208f,
                sweepAngle = 125f,
                useCenter = false,
                style = stroke,
                topLeft = Offset(2f, 6f),
                size = Size(size.width - 4f, size.height * 0.45f)
            )

            drawArc(
                color = Color.Black.copy(alpha = 0.85f),
                startAngle = 210f,
                sweepAngle = 112f,
                useCenter = false,
                style = stroke,
                topLeft = Offset(3.2f, 9f),
                size = Size(size.width - 6.4f, size.height * 0.34f)
            )
        }
    }
}

// ── Estados ─────────────────────────────────────────────────────────────────

@Composable
private fun LoadingBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = RecappedColors.BrandOrange)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Cargando tu historial musical...",
            color = RecappedColors.Muted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorBlock(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRetry,
            shape = CircleShape
        ) {
            Text("Reintentar")
        }
    }
}

private fun compact(n: Long): String = when {
    n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000.0)
    n >= 1_000 -> String.format("%.1fK", n / 1_000.0)
    else -> n.toString()
}