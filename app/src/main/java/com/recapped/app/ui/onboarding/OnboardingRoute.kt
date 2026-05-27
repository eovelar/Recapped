package com.recapped.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.ui.components.GlowSpot
import com.recapped.app.ui.components.Glows
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

private data class OnboardingStep(
    val title: String,
    val description: String
)

private val onboardingSteps = listOf(
    OnboardingStep(
        title = "Conectá tu Last.fm",
        description = "Ingresá tu nombre de usuario para que podamos leer tu historial."
    ),
    OnboardingStep(
        title = "Generá tus Recaps",
        description = "Elegí el período — semanal, mensual o anual — y armamos un informe con gráficos y análisis."
    ),
    OnboardingStep(
        title = "IA que te entiende",
        description = "Análisis generado por IA que describe tu perfil musical y recomienda nuevos artistas afines."
    )
)

@Composable
fun OnboardingRoute(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScreen(
        state = state,
        onLastFmUsernameChange = viewModel::onLastFmUsernameChange,
        onValidateLastFmUsername = { onValid ->
            viewModel.validateLastFmUsername(onValid)
        },
        onFinish = { viewModel.finish(onCompleted) },
        onSkip = { viewModel.skip(onCompleted) }
    )
}

@Composable
private fun OnboardingScreen(
    state: OnboardingUiState,
    onLastFmUsernameChange: (String) -> Unit,
    onValidateLastFmUsername: (() -> Unit) -> Unit,
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val currentStep = onboardingSteps[step]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
    ) {
        Glows(
            spots = listOf(
                GlowSpot(
                    xFraction = 0.40f,
                    yFraction = 0.20f,
                    radiusFraction = 0.46f,
                    color = RecappedColors.BrandOrange.copy(alpha = 0.30f),
                    drift = true
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onSkip,
                    enabled = !state.isSaving
                ) {
                    Text(
                        text = "Skip",
                        color = RecappedColors.Dim,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepNumberCard(step = step)

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = currentStep.title,
                    color = RecappedColors.OnSurface,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentStep.description,
                    color = RecappedColors.Muted,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (step == 0) {
                    Spacer(modifier = Modifier.height(28.dp))

                    LastFmInput(
                        value = state.lastFmUsername,
                        isError = state.error != null,
                        enabled = !state.isSaving,
                        onValueChange = onLastFmUsernameChange
                    )

                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.error != null) {
                            Text(
                                text = state.error,
                                color = RecappedColors.Error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                StepIndicators(currentStep = step)
            }

            OnboardingActions(
                step = step,
                username = state.lastFmUsername,
                isSaving = state.isSaving,
                onConnect = {
                    onValidateLastFmUsername {
                        step = 1
                    }
                },
                onNext = {
                    if (step < onboardingSteps.lastIndex) {
                        step += 1
                    }
                },
                onBack = {
                    if (step > 0) {
                        step -= 1
                    }
                },
                onFinish = onFinish
            )
        }
    }
}

@Composable
private fun StepNumberCard(step: Int) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(RecappedColors.Surface)
            .border(
                width = 0.5.dp,
                color = RecappedColors.BorderBright,
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${step + 1}",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = RecappedColors.BrandOrange,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LastFmInput(
    value: String,
    isError: Boolean,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(RecappedColors.SurfaceBright)
            .border(
                width = 0.5.dp,
                color = if (isError) {
                    RecappedColors.Error.copy(alpha = 0.7f)
                } else {
                    RecappedColors.BorderBright
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                onValueChange(
                    raw
                        .replace(" ", "")
                        .take(40)
                )
            },
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            ),
            cursorBrush = SolidColor(RecappedColors.BrandOrange),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "nombre de usuario",
                        color = RecappedColors.Dim,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun StepIndicators(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        onboardingSteps.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .size(
                        width = if (index == currentStep) 28.dp else 6.dp,
                        height = 3.dp
                    )
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        brush = if (index == currentStep) {
                            BrandGradient
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.14f),
                                    Color.White.copy(alpha = 0.14f)
                                )
                            )
                        }
                    )
            )
        }
    }
}

@Composable
private fun OnboardingActions(
    step: Int,
    username: String,
    isSaving: Boolean,
    onConnect: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            0 -> {
                GradientActionButton(
                    text = if (isSaving) "Verificando" else "Conectar",
                    enabled = username.isNotBlank() && !isSaving,
                    loading = isSaving,
                    onClick = onConnect
                )
            }

            1 -> {
                GradientActionButton(
                    text = "Siguiente",
                    enabled = !isSaving,
                    loading = false,
                    onClick = onNext
                )

                BackButton(
                    text = "Atrás",
                    enabled = !isSaving,
                    onClick = onBack
                )
            }

            2 -> {
                GradientActionButton(
                    text = if (isSaving) "Guardando" else "Comenzar",
                    enabled = !isSaving,
                    loading = isSaving,
                    onClick = onFinish
                )

                BackButton(
                    text = "Atrás",
                    enabled = !isSaving,
                    onClick = onBack
                )
            }
        }
    }
}

@Composable
private fun GradientActionButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(
                brush = if (enabled) {
                    BrandGradient
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    )
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }

            Text(
                text = text,
                color = if (enabled) Color.White else RecappedColors.Dim,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BackButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = RecappedColors.Dim,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 8.dp)
    )
}