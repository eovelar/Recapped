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
import com.recapped.app.ui.onboarding.OnboardingRoute
import com.recapped.app.ui.profile.EditProfileRoute
import com.recapped.app.ui.profile.ProfileRoute
import com.recapped.app.ui.profile.RecapHistoryRoute
import com.recapped.app.ui.recap.RecapGenRoute
import com.recapped.app.ui.recap.RecapResultRoute
import com.recapped.app.ui.songdetail.SongDetailRoute
import com.recapped.app.ui.theme.RecappedColors
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CHARTS = "charts"
    const val RECAP_GEN = "recap_gen"
    const val RECAP_RESULT = "recap_result"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val RECAP_HISTORY = "recap_history"
    const val DETAIL = "detail/{artistName}"
    const val SONG_DETAIL = "song_detail/{artistName}/{trackName}"

    fun detail(name: String): String {
        val encodedName = URLEncoder.encode(
            name,
            Charsets.UTF_8.name()
        )

        return "detail/$encodedName"
    }

    fun songDetail(
        artistName: String,
        trackName: String
    ): String {
        val encodedArtist = URLEncoder.encode(
            artistName,
            Charsets.UTF_8.name()
        )

        val encodedTrack = URLEncoder.encode(
            trackName,
            Charsets.UTF_8.name()
        )

        return "song_detail/$encodedArtist/$encodedTrack"
    }
}

private val TAB_ROUTES = setOf(
    Routes.HOME,
    Routes.CHARTS,
    Routes.RECAP_GEN,
    Routes.PROFILE
)

@Composable
fun RecappedNavGraph(
    authState: AuthUiState,
    spotifyCallbackUrl: String?,
    onSpotifyCallbackConsumed: () -> Unit
) {
    val nav = rememberNavController()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.SignedIn -> {
                nav.navigate(Routes.HOME) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }

            is AuthUiState.NeedsOnboarding -> {
                nav.navigate(Routes.ONBOARDING) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }

            AuthUiState.SignedOut -> {
                nav.navigate(Routes.LOGIN) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                RecappedBottomBar(
                    current = currentTab,
                    onSelect = { tab ->
                        nav.switchTab(tab)
                    }
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

            composable(Routes.ONBOARDING) {
                OnboardingRoute(
                    onCompleted = {
                        nav.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeRoute(
                    onArtistClick = { name ->
                        nav.navigate(Routes.detail(name))
                    },
                    onGoToRecap = {
                        nav.switchTab(RecappedTab.Recap)
                    }
                )
            }

            composable(Routes.CHARTS) {
                ChartsRoute(
                    onArtistClick = { name ->
                        nav.navigate(Routes.detail(name))
                    },
                    onSongClick = { artistName, trackName ->
                        nav.navigate(
                            Routes.songDetail(
                                artistName = artistName,
                                trackName = trackName
                            )
                        )
                    }
                )
            }

            composable(Routes.RECAP_GEN) {
                RecapGenRoute(
                    onRecapGenerated = {
                        nav.navigate(Routes.RECAP_RESULT)
                    }
                )
            }

            composable(Routes.RECAP_RESULT) {
                RecapResultRoute(
                    onBack = {
                        nav.popBackStack()
                    },
                    onShare = {
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileRoute(
                    onEditProfile = {
                        nav.navigate(Routes.EDIT_PROFILE)
                    },
                    onRecapHistory = {
                        nav.navigate(Routes.RECAP_HISTORY)
                    }
                )
            }

            composable(Routes.EDIT_PROFILE) {
                EditProfileRoute(
                    onBack = {
                        nav.popBackStack()
                    }
                )
            }

            composable(Routes.RECAP_HISTORY) {
                RecapHistoryRoute(
                    onBack = {
                        nav.popBackStack()
                    }
                )
            }

            composable(Routes.DETAIL) { backEntry ->
                val rawName = backEntry.arguments
                    ?.getString("artistName")
                    .orEmpty()

                val artistName = URLDecoder.decode(
                    rawName,
                    Charsets.UTF_8.name()
                )

                DetailRoute(
                    artistName = artistName,
                    spotifyCallbackUrl = spotifyCallbackUrl,
                    onSpotifyCallbackConsumed =
                        onSpotifyCallbackConsumed,
                    onSongClick = { trackName ->
                        nav.navigate(
                            Routes.songDetail(
                                artistName = artistName,
                                trackName = trackName
                            )
                        )
                    },
                    onBack = {
                        nav.popBackStack()
                    }
                )
            }

            composable(Routes.SONG_DETAIL) { backEntry ->
                val rawArtist = backEntry.arguments
                    ?.getString("artistName")
                    .orEmpty()

                val rawTrack = backEntry.arguments
                    ?.getString("trackName")
                    .orEmpty()

                val artistName = URLDecoder.decode(
                    rawArtist,
                    Charsets.UTF_8.name()
                )

                val trackName = URLDecoder.decode(
                    rawTrack,
                    Charsets.UTF_8.name()
                )

                SongDetailRoute(
                    artistName = artistName,
                    trackName = trackName,
                    spotifyCallbackUrl = spotifyCallbackUrl,
                    onSpotifyCallbackConsumed =
                        onSpotifyCallbackConsumed,
                    onBack = {
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}

private fun NavHostController.switchTab(
    tab: RecappedTab
) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}