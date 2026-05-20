package com.recapped.app.ui.recap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recapped.app.ui.components.GlowSpot
import com.recapped.app.ui.components.Glows
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

/**
 * STUB de Recap Gen. La feature completa (selector de período + llamado a
 * `user.getTopArtists/Tracks` con `period` + persistencia en Firestore +
 * stub de AI) llega en el próximo paso.
 *
 * Por ahora muestra una pantalla "en construcción" estilizada para que
 * el tab inferior tenga un destino válido y la navegación se sienta completa.
 */
@Composable
fun RecapGenRoute() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
    ) {
        Glows(
            spots = listOf(
                GlowSpot(
                    xFraction = 0.5f, yFraction = 0.3f, radiusFraction = 0.55f,
                    color = RecappedColors.BrandOrange.copy(alpha = 0.22f), drift = true
                ),
                GlowSpot(
                    xFraction = 0.5f, yFraction = 0.8f, radiusFraction = 0.45f,
                    color = RecappedColors.BrandRed.copy(alpha = 0.18f), drift = false
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(RecappedColors.Surface)
                    .border(0.5.dp, RecappedColors.BorderBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Adjust,
                    contentDescription = null,
                    tint = RecappedColors.BrandOrange,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Tu recap",
                style = TextStyle(
                    brush = BrandGradient,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Generador de recaps mensuales y anuales con tu data de Last.fm.\nDisponible en la próxima iteración.",
                color = RecappedColors.Muted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(28.dp))

            // Chip "Próximamente"
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(RecappedColors.Surface)
                    .border(0.5.dp, RecappedColors.Border, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "PRÓXIMAMENTE",
                    color = RecappedColors.BrandOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Pie con lo que va a venir
        Text(
            text = "Próximo paso · selector de período · generación con Last.fm · historial.",
            color = RecappedColors.Dim,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}
