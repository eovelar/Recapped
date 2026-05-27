package com.recapped.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.Unbounded

@Composable
fun EditProfileRoute(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EditProfileScreen(
        state = state,
        onBack = onBack,
        onLastFmUsernameChange = viewModel::onLastFmUsernameChange,
        onSave = {
            viewModel.saveChanges(onSaved = onBack)
        }
    )
}

@Composable
private fun EditProfileScreen(
    state: EditProfileUiState,
    onBack: () -> Unit,
    onLastFmUsernameChange: (String) -> Unit,
    onSave: () -> Unit
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
        if (state.isLoading) {
            CircularProgressIndicator(
                color = Color(0xFFFF6A00),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                EditProfileHeader(onBack = onBack)

                Spacer(modifier = Modifier.height(28.dp))

                EditProfileIdentity(
                    initial = state.initial,
                    displayName = state.displayName
                )

                Spacer(modifier = Modifier.height(26.dp))

                SectionTitle("TU CUENTA")

                Spacer(modifier = Modifier.height(12.dp))

                ProfileInputCard(
                    label = "NOMBRE",
                    value = state.displayName,
                    enabled = false,
                    onValueChange = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileInputCard(
                    label = "EMAIL",
                    value = state.email,
                    enabled = false,
                    onValueChange = {}
                )

                Spacer(modifier = Modifier.height(26.dp))

                SectionTitle("LAST.FM")

                Spacer(modifier = Modifier.height(12.dp))

                ProfileInputCard(
                    label = "USUARIO",
                    value = state.lastFmUsername,
                    enabled = true,
                    onValueChange = onLastFmUsernameChange
                )

                Spacer(modifier = Modifier.height(38.dp))

                WarningCard()

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = error,
                        color = Color(0xFFFF3B16),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                SaveButton(
                    isSaving = state.isSaving,
                    onClick = onSave
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EditProfileHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            text = "Editar perfil",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
private fun EditProfileIdentity(
    initial: String,
    displayName: String
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
                    text = initial,
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
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar avatar",
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
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.58f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}

@Composable
private fun ProfileInputCard(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF101010).copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(11.dp)
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.28f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.7.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            if (enabled) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = value.ifBlank { "-" },
                    color = if (label == "EMAIL") {
                        Color.White.copy(alpha = 0.48f)
                    } else {
                        Color.White
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        if (enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF151515))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.14f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun WarningCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF230300).copy(alpha = 0.46f))
            .border(
                width = 1.dp,
                color = Color(0xFFFF2A00).copy(alpha = 0.70f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = Color(0xFFFF2A00),
            modifier = Modifier.size(19.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Si cambiás el usuario se van a reemplazar todos los datos sincronizados en la app.",
            color = Color.White.copy(alpha = 0.68f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(BrandGradient)
            .clickable(enabled = !isSaving, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = "Guardar cambios",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}