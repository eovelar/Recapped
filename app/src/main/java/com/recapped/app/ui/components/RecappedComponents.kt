package com.recapped.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

/** Tarjeta "glass": fondo translúcido + borde fino. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(RecappedColors.Surface)
            .border(0.5.dp, RecappedColors.Border, RoundedCornerShape(16.dp))
    ) { content() }
}

/** Chip pequeño con estilo Recapped. */
@Composable
fun RecappedChip(
    text: String,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgBrush: Brush =
        if (selected) BrandGradient
        else Brush.linearGradient(listOf(RecappedColors.Surface, RecappedColors.Surface))
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgBrush)
            .border(0.5.dp, if (selected) Color.Transparent else RecappedColors.Border, CircleShape)
            .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else RecappedColors.Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Texto con gradiente de marca aplicado. */
@Composable
fun GradientText(
    text: String,
    fontSize: Int = 16,
    fontWeight: FontWeight = FontWeight.Bold,
    modifier: Modifier = Modifier
) {
    // Compose no soporta brush en texto vía param simple sin TextStyle.
    // Usamos copy de style con brush.
    Text(
        text = text,
        modifier = modifier,
        style = androidx.compose.ui.text.TextStyle(
            brush = BrandGradient,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Start,
            letterSpacing = (-0.5).sp
        )
    )
}
