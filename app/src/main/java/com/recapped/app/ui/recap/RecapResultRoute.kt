package com.recapped.app.ui.recap

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.recapped.app.R
import com.recapped.app.domain.model.RecapArtist
import com.recapped.app.domain.model.RecapGenre
import com.recapped.app.domain.model.RecapRecommendation
import com.recapped.app.domain.model.RecapResult
import com.recapped.app.domain.model.RecapTrack
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SpotifyGreen = Color(0xFF1ED760)

private val RecapAccents = listOf(
    Color(0xFFFF2D00),
    Color(0xFFFF7A00),
    Color(0xFFFFD000),
    Color(0xFF7A3DFF),
    Color(0xFF2F7DFF)
)

@Composable
fun RecapResultRoute(
    spotifyCallbackUrl: String?,
    onSpotifyCallbackConsumed: () -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onArtistClick: (String) -> Unit,
    onSongClick: (artistName: String, trackName: String) -> Unit,
    viewModel: RecapViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(spotifyCallbackUrl) {
        if (!spotifyCallbackUrl.isNullOrBlank()) {
            viewModel.onSpotifyAuthorizationCallback(spotifyCallbackUrl)
            onSpotifyCallbackConsumed()
        }
    }

    LaunchedEffect(state.spotifyAction) {
        when (val action = state.spotifyAction) {
            is RecapSpotifyAction.Authorize -> {
                openUrl(context, action.url)
                viewModel.consumeSpotifyAction()
            }

            is RecapSpotifyAction.OpenArtist -> {
                openUrl(context, action.url)
                viewModel.consumeSpotifyAction()
            }

            is RecapSpotifyAction.Error -> {
                Toast.makeText(
                    context,
                    action.message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.consumeSpotifyAction()
            }

            RecapSpotifyAction.Idle,
            RecapSpotifyAction.Loading -> Unit
        }
    }

    val recap = state.result

    if (recap == null) {
        EmptyRecapResult(onBack = onBack)
        return
    }

    RecapResultScreen(
        recap = recap,
        loadingSpotifyArtist = state.loadingSpotifyArtist,
        onBack = onBack,
        onShare = onShare,
        onArtistClick = onArtistClick,
        onSongClick = onSongClick,
        onOpenSpotify = viewModel::openArtistInSpotify
    )
}

@Composable
private fun RecapResultScreen(
    recap: RecapResult,
    loadingSpotifyArtist: String?,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onArtistClick: (String) -> Unit,
    onSongClick: (artistName: String, trackName: String) -> Unit,
    onOpenSpotify: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        RecapResultBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 18.dp, bottom = 28.dp)
        ) {
            RecapResultHeader(
                label = formatPeriodLabel(recap),
                onBack = onBack,
                onShare = onShare
            )

            Spacer(modifier = Modifier.height(30.dp))

            HeroStats(recap)

            Spacer(modifier = Modifier.height(30.dp))

            SectionLabel("Top artistas")
            Spacer(modifier = Modifier.height(14.dp))

            TopArtistsCarousel(
                artists = recap.topArtists,
                onArtistClick = onArtistClick
            )

            if (recap.topTracks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(34.dp))
                SectionLabel("Top canciones")
                Spacer(modifier = Modifier.height(12.dp))

                TopTracksList(
                    tracks = recap.topTracks,
                    onSongClick = onSongClick
                )
            }

            Spacer(modifier = Modifier.height(34.dp))
            SectionLabel("Tu sonido")
            Spacer(modifier = Modifier.height(14.dp))

            GenreBreakdown(recap.genres)

            Spacer(modifier = Modifier.height(26.dp))

            AiAnalysisCard(
                headline = recap.aiHeadline,
                summary = recap.aiSummary
            )

            Spacer(modifier = Modifier.height(26.dp))
            SectionLabel("Para vos")
            Spacer(modifier = Modifier.height(12.dp))

            RecommendationList(
                recommendations = recap.recommendations,
                loadingSpotifyArtist = loadingSpotifyArtist,
                onArtistClick = onArtistClick,
                onOpenSpotify = onOpenSpotify
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmptyRecapResult(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No hay un recap generado",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Volvé a la pantalla anterior y generá uno.",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BrandGradient)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Volver",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RecapResultHeader(
    label: String,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Volver",
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.30f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.4.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        HeaderIconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Rounded.IosShare,
                contentDescription = "Compartir",
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun HeroStats(
    recap: RecapResult
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = recap.totalScrobbles.toString(),
            style = TextStyle(
                brush = BrandGradient,
                fontFamily = FontFamily(
                    Font(R.font.unbounded_extrabold)
                ),
                fontSize = 82.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.sp
            ),
            maxLines = 1
        )

        Text(
            text = "REPRODUCCIONES",
            color = Color.White.copy(alpha = 0.24f),
            fontFamily = FontFamily(
                Font(R.font.unbounded_extrabold)
            ),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HeroMetric(
                value = recap.uniqueArtists.toString(),
                label = "ARTISTAS"
            )

            HeroMetric(
                value = recap.uniqueTracks.toString(),
                label = "CANCIONES"
            )

            HeroMetric(
                value = recap.genres.size.toString(),
                label = "GÉNEROS"
            )
        }
    }
}

@Composable
private fun HeroMetric(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontFamily = FontFamily(
                Font(R.font.unbounded_semibold)
            ),
            fontSize = 27.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.20f),
            fontSize = 8.sp,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionLabel(
    text: String
) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.46f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.1.sp
    )
}

@Composable
private fun TopArtistsCarousel(
    artists: List<RecapArtist>,
    onArtistClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        artists.forEachIndexed { index, artist ->
            TopArtistCard(
                artist = artist,
                accent = RecapAccents[index % RecapAccents.size],
                onClick = {
                    onArtistClick(artist.name)
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun TopArtistCard(
    artist: RecapArtist,
    accent: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 130.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFF111111))
        ) {
            GlideImage(
                model = artist.imageUrl,
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                loading = placeholder(R.drawable.ic_splash_logo),
                failure = placeholder(R.drawable.ic_splash_logo),
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .padding(9.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (artist.rank == 1) {
                            BrandGradient
                        } else {
                            Brush.linearGradient(
                                listOf(accent, accent)
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = artist.rank.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = artist.playcount.toString(),
                    color = Color.White.copy(alpha = 0.84f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = artist.name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TopTracksList(
    tracks: List<RecapTrack>,
    onSongClick: (artistName: String, trackName: String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tracks.forEach { track ->
            TopTrackRow(
                track = track,
                onClick = {
                    onSongClick(track.artistName, track.name)
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun TopTrackRow(
    track: RecapTrack,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141414))
            .border(
                width = 0.6.dp,
                color = Color.White.copy(alpha = 0.09f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = track.rank.toString(),
            color = RecappedColors.Dim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(22.dp)
        )

        GlideImage(
            model = track.imageUrl,
            contentDescription = track.name,
            contentScale = ContentScale.Crop,
            loading = placeholder(R.drawable.ic_splash_logo),
            failure = placeholder(R.drawable.ic_splash_logo),
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(9.dp))
        )

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.artistName,
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = track.playcount.toString(),
            color = Color.White.copy(alpha = 0.24f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GenreBreakdown(
    genres: List<RecapGenre>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        genres.forEachIndexed { index, genre ->
            GenreRow(
                genre = genre,
                accent = RecapAccents[index % RecapAccents.size],
                isMain = index == 0
            )
        }
    }
}

@Composable
private fun GenreRow(
    genre: RecapGenre,
    accent: Color,
    isMain: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "·",
            color = accent,
            fontSize = if (isMain) 26.sp else 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(14.dp)
        )

        Text(
            text = genre.name,
            color = if (isMain) {
                Color.White
            } else {
                Color.White.copy(alpha = 0.42f)
            },
            fontFamily = FontFamily(
                Font(R.font.unbounded_semibold)
            ),
            fontSize = if (isMain) 28.sp else 19.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${genre.percentage}%",
            color = Color.White.copy(
                alpha = if (isMain) 0.30f else 0.20f
            ),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AiAnalysisCard(
    headline: String,
    summary: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF100D06).copy(alpha = 0.78f))
            .border(
                width = 0.7.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        RecappedColors.BrandOrange,
                        Color(0xFFE4B000)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = "ANÁLISIS DE IA",
                color = Color(0xFFE4B000),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = headline,
            color = Color.White,
            fontFamily = FontFamily(
                Font(R.font.syne_semibold)
            ),
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 29.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.10f))
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = summary,
            color = Color.White.copy(alpha = 0.50f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 23.sp
        )
    }
}

@Composable
private fun RecommendationList(
    recommendations: List<RecapRecommendation>,
    loadingSpotifyArtist: String?,
    onArtistClick: (String) -> Unit,
    onOpenSpotify: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        recommendations.forEach { recommendation ->
            RecommendationRow(
                recommendation = recommendation,
                loading = loadingSpotifyArtist.equals(
                    recommendation.name,
                    ignoreCase = true
                ),
                onArtistClick = {
                    onArtistClick(recommendation.name)
                },
                onOpenSpotify = {
                    onOpenSpotify(recommendation.name)
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun RecommendationRow(
    recommendation: RecapRecommendation,
    loading: Boolean,
    onArtistClick: () -> Unit,
    onOpenSpotify: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFF141414))
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(15.dp)
            )
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlideImage(
                model = recommendation.imageUrl,
                contentDescription = recommendation.name,
                contentScale = ContentScale.Crop,
                loading = placeholder(R.drawable.ic_splash_logo),
                failure = placeholder(R.drawable.ic_splash_logo),
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onArtistClick)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onArtistClick)
            ) {
                Text(
                    text = recommendation.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = recommendation.genre.uppercase(),
                    color = Color.White.copy(alpha = 0.32f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SpotifyGreen)
                    .clickable(
                        enabled = !loading,
                        onClick = onOpenSpotify
                    )
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(3.dp))

                        Text(
                            text = "Spotify",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (recommendation.reason.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recommendation.reason,
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun RecapResultBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF2D00).copy(alpha = 0.16f),
                    Color.Transparent
                ),
                center = Offset(
                    x = size.width * 0.18f,
                    y = size.height * 0.10f
                ),
                radius = size.width * 0.55f
            ),
            radius = size.width * 0.55f,
            center = Offset(
                x = size.width * 0.18f,
                y = size.height * 0.10f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF8A00).copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(
                    x = size.width * 0.86f,
                    y = size.height * 0.28f
                ),
                radius = size.width * 0.42f
            ),
            radius = size.width * 0.42f,
            center = Offset(
                x = size.width * 0.86f,
                y = size.height * 0.28f
            )
        )
    }
}

private fun formatPeriodLabel(
    recap: RecapResult
): String {
    val date = SimpleDateFormat(
        "MMM yyyy",
        Locale("es", "AR")
    ).format(Date(recap.generatedAt))

    return "${recap.period.title} · $date"
        .replaceFirstChar { it.uppercase() }
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
