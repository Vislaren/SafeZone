package com.safezone.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safezone.app.ui.screens.alert.AlertScreen
import com.safezone.app.ui.screens.auth.BiometricLoginScreen
import com.safezone.app.ui.screens.auth.LoginScreen
import com.safezone.app.ui.screens.auth.SignUpScreen
import com.safezone.app.ui.screens.history.HistoryScreen
import com.safezone.app.ui.screens.home.HomeScreen
import com.safezone.app.ui.screens.map.MapScreen
import com.safezone.app.ui.screens.profile.ProfileSetupScreen
import com.safezone.app.ui.screens.settings.PhraseSetupScreen
import com.safezone.app.ui.screens.settings.SettingsScreen
import com.safezone.app.ui.screens.sos.SosActiveScreen
import com.safezone.app.ui.screens.splash.SplashScreen

@Composable
fun SafeZoneNavGraph(initialAlertId: String? = null) {
    val nav = rememberNavController()

    // Deep link: push the Alert screen on top as soon as we mount.
    LaunchedEffect(initialAlertId) {
        if (!initialAlertId.isNullOrBlank()) {
            nav.navigate(Route.Alert.of(initialAlertId))
        }
    }

    NavHost(navController = nav, startDestination = Route.Splash.path) {
        composable(Route.Splash.path) {
            SplashScreen(
                onAuthenticated = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                },
                onNeedsLogin = {
                    nav.navigate(Route.Login.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Login.path) {
            LoginScreen(
                onLoggedIn = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onGoSignUp = { nav.navigate(Route.SignUp.path) },
                onBiometric = { nav.navigate(Route.BiometricLogin.path) }
            )
        }
        composable(Route.BiometricLogin.path) {
            BiometricLoginScreen(
                onAuthenticated = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onFallbackPassword = { nav.popBackStack() }
            )
        }
        composable(Route.SignUp.path) {
            SignUpScreen(
                onSignedUp = {
                    nav.navigate(Route.ProfileSetup.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onGoLogin = { nav.popBackStack() }
            )
        }
        composable(Route.ProfileSetup.path) {
            ProfileSetupScreen(
                onSaved = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.ProfileSetup.path) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Home.path) {
            HomeScreen(
                onOpenSettings = { nav.navigate(Route.Settings.path) },
                onOpenMap = { nav.navigate(Route.Map.path) },
                onOpenHistory = { nav.navigate(Route.History.path) },
                onSosActive = { nav.navigate(Route.SosActive.path) },
                onOpenProfile = { nav.navigate(Route.ProfileSetup.path) }
            )
        }
        composable(Route.Map.path) {
            MapScreen(
                onOpenAlert = { id -> nav.navigate(Route.Alert.of(id)) },
                onBack = { nav.popBackStack() },
                onTabHome = { nav.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } },
                onTabHistory = { nav.navigate(Route.History.path) },
                onTabSettings = { nav.navigate(Route.Settings.path) }
            )
        }
        composable(Route.History.path) {
            HistoryScreen(
                onTabHome = { nav.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } },
                onTabMap = { nav.navigate(Route.Map.path) },
                onTabSettings = { nav.navigate(Route.Settings.path) }
            )
        }
        composable(Route.Settings.path) {
            SettingsScreen(
                onPhraseSetup = { nav.navigate(Route.PhraseSetup.path) },
                onSignOut = {
                    nav.navigate(Route.Login.path) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onTabHome = { nav.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } },
                onTabMap = { nav.navigate(Route.Map.path) },
                onTabHistory = { nav.navigate(Route.History.path) }
            )
        }
        composable(Route.PhraseSetup.path) {
            PhraseSetupScreen(onBack = { nav.popBackStack() })
        }
        composable(Route.SosActive.path) {
            SosActiveScreen(
                onEnded = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.Home.path) { inclusive = true }
                    }
                }
            )
        }
        composable(
            Route.Alert.path,
            arguments = listOf(navArgument(Route.Alert.ARG) { type = NavType.StringType })
        ) { backStack ->
            AlertScreen(
                eventId = backStack.arguments?.getString(Route.Alert.ARG).orEmpty(),
                onNavigate = { nav.popBackStack() },
                onAssist = { nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
