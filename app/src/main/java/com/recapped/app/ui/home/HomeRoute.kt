package com.recapped.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.domain.model.Artist
import com.recapped.app.ui.components.ArtistAvatar
import com.recapped.app.ui.components.GlowSpot
import com.recapped.app.ui.components.Glows
import com.recapped.app.ui.components.RecappedChip
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
                    xFraction = 0.55f, yFraction = -0.08f, radiusFraction = 0.55f,
                    color = Color(0xFFCC1500).copy(alpha = 0.35f), drift = true
                ),
                GlowSpot(
                    xFraction = -0.15f, yFraction = 0.45f, radiusFraction = 0.45f,
                    color = Color(0xFFD4A017).copy(alpha = 0.18f), drift = true
                )
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header(state.header) }

            when (val phase = state.phase) {
                HomePhase.Loading -> {
                    item { LoadingBlock() }
                }
                is HomePhase.Error -> {
                    item { ErrorBlock(phase.message, onRetry) }
                }
                is HomePhase.Success -> {
                    item {
                        StatsRow(
                            topArtist = phase.topArtists.firstOrNull(),
                            totalArtists = phase.totalArtists,
                            totalScrobbles = phase.totalScrobbles
                        )
                    }
                    item { RecapCta(onClick = onGoToRecap) }
                    item { SectionLabel("Más escuchados") }
                    items(phase.topArtists, key = { it.mbid.ifBlank { it.name } }) { artist ->
                        ArtistRow(artist = artist, onClick = { onArtistClick(artist.name) })
                    }
                }
            }
        }
    }
}

// ── Header con avatar ────────────────────────────────────────────────────────
@Composable
private fun Header(header: HomeHeader) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "BIENVENIDO",
                color = RecappedColors.Muted,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = header.displayName,
                style = TextStyle(
                    brush = BrandGradient,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp
                )
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrandGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = header.initial,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ── Grid de 3 stats ──────────────────────────────────────────────────────────
@Composable
private fun StatsRow(topArtist: Artist?, totalArtists: Int, totalScrobbles: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            value = "#1",
            label = topArtist?.name?.take(10) ?: "Top global",
            highlighted = true,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = compact(totalScrobbles),
            label = "Scrobbles",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = totalArtists.toString(),
            label = "Artistas",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(RecappedColors.Surface)
            .border(
                0.5.dp,
                if (highlighted) RecappedColors.BrandOrange.copy(alpha = 0.5f) else RecappedColors.Border,
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (highlighted) {
                Text(
                    text = value,
                    style = TextStyle(
                        brush = BrandGradient,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                )
            } else {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = label.uppercase(),
                color = if (highlighted) RecappedColors.BrandOrange else RecappedColors.Dim,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

// ── Recap CTA (placeholder hasta tener recap real) ──────────────────────────
@Composable
private fun RecapCta(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(RecappedColors.Surface)
            .border(0.5.dp, RecappedColors.Border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        // Glow esquina sup. derecha — replica el efecto de la mock
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(110.dp)
                .offset(x = 20.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(RecappedColors.BrandOrange.copy(alpha = 0.18f))
        )
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RecappedChip(text = "Tu primer recap", selected = false)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Ver →",
                    style = TextStyle(
                        brush = BrandGradient,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Generá tu recap",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Elegí un período y te armamos un resumen de tus hábitos musicales.",
                color = RecappedColors.Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(RecappedColors.BrandOrange.copy(alpha = 0.6f))
            )
        }
    }
}

// ── Section label ───────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = RecappedColors.Muted,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// ── Artist row (lista de top) ───────────────────────────────────────────────
@Composable
private fun ArtistRow(artist: Artist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RecappedColors.Surface)
            .border(0.5.dp, RecappedColors.Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(BrandGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = artist.rank.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.width(12.dp))
        ArtistAvatar(name = artist.name, imageUrl = artist.imageUrl, size = 46.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = artist.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${compact(artist.playcount)} scrobbles",
                color = RecappedColors.Muted,
                fontSize = 11.sp
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
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry, shape = CircleShape) { Text("Reintentar") }
    }
}

/** "23456" → "23.4K", "1200000" → "1.2M". Igual que la mock. */
private fun compact(n: Long): String = when {
    n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000.0)
    n >= 1_000 -> String.format("%.1fK", n / 1_000.0)
    else -> n.toString()
}
