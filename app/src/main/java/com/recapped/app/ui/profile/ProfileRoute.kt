package com.recapped.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

/**
 * STUB de Perfil — placeholder mientras no exista la pantalla completa.
 * Muestra avatar + displayName + email, y los CTAs principales que ya
 * conocemos:
 *  - Editar perfil → todavía no implementado, queda inerte
 *  - Cuenta de Last.fm → idem
 *  - Cerrar sesión → conectado al AuthRepository real (sí funciona)
 *
 * Cuando hagamos la Profile real, este file se reemplaza completo.
 */
@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(state = state, onSignOut = viewModel::signOut)
}

@Composable
fun ProfileScreen(state: ProfileUiState, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Perfil",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp
        )

        Spacer(Modifier.height(28.dp))

        // Avatar + nombre + mail
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (state.displayName.firstOrNull()?.uppercase() ?: "U"),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = state.displayName,
                style = TextStyle(
                    brush = BrandGradient,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.6).sp
                )
            )
            state.email?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = RecappedColors.Muted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(28.dp))

        // Sección de acciones — los placeholders quedan inertes hasta tener Profile real
        SectionLabel("Cuenta")
        Spacer(Modifier.height(10.dp))
        OptionRow(label = "Editar perfil", placeholder = true)
        Spacer(Modifier.height(8.dp))
        OptionRow(label = "Cuenta de Last.fm", placeholder = true)

        Spacer(Modifier.height(24.dp))
        SectionLabel("Sesión")
        Spacer(Modifier.height(10.dp))
        OptionRow(
            label = "Cerrar sesión",
            destructive = true,
            onClick = onSignOut
        )

        Spacer(Modifier.weight(1f))

        // Nota honesta — esta sección se completa en los próximos pasos
        Text(
            text = "Próximamente · edición de perfil, vinculación de Last.fm e historial de recaps.",
            color = RecappedColors.Dim,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = RecappedColors.Muted,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun OptionRow(
    label: String,
    destructive: Boolean = false,
    placeholder: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RecappedColors.Surface)
            .border(0.5.dp, RecappedColors.Border, RoundedCornerShape(14.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (destructive) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = null,
                tint = RecappedColors.BrandRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = label,
            color = when {
                destructive -> RecappedColors.BrandRed
                placeholder -> RecappedColors.Muted
                else -> Color.White
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (placeholder) {
            Text(
                text = "Próx.",
                color = RecappedColors.Dim,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
        } else if (!destructive) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = RecappedColors.Dim,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
