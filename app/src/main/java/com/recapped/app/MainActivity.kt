package com.recapped.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

    override fun onCreate(savedInstanceState: Bundle?) {
        // API nativa de Splash Screen (Android 12+; compat hasta API 23 vía AndroidX).
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Mantenemos el splash visible hasta que RootViewModel resuelva si hay sesión.
        var keepSplash = true
        splash.setKeepOnScreenCondition { keepSplash }

        setContent {
            RecappedTheme {
                val rootVm: RootViewModel = hiltViewModel()
                val authState by rootVm.authState.collectAsStateWithLifecycle()

                // Liberar splash sólo cuando ya conocemos el estado real (no Checking).
                LaunchedEffect(authState) {
                    if (authState !is AuthUiState.Checking) keepSplash = false
                }

                RecappedNavGraph(authState = authState)
            }
        }
    }
}
