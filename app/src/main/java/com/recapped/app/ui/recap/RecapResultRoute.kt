package com.recapped.app.ui.recap

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
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recapped.app.R
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

private data class MockRecapArtist(
    val position: Int,
    val name: String,
    val scrobbles: Int,
    val initial: String,
    val accent: Color
)

private data class MockGenre(
    val name: String,
    val percentage: Int,
    val accent: Color
)

private data class MockRecommendation(
    val name: String,
    val genre: String,
    val initial: String,
    val accent: Color
)

private val mockArtists = listOf(
    MockRecapArtist(1, "Tame Impala", 847, "T", Color(0xFF5B20A5)),
    MockRecapArtist(2, "Khruangbin", 612, "K", Color(0xFF123A7A)),
    MockRecapArtist(3, "Stereolab", 489, "S", Color(0xFF0A5E47)),
    MockRecapArtist(4, "Floating Points", 376, "F", Color(0xFF8A4E04))
)

private val mockGenres = listOf(
    MockGenre("Psych Rock", 31, Color(0xFFFF2D00)),
    MockGenre("Krautrock", 22, Color(0xFFFF7A00)),
    MockGenre("Electronic", 19, Color(0xFFFFD000)),
    MockGenre("Trip Hop", 14, Color(0xFF7A3DFF)),
    MockGenre("Art Pop", 9, Color(0xFF2F7DFF))
)

private val mockRecommendations = listOf(
    MockRecommendation("King Gizzard & the Lizard Wizard", "Psych Rock", "K", Color(0xFF5B20A5)),
    MockRecommendation("Neu!", "Krautrock", "N", Color(0xFF0A5E47)),
    MockRecommendation("Goat", "Psych Folk", "G", Color(0xFF8A4E04))
)

@Composable
fun RecapResultRoute(
    onBack: () -> Unit = {},
    onShare: () -> Unit = {}
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
                month = "MARZO 2026",
                onBack = onBack,
                onShare = onShare
            )

            Spacer(modifier = Modifier.height(30.dp))

            HeroStats()

            Spacer(modifier = Modifier.height(30.dp))

            SectionLabel("Top artistas")

            Spacer(modifier = Modifier.height(14.dp))

            TopArtistsCarousel()

            Spacer(modifier = Modifier.height(34.dp))

            SectionLabel("Tu sonido")

            Spacer(modifier = Modifier.height(14.dp))

            GenreBreakdown()

            Spacer(modifier = Modifier.height(26.dp))

            AiAnalysisCard()

            Spacer(modifier = Modifier.height(26.dp))

            SectionLabel("Para vos")

            Spacer(modifier = Modifier.height(12.dp))

            RecommendationList()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RecapResultHeader(
    month: String,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Volver",
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = month,
            color = Color.White.copy(alpha = 0.23f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 4.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        HeaderIconButton(
            onClick = onShare
        ) {
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
private fun HeroStats() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "346",
            style = TextStyle(
                brush = BrandGradient,
                fontFamily = FontFamily(Font(R.font.unbounded_extrabold)),
                fontSize = 92.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-6).sp
            ),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "CANCIONES ESCUCHADAS",
            color = Color.White.copy(alpha = 0.20f),
            fontFamily = FontFamily(Font(R.font.unbounded_extrabold)),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.4.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroMetric(
                value = "43",
                label = "ARTISTAS"
            )

            HeroMetric(
                value = "62h",
                label = "ESCUCHADAS"
            )

            HeroMetric(
                value = "9",
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
            fontFamily = FontFamily(Font(R.font.unbounded_semibold)),
            fontSize = 27.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.20f),
            fontFamily = FontFamily(Font(R.font.unbounded_semibold)),
            fontSize = 8.sp,
            letterSpacing = 2.1.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.46f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.1.sp
    )
}

@Composable
private fun TopArtistsCarousel() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        mockArtists.forEach { artist ->
            TopArtistCard(artist = artist)
        }
    }
}

@Composable
private fun TopArtistCard(
    artist: MockRecapArtist
) {
    Column(
        modifier = Modifier.width(110.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 130.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFF111111))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            artist.accent.copy(alpha = 0.95f),
                            artist.accent.copy(alpha = 0.30f),
                            Color.Black.copy(alpha = 0.96f)
                        ),
                        center = Offset(
                            x = size.width * 0.36f,
                            y = size.height * 0.20f
                        ),
                        radius = size.width * 0.92f
                    ),
                    radius = size.width * 0.92f,
                    center = Offset(
                        x = size.width * 0.36f,
                        y = size.height * 0.20f
                    )
                )
            }

            Box(
                modifier = Modifier
                    .padding(9.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (artist.position == 1) {
                            BrandGradient
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF071428),
                                    Color(0xFF071428)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = artist.position.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = artist.initial,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.syne_semibold)),
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.Black.copy(alpha = 0.56f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = artist.scrobbles.toString(),
                    color = Color.White.copy(alpha = 0.82f),
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
private fun GenreBreakdown() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        mockGenres.forEachIndexed { index, genre ->
            GenreRow(
                genre = genre,
                isMain = index == 0
            )
        }
    }
}

@Composable
private fun GenreRow(
    genre: MockGenre,
    isMain: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "·",
            color = genre.accent,
            fontSize = if (isMain) 26.sp else 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(14.dp)
        )

        Text(
            text = genre.name,
            color = if (isMain) Color.White else Color.White.copy(alpha = 0.42f),
            fontFamily = FontFamily(Font(R.font.unbounded_semibold)),
            fontSize = if (isMain) 31.sp else 21.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = if (isMain) (-1.2).sp else (-0.8).sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${genre.percentage}%",
            color = Color.White.copy(alpha = if (isMain) 0.30f else 0.20f),
            fontSize = if (isMain) 12.sp else 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AiAnalysisCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF100D06).copy(alpha = 0.78f))
            .border(
                width = 0.7.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        RecappedColors.BrandOrange,
                        Color(0xFFE4B000)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            text = "Un explorador de texturas con raíz en el rock psicodélico de los 70.",
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.syne_semibold)),
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 29.sp,
            letterSpacing = (-0.5).sp
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
            text = "La dominancia de Tame Impala y Khruangbin revela una preferencia por texturas hipnóticas. Tu 22% de Krautrock confirma una fascinación por el ritmo motorik y la repetición como viaje.",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 23.sp
        )
    }
}

@Composable
private fun RecommendationList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        mockRecommendations.forEach { recommendation ->
            RecommendationRow(recommendation = recommendation)
        }
    }
}

@Composable
private fun RecommendationRow(
    recommendation: MockRecommendation
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFF141414).copy(alpha = 0.96f))
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(15.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            recommendation.accent.copy(alpha = 0.90f),
                            Color.Black.copy(alpha = 0.96f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = recommendation.initial,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.syne_semibold)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = recommendation.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

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
                .background(Color(0xFF1ED760))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Spotify",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
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