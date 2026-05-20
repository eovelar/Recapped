package com.recapped.app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.recapped.app.ui.charts.ChartsRoute
import com.recapped.app.ui.components.RecappedBottomBar
import com.recapped.app.ui.components.RecappedTab
import com.recapped.app.ui.detail.DetailRoute
import com.recapped.app.ui.home.HomeRoute
import com.recapped.app.ui.login.LoginRoute
import com.recapped.app.ui.profile.ProfileRoute
import com.recapped.app.ui.recap.RecapGenRoute
import com.recapped.app.ui.theme.RecappedColors
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CHARTS = "charts"
    const val RECAP_GEN = "recap_gen"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{artistName}"
    fun detail(name: String): String =
        "detail/" + URLEncoder.encode(name, Charsets.UTF_8.name())
}

/** Rutas que conviven con el bottom bar. Detail y Login quedan a pantalla completa. */
private val TAB_ROUTES = setOf(
    Routes.HOME, Routes.CHARTS, Routes.RECAP_GEN, Routes.PROFILE
)

@Composable
fun RecappedNavGraph(authState: AuthUiState) {
    val nav = rememberNavController()

    // Re-routea según cambios de sesión (login/logout) en caliente.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.SignedIn -> nav.navigate(Routes.HOME) {
                popUpTo(0) { inclusive = true }
            }
            AuthUiState.SignedOut -> nav.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
            AuthUiState.Checking -> Unit
        }
    }

    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in TAB_ROUTES
    val currentTab = RecappedTab.fromRoute(currentRoute)

    Scaffold(
        containerColor = RecappedColors.Background,
        contentColor = Color.White,
        // Cada Route maneja su propio status-bar inset. Scaffold sólo
        // se encarga del bottom bar para no duplicar el top padding.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                RecappedBottomBar(
                    current = currentTab,
                    onSelect = { tab -> nav.switchTab(tab) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.LOGIN) {
                LoginRoute()
            }
            composable(Routes.HOME) {
                HomeRoute(
                    onArtistClick = { name -> nav.navigate(Routes.detail(name)) },
                    onGoToRecap = { nav.switchTab(RecappedTab.Recap) }
                )
            }
            composable(Routes.CHARTS) {
                ChartsRoute(
                    onArtistClick = { name -> nav.navigate(Routes.detail(name)) }
                )
            }
            composable(Routes.RECAP_GEN) {
                RecapGenRoute()
            }
            composable(Routes.PROFILE) {
                ProfileRoute()
            }
            composable(Routes.DETAIL) { backEntry ->
                val raw = backEntry.arguments?.getString("artistName").orEmpty()
                val name = URLDecoder.decode(raw, Charsets.UTF_8.name())
                DetailRoute(
                    artistName = name,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

/**
 * Navegación entre tabs. Patrón estándar de Compose Navigation:
 *  - popUpTo(startDestination, saveState=true): no acumulamos back-stack tras tabs
 *  - launchSingleTop: no instanciamos dos veces el mismo destination
 *  - restoreState: reusa el state previo del tab (scroll, búsqueda, etc.)
 */
private fun NavHostController.switchTab(tab: RecappedTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
