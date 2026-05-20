package com.recapped.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.recapped.app.ui.theme.RecappedColors

/**
 * Avatar/foto para un artista. Mientras Glide resuelve la imagen remota,
 * mostramos un placeholder bonito: gradiente lineal derivado del nombre
 * + la inicial centrada. Replica el "photo-like card" de la mock.
 *
 * Importante: `imageUrl` puede ser null o vacío — Last.fm a veces no
 * devuelve imagen para artistas obscuros. En ese caso el placeholder
 * queda visible.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArtistAvatar(
    name: String,
    imageUrl: String?,
    size: Dp,
    cornerRadius: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val seedColor = colorForName(name)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    0f to seedColor.copy(alpha = 0.55f),
                    0.6f to seedColor.copy(alpha = 0.18f),
                    1f to Color.Black.copy(alpha = 0.85f)
                )
            )
            .border(0.5.dp, RecappedColors.Border, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        // Inicial como fallback
        Text(
            text = (name.firstOrNull()?.uppercase() ?: "?"),
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.42f).sp
        )
        // Glide encima — si carga, tapa el placeholder
        if (!imageUrl.isNullOrBlank()) {
            GlideImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(cornerRadius))
            )
        }
    }
}

/**
 * Color "estable" a partir del nombre. Hash determinístico → paleta curada.
 * Imita los colores que la mock tenía hardcodeados por artista.
 */
private fun colorForName(name: String): Color {
    if (name.isBlank()) return RecappedColors.BrandOrange
    val palette = listOf(
        Color(0xFF7C3AED), // violeta
        Color(0xFF2563EB), // azul
        Color(0xFF059669), // verde
        Color(0xFFD97706), // ámbar
        Color(0xFFDB2777), // rosa
        Color(0xFF0891B2), // cyan
        Color(0xFFCC1500), // brand red
        Color(0xFF065F46), // verde oscuro
        Color(0xFF1D4ED8)  // azul medio
    )
    val idx = (name.fold(0) { acc, c -> acc + c.code } % palette.size + palette.size) % palette.size
    return palette[idx]
}
