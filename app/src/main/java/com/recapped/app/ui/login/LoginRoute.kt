package com.recapped.app.ui.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.recapped.app.BuildConfig
import com.recapped.app.R
import com.recapped.app.ui.theme.RecappedColors
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LoginScreen(
        state = state,
        onContinueWithGoogle = {
            scope.launch {
                runCatching { requestGoogleIdToken(context) }
                    .onSuccess { token -> viewModel.onGoogleIdToken(token) }
                    .onFailure { e ->
                        Log.e("Login", "Sign-in error", e)
                        viewModel.onSignInError(e.message ?: "No se pudo continuar con Google")
                    }
            }
        },
        onDismissError = viewModel::clearError
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onContinueWithGoogle: () -> Unit,
    onDismissError: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecappedColors.Background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    RecappedColors.BrandOrange.copy(alpha = 0.52f),
                                    RecappedColors.BrandRed.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.recapped_logo_raw),
                    contentDescription = "Recapped",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(104.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Recapped",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.2).sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.login_subtitle),
                color = RecappedColors.Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinueWithGoogle,
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF111111)
                )
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF111111),
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Conectando…",
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.continue_with_google),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Al continuar aceptás los Términos de uso",
                color = RecappedColors.Dim,
                fontSize = 11.sp
            )
        }

        state.error?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onDismissError) {
                        Text("Ok")
                    }
                }
            ) {
                Text(msg)
            }
        }
    }
}

@Composable
private fun stringResource(@androidx.annotation.StringRes id: Int): String =
    androidx.compose.ui.res.stringResource(id)

private suspend fun requestGoogleIdToken(context: Context): String {
    val cm = CredentialManager.create(context)

    val googleOption = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleOption)
        .build()

    val response = try {
        cm.getCredential(context, request)
    } catch (e: GetCredentialException) {
        throw IllegalStateException(e.message ?: "Sign-in cancelado", e)
    }

    val credential = response.credential

    require(
        credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        "Credencial inesperada (${credential.type})"
    }

    val googleIdCred = GoogleIdTokenCredential.createFrom(credential.data)
    return googleIdCred.idToken
}