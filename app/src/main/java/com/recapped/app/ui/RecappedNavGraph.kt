package com.recapped.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.recapped.app.ui.charts.ChartsRoute
import com.recapped.app.ui.detail.DetailRoute
import com.recapped.app.ui.login.LoginRoute
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val LOGIN = "login"
    const val CHARTS = "charts"
    const val DETAIL = "detail/{artistName}"
    fun detail(name: String): String =
        "detail/" + URLEncoder.encode(name, Charsets.UTF_8.name())
}

@Composable
fun RecappedNavGraph(authState: AuthUiState) {
    val nav = rememberNavController()

    // Re-routea según cambios de sesión (login/logout) en caliente.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.SignedIn -> nav.navigate(Routes.CHARTS) {
                popUpTo(0) { inclusive = true }
            }
            AuthUiState.SignedOut -> nav.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
            AuthUiState.Checking -> Unit
        }
    }

    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginRoute() // Auth observer del Root se encarga de avanzar
        }
        composable(Routes.CHARTS) {
            ChartsRoute(
                onArtistClick = { name -> nav.navigate(Routes.detail(name)) }
            )
        }
        composable(Routes.DETAIL) { backStack ->
            val raw = backStack.arguments?.getString("artistName").orEmpty()
            val name = URLDecoder.decode(raw, Charsets.UTF_8.name())
            DetailRoute(
                artistName = name,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
