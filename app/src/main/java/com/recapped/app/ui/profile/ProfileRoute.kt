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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.recapped.app.BuildConfig
import com.recapped.app.domain.model.RecapPeriod
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
    var showPeriodDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    ProfileScreen(
        state = state,
        onBack = onBack,
        onEditProfile = onEditProfile,
        onRecapHistory = onRecapHistory,
        onDefaultPeriodClick = {
            showPeriodDialog = true
        },
        onNotificationsChange =
            viewModel::setNotificationsEnabled,
        onAboutClick = {
            showAboutDialog = true
        },
        onSignOut = viewModel::signOut
    )

    if (showPeriodDialog) {
        DefaultPeriodDialog(
            selectedPeriod = state.defaultPeriod,
            onPeriodSelected = { period ->
                viewModel.setDefaultPeriod(period)
                showPeriodDialog = false
            },
            onDismiss = {
                showPeriodDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = {
                showAboutDialog = false
            }
        )
    }
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onRecapHistory: () -> Unit,
    onDefaultPeriodClick: () -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onAboutClick: () -> Unit,
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
                displayName = state.displayName
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                    subtitle = periodLabel(state.defaultPeriod),
                    onClick = onDefaultPeriodClick
                )

                NotificationOptionRow(
                    enabled = state.notificationsEnabled,
                    onEnabledChange = onNotificationsChange
                )

                ProfileOptionRow(
                    title = "Historial",
                    subtitle = if (state.recapCount == 1) {
                        "1 recap guardado"
                    } else {
                        "${state.recapCount} recaps guardados"
                    },
                    onClick = onRecapHistory
                )

                ProfileOptionRow(
                    title = "Acerca de",
                    subtitle = "Version ${BuildConfig.VERSION_NAME}",
                    onClick = onAboutClick
                )
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = error,
                    color = Color(0xFFFF3B16),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
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
                imageVector =
                    Icons.AutoMirrored.Outlined.ArrowBack,
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
    displayName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(BrandGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName
                    .firstOrNull()
                    ?.uppercase()
                    ?: "U",
                color = Color.White,
                fontSize = 30.sp,
                fontFamily = Unbounded,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp
            )
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
private fun ProfileOptionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
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
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OptionText(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector =
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.20f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun NotificationOptionRow(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
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
            .clickable {
                onEnabledChange(!enabled)
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OptionText(
            title = "Notificaciones",
            subtitle = if (enabled) {
                "Activadas"
            } else {
                "Desactivadas"
            },
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF3B16),
                uncheckedThumbColor =
                    Color.White.copy(alpha = 0.72f),
                uncheckedTrackColor = Color(0xFF292929),
                uncheckedBorderColor =
                    Color.White.copy(alpha = 0.12f)
            )
        )
    }
}

@Composable
private fun OptionText(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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
            lineHeight = 12.sp
        )
    }
}

@Composable
private fun DefaultPeriodDialog(
    selectedPeriod: RecapPeriod,
    onPeriodSelected: (RecapPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF151515),
        title = {
            Text(
                text = "Periodo por defecto",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                RecapPeriod.entries.forEach { period ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPeriodSelected(period)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = period == selectedPeriod,
                            onClick = {
                                onPeriodSelected(period)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFFFF3B16),
                                unselectedColor =
                                    Color.White.copy(alpha = 0.45f)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = periodLabel(period),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = period.subtitle,
                                color = Color.White.copy(alpha = 0.42f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = Color(0xFFFF3B16)
                )
            }
        }
    )
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF151515),
        title = {
            Text(
                text = "Acerca de Recapped",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    color = Color(0xFFFF3B16),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Recapped analiza tu historial musical de Last.fm y crea resúmenes personalizados.",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Text(
                    text = "Privacidad",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "La app usa tu cuenta de Firebase y tu usuario de Last.fm. Los recaps se guardan localmente en el dispositivo mediante Room.",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cerrar",
                    color = Color(0xFFFF3B16)
                )
            }
        }
    )
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
            text = "Cerrar sesion",
            color = Color(0xFFFF2A00),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

private fun periodLabel(
    period: RecapPeriod
): String {
    return when (period) {
        RecapPeriod.Week -> "Semanal"
        RecapPeriod.Month -> "Mensual"
        RecapPeriod.Quarter -> "Trimestral"
        RecapPeriod.Year -> "Anual"
    }
}
