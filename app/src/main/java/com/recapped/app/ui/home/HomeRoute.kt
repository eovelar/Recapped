package com.recapped.app.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.R
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ForgottenArtist
import com.recapped.app.ui.components.ArtistAvatar
import com.recapped.app.ui.components.GlowSpot
import com.recapped.app.ui.components.Glows
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

@Composable
fun HomeRoute(
    spotifyCallbackUrl: String?,
    onSpotifyCallbackConsumed: () -> Unit,
    onArtistClick: (String) -> Unit,
    onGoToRecap: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(spotifyCallbackUrl) {
        if (!spotifyCallbackUrl.isNullOrBlank()) {
            viewModel.onSpotifyAuthorizationCallback(
                spotifyCallbackUrl
            )
            onSpotifyCallbackConsumed()
        }
    }

    LaunchedEffect(state.spotifyAction) {
        when (val action = state.spotifyAction) {
            is HomeSpotifyAction.Authorize -> {
                openUrl(context, action.url)
                viewModel.consumeSpotifyAction()
            }

            is HomeSpotifyAction.OpenArtist -> {
                openUrl(context, action.url)
                viewModel.consumeSpotifyAction()
            }

            is HomeSpotifyAction.Error -> {
                Toast.makeText(
                    context,
                    action.message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.consumeSpotifyAction()
            }

            HomeSpotifyAction.Idle,
            HomeSpotifyAction.Loading -> Unit
        }
    }

    HomeScreen(
        state = state,
        onRetry = viewModel::load,
        onArtistClick = onArtistClick,
        onGoToRecap = onGoToRecap,
        onOpenSpotify = viewModel::openForgottenArtistInSpotify
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onArtistClick: (String) -> Unit,
    onGoToRecap: () -> Unit,
    onOpenSpotify: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Glows(
            spots = listOf(
                GlowSpot(
                    xFraction = 0.72f,
                    yFraction = -0.08f,
                    radiusFraction = 0.50f,
                    color = RecappedColors.BrandOrange.copy(alpha = 0.22f),
                    drift = true
                ),
                GlowSpot(
                    xFraction = 1.02f,
                    yFraction = 0.32f,
                    radiusFraction = 0.42f,
                    color = RecappedColors.BrandOrange.copy(alpha = 0.13f),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        val forgottenArtist =
                            state.forgottenArtist

                        when {
                            state.isLoadingForgottenArtist -> {
                                ForgottenArtistLoadingCard()
                            }

                            forgottenArtist != null -> {
                                ForgottenArtistCard(
                                    artist = forgottenArtist,
                                    spotifyLoading =
                                        state.spotifyAction is
                                                HomeSpotifyAction.Loading,
                                    onClick = {
                                        onArtistClick(
                                            forgottenArtist.name
                                        )
                                    },
                                    onOpenSpotify = onOpenSpotify
                                )
                            }

                            else -> {
                                ForgottenArtistEmptyCard()
                            }
                        }
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
                color = Color.White.copy(alpha = 0.32f),
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
            .height(62.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (highlighted) {
                    Color(0xFF160C06).copy(alpha = 0.95f)
                } else {
                    Color(0xFF111111).copy(alpha = 0.95f)
                }
            )
            .border(
                width = 0.8.dp,
                color = if (highlighted) {
                    RecappedColors.BrandOrange.copy(alpha = 0.70f)
                } else {
                    Color.White.copy(alpha = 0.12f)
                },
                shape = RoundedCornerShape(15.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            color = if (highlighted) RecappedColors.BrandOrange else Color.White,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 23.sp,
            letterSpacing = (-0.8).sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = label.uppercase(),
            color = if (highlighted) {
                RecappedColors.BrandOrange.copy(alpha = 0.85f)
            } else {
                Color.White.copy(alpha = 0.26f)
            },
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            letterSpacing = 1.3.sp,
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
            .height(156.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF111111).copy(alpha = 0.96f))
            .border(
                width = 0.8.dp,
                color = Color.White.copy(alpha = 0.13f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF6A00).copy(alpha = 0.42f),
                        Color(0xFFFF6A00).copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(
                        x = size.width * 0.86f,
                        y = size.height * 0.38f
                    ),
                    radius = size.width * 0.36f
                ),
                radius = size.width * 0.36f,
                center = Offset(
                    x = size.width * 0.86f,
                    y = size.height * 0.38f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A1A87).copy(alpha = 0.18f),
                        Color(0xFF4A1A87).copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(
                        x = size.width * 0.10f,
                        y = size.height * 0.86f
                    ),
                    radius = size.width * 0.28f
                ),
                radius = size.width * 0.28f,
                center = Offset(
                    x = size.width * 0.10f,
                    y = size.height * 0.86f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(
                        width = 0.7.dp,
                        color = Color.White.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(99.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Marzo 2025",
                    color = Color.White.copy(alpha = 0.58f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(11.dp))

            Text(
                text = "Recap Mensual",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "847 canciones · $totalArtists artistas · 62 horas",
                color = Color.White.copy(alpha = 0.38f),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                RecappedColors.BrandOrange.copy(alpha = 0.85f),
                                RecappedColors.BrandOrange.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(9.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.Top
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
    Column(
        modifier = Modifier.width(70.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = value,
            color = Color.White,
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            letterSpacing = (-0.4).sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.22f),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 7.sp,
            letterSpacing = 0.8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Secciones ───────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.32f),
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
            .background(Color(0xFF111111).copy(alpha = 0.92f))
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = 0.11f),
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
                color = Color.White.copy(alpha = 0.38f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.26f),
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── ¿Lo olvidaste? ──────────────────────────────────────────────────────────

@Composable
private fun ForgottenArtistCard(
    artist: ForgottenArtist,
    spotifyLoading: Boolean,
    onClick: () -> Unit,
    onOpenSpotify: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111).copy(alpha = 0.92f))
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = 0.11f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtistAvatar(
            name = artist.name,
            imageUrl = artist.imageUrl,
            size = 46.dp,
            cornerRadius = 9.dp
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

            Row {
                Text(
                    text = "Hace ",
                    color = Color.White.copy(alpha = 0.38f),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "${artist.daysSinceLastListen} días",
                    color = RecappedColors.BrandOrange,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = " que no lo escuchás",
                    color = Color.White.copy(alpha = 0.38f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        SpotifyBadge(
            loading = spotifyLoading,
            onClick = onOpenSpotify
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.26f),
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── Visuales auxiliares ─────────────────────────────────────────────────────

@Composable
private fun SpotifyBadge(
    loading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF1ED760))
            .clickable(
                enabled = !loading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.Black,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_spotify),
                contentDescription = "Abrir en Spotify",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ForgottenArtistLoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111).copy(alpha = 0.92f))
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = 0.11f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = RecappedColors.BrandOrange,
            strokeWidth = 2.dp,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ForgottenArtistEmptyCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111).copy(alpha = 0.92f))
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = 0.11f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Text(
            text = "No encontramos artistas olvidados en tu último año.",
            color = Color.White.copy(alpha = 0.42f),
            style = MaterialTheme.typography.bodySmall
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

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Cargando tu historial musical...",
            color = Color.White.copy(alpha = 0.42f),
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

private fun openUrl(
    context: Context,
    url: String
) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No encontramos una aplicación para abrir Spotify.",
            Toast.LENGTH_LONG
        ).show()
    }
}
