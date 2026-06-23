package com.recapped.app.ui.detail

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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.recapped.app.R
import com.recapped.app.domain.model.ArtistDetail
import com.recapped.app.domain.model.Track
import com.recapped.app.ui.components.RecappedChip
import com.recapped.app.ui.theme.RecappedColors
import com.recapped.app.ui.theme.Unbounded

private val SpotifyGreen = Color(0xFF1DB954)

@Composable
fun DetailRoute(
    artistName: String,
    spotifyCallbackUrl: String?,
    onSpotifyCallbackConsumed: () -> Unit,
    onSongClick: (trackName: String) -> Unit,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
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
            is ArtistSpotifyAction.Authorize -> {
                openUrl(context, action.url)
                viewModel.consumeSpotifyAction()
            }

            is ArtistSpotifyAction.OpenArtist -> {
                openUrl(context, action.url)
                viewModel.consumeSpotifyAction()
            }

            is ArtistSpotifyAction.Error -> {
                Toast.makeText(
                    context,
                    action.message,
                    Toast.LENGTH_LONG
                ).show()

                viewModel.consumeSpotifyAction()
            }

            ArtistSpotifyAction.Idle,
            ArtistSpotifyAction.Loading -> Unit
        }
    }

    DetailScreen(
        state = state,
        onBack = onBack,
        onRetry = {
            viewModel.load(artistName)
        },
        onOpenSpotify = viewModel::openArtistInSpotify,
        onSongClick = onSongClick
    )
}

@Composable
private fun DetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenSpotify: (artistName: String) -> Unit,
    onSongClick: (trackName: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
    ) {
        when (val phase = state.phase) {
            DetailPhase.Loading -> {
                CenterContent {
                    CircularProgressIndicator(
                        color = RecappedColors.BrandOrange
                    )
                }
            }

            is DetailPhase.Error -> {
                CenterContent {
                    Text(
                        text = phase.message,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onRetry,
                        shape = CircleShape
                    ) {
                        Text(
                            androidx.compose.ui.res.stringResource(
                                R.string.retry
                            )
                        )
                    }
                }
            }

            is DetailPhase.Success -> {
                Content(
                    detail = phase.detail,
                    spotifyLoading =
                        state.spotifyAction is ArtistSpotifyAction.Loading,
                    onOpenSpotify = onOpenSpotify,
                    onSongClick = onSongClick
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
                    .background(Color.Black.copy(alpha = 0.5f))
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
private fun Content(
    detail: ArtistDetail,
    spotifyLoading: Boolean,
    onOpenSpotify: (artistName: String) -> Unit,
    onSongClick: (trackName: String) -> Unit
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            GlideImage(
                model = detail.artist.imageUrl,
                contentDescription = detail.artist.name,
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
                                Color.Black.copy(alpha = 0.20f),
                                RecappedColors.Background
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp
                    )
            ) {
                if (detail.tags.isNotEmpty()) {
                    RecappedChip(
                        text = detail.tags
                            .first()
                            .replaceFirstChar { it.uppercase() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = detail.artist.name,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontFamily = Unbounded,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )

                Text(
                    text = "${detail.artist.playcount} scrobbles · " +
                            "${detail.artist.listeners} listeners",
                    color = RecappedColors.Muted,
                    fontSize = 12.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SpotifyButton(
                loading = spotifyLoading,
                onClick = {
                    onOpenSpotify(detail.artist.name)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!detail.bio.isNullOrBlank()) {
                SectionLabel("Sobre el artista")

                Text(
                    text = detail.bio,
                    color = RecappedColors.OnSurface.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            SectionLabel("Top canciones")

            detail.topTracks.forEachIndexed { index, track ->
                TrackRow(
                    rank = index + 1,
                    track = track,
                    onClick = {
                        onSongClick(track.name)
                    }
                )

                if (index < detail.topTracks.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
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
private fun SectionLabel(
    text: String
) {
    Text(
        text = text.uppercase(),
        color = RecappedColors.Dim,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(10.dp))
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun TrackRow(
    rank: Int,
    track: Track,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RecappedColors.Surface)
            .border(
                width = 0.5.dp,
                color = RecappedColors.Border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 14.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            color = RecappedColors.Dim,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(20.dp)
        )

        GlideImage(
            model = track.imageUrl,
            contentDescription = track.name,
            contentScale = ContentScale.Crop,
            loading = placeholder(R.drawable.ic_splash_logo),
            failure = placeholder(R.drawable.ic_splash_logo),
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(RecappedColors.SurfaceBright)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = track.name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CenterContent(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
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