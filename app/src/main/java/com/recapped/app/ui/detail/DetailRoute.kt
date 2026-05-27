package com.recapped.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

@Composable
fun DetailRoute(
    artistName: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailScreen(
        title = state.artistName.ifBlank { artistName },
        state = state,
        onBack = onBack,
        onRetry = { viewModel.load(artistName) }
    )
}

@Composable
fun DetailScreen(
    title: String,
    state: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
    ) {
        when (val phase = state.phase) {
            DetailPhase.Loading -> CenterContent {
                CircularProgressIndicator(color = RecappedColors.BrandOrange)
            }

            is DetailPhase.Error -> CenterContent {
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
                    Text(androidx.compose.ui.res.stringResource(R.string.retry))
                }
            }

            is DetailPhase.Success -> {
                Content(detail = phase.detail)
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
    detail: ArtistDetail
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
                .height(280.dp)
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
                            listOf(
                                Color.Black.copy(alpha = 0.25f),
                                RecappedColors.Background
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                if (detail.tags.isNotEmpty()) {
                    RecappedChip(
                        text = detail.tags.first().replaceFirstChar { it.uppercase() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = detail.artist.name,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontFamily = Unbounded,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.2).sp
                )

                Text(
                    text = "${detail.artist.playcount} scrobbles · ${detail.artist.listeners} listeners",
                    color = RecappedColors.Muted,
                    fontSize = 12.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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

            detail.topTracks.forEachIndexed { idx, track ->
                TrackRow(
                    rank = idx + 1,
                    track = track
                )

                if (idx < detail.topTracks.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
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
    track: Track
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RecappedColors.Surface)
            .border(
                width = 0.5.dp,
                color = RecappedColors.Border,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
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
            loading = placeholder(R.drawable.ic_splash_logo),
            failure = placeholder(R.drawable.ic_splash_logo),
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(RecappedColors.SurfaceBright)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        Text(
            text = track.playcount.toString(),
            color = RecappedColors.Dim,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
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