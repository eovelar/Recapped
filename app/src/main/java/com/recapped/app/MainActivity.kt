package com.recapped.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recapped.app.ui.AuthUiState
import com.recapped.app.ui.RecappedNavGraph
import com.recapped.app.ui.RootViewModel
import com.recapped.app.ui.theme.RecappedTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var spotifyCallbackUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleSpotifyCallback(intent)

        var keepSplash = true
        splash.setKeepOnScreenCondition { keepSplash }

        setContent {
            RecappedTheme {
                val rootVm: RootViewModel = hiltViewModel()
                val authState by rootVm.authState.collectAsStateWithLifecycle()

                LaunchedEffect(authState) {
                    if (authState !is AuthUiState.Checking) {
                        keepSplash = false
                    }
                }

                RecappedNavGraph(
                    authState = authState,
                    spotifyCallbackUrl = spotifyCallbackUrl,
                    onSpotifyCallbackConsumed = {
                        spotifyCallbackUrl = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSpotifyCallback(intent)
    }

    private fun handleSpotifyCallback(intent: Intent?) {
        val data = intent?.data ?: return

        if (
            data.scheme == "com.recapped.app" &&
            data.host == "spotify-callback"
        ) {
            spotifyCallbackUrl = data.toString()
        }
    }
}
