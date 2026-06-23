package com.recapped.app.ui.songdetail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.recapped.app.R
import com.recapped.app.domain.model.AlbumTrack
import com.recapped.app.domain.model.SongDetail
import com.recapped.app.ui.theme.RecappedColors
import com.recapped.app.ui.theme.Unbounded

private val SpotifyGreen = Color(0xFF1DB954)

@Composable
fun SongDetailRoute(
    artistName: String,
    trackName: String,
    spotifyCallbackUrl: String?,
    onSpotifyCallbackConsumed: () -> Unit,
    onBack: () -> Unit,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(artistName, trackName) {
        viewModel.load(
            artistName = artistName,
            trackName = trackName
        )
    }

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
            is SpotifyAction.Authorize -> {
                openUrl(context, action.url)
                viewModel.consumeSpotifyAction()
            }

            is SpotifyAction.OpenTrack -> {
                openUrl(context, action.url)
                viewModel.consumeSpotifyAction()
            }

            is SpotifyAction.Error -> {
                Toast.makeText(
                    context,
                    action.message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.consumeSpotifyAction()
            }

            SpotifyAction.Idle,
            SpotifyAction.Loading -> Unit
        }
    }

    SongDetailScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenSpotify = { deezerTrackId, isrc ->
            viewModel.openInSpotify(
                deezerTrackId = deezerTrackId,
                knownIsrc = isrc
            )
        }
    )
}

@Composable
private fun SongDetailScreen(
    state: SongDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenSpotify: (deezerTrackId: Long, isrc: String?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
    ) {
        when (val phase = state.phase) {
            SongDetailPhase.Loading -> {
                CircularProgressIndicator(
                    color = RecappedColors.BrandOrange,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is SongDetailPhase.Error -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = phase.message,
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onRetry,
                        shape = CircleShape
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            is SongDetailPhase.Success -> {
                SongContent(
                    detail = phase.detail,
                    spotifyLoading =
                        state.spotifyAction is SpotifyAction.Loading,
                    onOpenSpotify = onOpenSpotify
                )
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.60f))
                    .border(
                        width = 0.5.dp,
                        color = RecappedColors.Border,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun SongContent(
    detail: SongDetail,
    spotifyLoading: Boolean,
    onOpenSpotify: (deezerTrackId: Long, isrc: String?) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            GlideImage(
                model = detail.imageUrl,
                contentDescription = detail.albumTitle,
                contentScale = ContentScale.Crop,
                loading = placeholder(R.drawable.ic_splash_logo),
                failure = placeholder(R.drawable.ic_splash_logo),
                modifier = Modifier
                    .fillMaxSize()
                    .background(RecappedColors.SurfaceBright)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f),
                                RecappedColors.Background
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "CANCIÓN",
                color = RecappedColors.BrandOrange,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = detail.name,
                color = Color.White,
                fontFamily = Unbounded,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = detail.artistName,
                color = RecappedColors.Muted,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            SpotifyButton(
                loading = spotifyLoading,
                onClick = {
                    onOpenSpotify(
                        detail.deezerTrackId,
                        detail.isrc
                    )
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            AlbumInformation(detail)

            if (detail.albumTracks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "CANCIONES DEL ÁLBUM",
                    color = RecappedColors.Dim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                detail.albumTracks.forEachIndexed { index, track ->
                    val selected = track.name.equals(
                        detail.name,
                        ignoreCase = true
                    )

                    AlbumTrackRow(
                        position = index + 1,
                        track = track,
                        selected = selected,
                        onClick = {
                            onOpenSpotify(
                                track.deezerTrackId,
                                null
                            )
                        }
                    )

                    if (index < detail.albumTracks.lastIndex) {
                        Spacer(modifier = Modifier.height(7.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SpotifyButton(
    loading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(SpotifyGreen)
            .clickable(
                enabled = !loading,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.Black,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (loading) {
                "Buscando en Spotify..."
            } else {
                "Escuchar en Spotify"
            },
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AlbumInformation(
    detail: SongDetail
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RecappedColors.Surface)
            .border(
                width = 0.5.dp,
                color = RecappedColors.Border,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "ÁLBUM",
            color = RecappedColors.Dim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = detail.albumTitle,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        val metadata = buildList {
            detail.releaseDate
                ?.takeIf { it.isNotBlank() }
                ?.let { add(it.substringBefore("-")) }

            if (detail.trackCount > 0) {
                add("${detail.trackCount} canciones")
            }
        }.joinToString(" · ")

        if (metadata.isNotBlank()) {
            Text(
                text = metadata,
                color = RecappedColors.Muted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AlbumTrackRow(
    position: Int,
    track: AlbumTrack,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) {
                    RecappedColors.BrandOrange.copy(alpha = 0.12f)
                } else {
                    RecappedColors.Surface
                }
            )
            .border(
                width = if (selected) 1.dp else 0.5.dp,
                color = if (selected) {
                    RecappedColors.BrandOrange
                } else {
                    RecappedColors.Border
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = position.toString(),
            color = if (selected) {
                RecappedColors.BrandOrange
            } else {
                RecappedColors.Dim
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.SemiBold
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (selected) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "SELECCIONADA",
                    color = RecappedColors.BrandOrange,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Text(
            text = formatDuration(track.durationSeconds),
            color = RecappedColors.Dim,
            fontSize = 11.sp
        )
    }
}

private fun formatDuration(seconds: Int): String {
    if (seconds <= 0) {
        return "--:--"
    }

    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return "%d:%02d".format(
        minutes,
        remainingSeconds
    )
}

private fun openUrl(
    context: android.content.Context,
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
