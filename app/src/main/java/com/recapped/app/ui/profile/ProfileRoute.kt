package com.recapped.app.ui.profile

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.Unbounded

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onRecapHistory: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreen(
        state = state,
        onBack = onBack,
        onEditProfile = onEditProfile,
        onRecapHistory = onRecapHistory,
        onSignOut = viewModel::signOut
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onRecapHistory: () -> Unit,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF431500).copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    center = Offset(250f, 80f),
                    radius = 520f
                )
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2A0E00).copy(alpha = 0.65f),
                        Color.Transparent
                    ),
                    center = Offset(780f, 920f),
                    radius = 620f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            ProfileHeader(onBack = onBack)

            Spacer(modifier = Modifier.height(28.dp))

            ProfileIdentity(
                displayName = state.displayName,
                onEditProfile = onEditProfile
            )

            Spacer(modifier = Modifier.height(22.dp))

            ProfileStatsRow(
                streak = "23",
                recaps = "7"
            )

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                ProfileOptionRow(
                    title = "Cuenta",
                    subtitle = "Editar perfil",
                    onClick = onEditProfile
                )

                ProfileOptionRow(
                    title = "Periodo por defecto",
                    subtitle = "Mensual"
                )

                ProfileOptionRow(
                    title = "Notificaciones",
                    subtitle = "Activadas"
                )

                ProfileOptionRow(
                    title = "Historial",
                    subtitle = "Todos tus recaps",
                    onClick = onRecapHistory
                )

                ProfileOptionRow(
                    title = "Acerca de",
                    subtitle = "Versión · Política de privacidad"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LogoutButton(
                onClick = onSignOut
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProfileHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF070707))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.10f),
                    shape = CircleShape
                )
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Perfil",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
private fun ProfileIdentity(
    displayName: String,
    onEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(92.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontFamily = Unbounded,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF151515))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.14f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onEditProfile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar perfil",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = displayName,
            style = TextStyle(
                brush = BrandGradient,
                fontSize = 24.sp,
                fontFamily = Unbounded,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.2).sp
            )
        )
    }
}

@Composable
private fun ProfileStatsRow(
    streak: String,
    recaps: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        ProfileStatCard(
            value = streak,
            label = "RACHA",
            highlighted = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        ProfileStatCard(
            value = recaps,
            label = "RECAPS",
            highlighted = false
        )
    }
}

@Composable
private fun ProfileStatCard(
    value: String,
    label: String,
    highlighted: Boolean
) {
    Column(
        modifier = Modifier
            .width(66.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111111).copy(alpha = 0.94f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(24.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 19.sp,
            fontFamily = Unbounded,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 18.sp
        )

        Text(
            text = label,
            color = if (highlighted) Color(0xFFFF3B16) else Color.White.copy(alpha = 0.30f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            lineHeight = 9.sp
        )
    }
}

@Composable
private fun ProfileOptionRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(59.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF101010).copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.36f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 12.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.20f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LogoutButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF230300).copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = Color(0xFFFF2A00).copy(alpha = 0.75f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cerrar sesión",
            color = Color(0xFFFF2A00),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}