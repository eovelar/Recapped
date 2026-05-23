package com.recapped.app.ui.charts

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.recapped.app.R
import com.recapped.app.domain.model.Artist
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

@Composable
fun ChartsRoute(
    onArtistClick: (String) -> Unit,
    onBack: () -> Unit = {},
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChartsScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::load,
        onArtistClick = onArtistClick,
        onBack = onBack,
        onContentTypeChange = viewModel::onContentTypeChange,
        onViewModeChange = viewModel::onViewModeChange
    )
}

@Composable
fun ChartsScreen(
    state: ChartsUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onArtistClick: (String) -> Unit,
    onBack: () -> Unit,
    onContentTypeChange: (ChartsContentType) -> Unit,
    onViewModeChange: (ChartsViewMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        ChartsHeader(
            viewMode = state.viewMode,
            onBack = onBack,
            onViewModeChange = onViewModeChange
        )

        SearchField(
            query = state.query,
            onQueryChange = onQueryChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        ContentChips(
            selected = state.contentType,
            onSelected = onContentTypeChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        when (state.phase) {
            ChartsPhase.Loading -> LoadingState()

            is ChartsPhase.Error -> ErrorState(
                message = state.phase.message,
                onRetry = onRetry
            )

            is ChartsPhase.Success -> {
                when (state.contentType) {
                    ChartsContentType.ARTISTS -> {
                        val artists = state.filteredArtists

                        if (artists.isEmpty()) {
                            EmptyState()
                        } else if (state.viewMode == ChartsViewMode.GRID) {
                            ArtistsGrid(
                                artists = artists,
                                onArtistClick = onArtistClick
                            )
                        } else {
                            ArtistsList(
                                artists = artists,
                                onArtistClick = onArtistClick
                            )
                        }
                    }

                    ChartsContentType.SONGS -> {
                        val songs = state.filteredSongs

                        if (songs.isEmpty()) {
                            EmptySongsState()
                        } else if (state.viewMode == ChartsViewMode.GRID) {
                            SongsGrid(
                                songs = songs,
                                onSongClick = { song ->
                                    onArtistClick(song.artistName)
                                }
                            )
                        } else {
                            SongsList(
                                songs = songs,
                                onSongClick = { song ->
                                    onArtistClick(song.artistName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun ChartsHeader(
    viewMode: ChartsViewMode,
    onBack: () -> Unit,
    onViewModeChange: (ChartsViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .border(
                    width = 0.7.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Top charts",
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.syne_semibold)),
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.weight(1f)
        )

        GridModeButton(
            selected = viewMode == ChartsViewMode.GRID,
            onClick = { onViewModeChange(ChartsViewMode.GRID) }
        )

        Spacer(modifier = Modifier.width(10.dp))

        ListModeButton(
            selected = viewMode == ChartsViewMode.LIST,
            onClick = { onViewModeChange(ChartsViewMode.LIST) }
        )
    }
}

@Composable
private fun GridModeButton(
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) {
        RecappedColors.BrandOrange
    } else {
        Color.White.copy(alpha = 0.22f)
    }

    Column(
        modifier = Modifier
            .size(20.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            GridDot(color)
            GridDot(color)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            GridDot(color)
            GridDot(color)
        }
    }
}

@Composable
private fun GridDot(color: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
private fun ListModeButton(
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) {
        RecappedColors.BrandOrange
    } else {
        Color.White.copy(alpha = 0.22f)
    }

    Column(
        modifier = Modifier
            .size(width = 24.dp, height = 20.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(color)
            )

            if (index < 2) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ── Search + filtros ────────────────────────────────────────────────────────

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF111111).copy(alpha = 0.94f))
            .border(
                width = 0.6.dp,
                color = Color.White.copy(alpha = 0.13f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.30f),
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "Buscar...",
                    color = Color.White.copy(alpha = 0.34f),
                    fontSize = 11.sp
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 11.sp
                ),
                cursorBrush = SolidColor(RecappedColors.BrandOrange),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    capitalization = KeyboardCapitalization.None
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ContentChips(
    selected: ChartsContentType,
    onSelected: (ChartsContentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipButton(
            text = "Artistas",
            selected = selected == ChartsContentType.ARTISTS,
            onClick = { onSelected(ChartsContentType.ARTISTS) }
        )

        FilterChipButton(
            text = "Canciones",
            selected = selected == ChartsContentType.SONGS,
            onClick = { onSelected(ChartsContentType.SONGS) }
        )
    }
}

@Composable
private fun FilterChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(
                brush = if (selected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF2D00),
                            Color(0xFFFF7A00)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF111111),
                            Color(0xFF111111)
                        )
                    )
                }
            )
            .border(
                width = 0.7.dp,
                color = if (selected) {
                    Color.Transparent
                } else {
                    Color.White.copy(alpha = 0.13f)
                },
                shape = RoundedCornerShape(99.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.barlow_semibold)),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Grid artistas ───────────────────────────────────────────────────────────

@Composable
private fun ArtistsGrid(
    artists: List<Artist>,
    onArtistClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = 96.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        gridItemsIndexed(
            items = artists,
            key = { _, artist -> artist.mbid.ifBlank { artist.name } }
        ) { index, artist ->
            ArtistGridCard(
                artist = artist,
                position = index + 1,
                onClick = { onArtistClick(artist.name) }
            )
        }
    }
}

@Composable
private fun ArtistGridCard(
    artist: Artist,
    position: Int,
    onClick: () -> Unit
) {
    ChartGridCard(
        position = position,
        title = artist.name,
        subtitle = "ARTISTA",
        imageUrl = artist.imageUrl,
        centerLabel = artist.name.firstOrNull()?.uppercase() ?: "?",
        accent = gridAccent(position),
        onClick = onClick
    )
}

// ── Grid canciones ──────────────────────────────────────────────────────────

@Composable
private fun SongsGrid(
    songs: List<ChartSong>,
    onSongClick: (ChartSong) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = 96.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        gridItemsIndexed(
            items = songs,
            key = { _, song -> "${song.artistName}-${song.name}" }
        ) { index, song ->
            SongGridCard(
                song = song,
                position = index + 1,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
private fun SongGridCard(
    song: ChartSong,
    position: Int,
    onClick: () -> Unit
) {
    ChartGridCard(
        position = position,
        title = song.name,
        subtitle = song.artistName.uppercase(),
        imageUrl = song.imageUrl,
        centerLabel = song.name.firstOrNull()?.uppercase() ?: "?",
        accent = gridAccent(position),
        onClick = onClick
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ChartGridCard(
    position: Int,
    title: String,
    subtitle: String,
    imageUrl: String?,
    centerLabel: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111111))
            .clickable(onClick = onClick)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            GlideImage(
                model = imageUrl,
                contentDescription = title,
                loading = placeholder(R.drawable.ic_splash_logo),
                failure = placeholder(R.drawable.ic_splash_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.10f),
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
            )
        } else {
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.60f),
                            accent.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = size.width * 0.46f,
                            y = size.height * 0.40f
                        ),
                        radius = size.width * 0.70f
                    ),
                    radius = size.width * 0.70f,
                    center = Offset(
                        x = size.width * 0.46f,
                        y = size.height * 0.40f
                    )
                )
            }

            Text(
                text = centerLabel,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .padding(9.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position.toString(),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 8.sp,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Lista artistas ──────────────────────────────────────────────────────────

@Composable
private fun ArtistsList(
    artists: List<Artist>,
    onArtistClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(
            items = artists,
            key = { _, artist -> artist.mbid.ifBlank { artist.name } }
        ) { index, artist ->
            ArtistListRow(
                artist = artist,
                position = index + 1,
                onClick = { onArtistClick(artist.name) }
            )
        }
    }
}

@Composable
private fun ArtistListRow(
    artist: Artist,
    position: Int,
    onClick: () -> Unit
) {
    ChartListRow(
        position = position,
        title = artist.name,
        subtitle = "${compact(artist.playcount)} scrobbles",
        imageUrl = artist.imageUrl,
        fallbackLabel = artist.name.firstOrNull()?.uppercase() ?: "?",
        onClick = onClick
    )
}

// ── Lista canciones ─────────────────────────────────────────────────────────

@Composable
private fun SongsList(
    songs: List<ChartSong>,
    onSongClick: (ChartSong) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(
            items = songs,
            key = { _, song -> "${song.artistName}-${song.name}" }
        ) { index, song ->
            SongListRow(
                song = song,
                position = index + 1,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
private fun SongListRow(
    song: ChartSong,
    position: Int,
    onClick: () -> Unit
) {
    ChartListRow(
        position = position,
        title = song.name,
        subtitle = song.artistName,
        imageUrl = song.imageUrl,
        fallbackLabel = song.name.firstOrNull()?.uppercase() ?: "?",
        onClick = onClick
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ChartListRow(
    position: Int,
    title: String,
    subtitle: String,
    imageUrl: String?,
    fallbackLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111).copy(alpha = 0.96f))
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = 0.11f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(BrandGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            GlideImage(
                model = imageUrl,
                contentDescription = title,
                loading = placeholder(R.drawable.ic_splash_logo),
                failure = placeholder(R.drawable.ic_splash_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            if (imageUrl.isNullOrBlank()) {
                Text(
                    text = fallbackLabel,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "›",
            color = Color.White.copy(alpha = 0.28f),
            fontSize = 28.sp
        )
    }
}

// ── Estados ─────────────────────────────────────────────────────────────────

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = RecappedColors.BrandOrange)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cargando charts...",
            color = Color.White.copy(alpha = 0.42f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRetry,
            shape = CircleShape
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 96.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No se encontraron resultados.",
            color = Color.White.copy(alpha = 0.42f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun EmptySongsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 96.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cargando canciones...",
            color = Color.White.copy(alpha = 0.42f),
            fontSize = 13.sp
        )
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

private fun gridAccent(position: Int): Color {
    return when ((position - 1) % 4) {
        0 -> Color(0xFF5B20A5)
        1 -> Color(0xFF123A7A)
        2 -> Color(0xFF0A5E47)
        else -> Color(0xFF8A4E04)
    }
}

private fun compact(n: Long): String = when {
    n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000.0)
    n >= 1_000 -> String.format("%.1fK", n / 1_000.0)
    else -> n.toString()
}