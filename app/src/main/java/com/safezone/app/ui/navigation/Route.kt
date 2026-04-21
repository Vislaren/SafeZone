package com.safezone.app.ui.navigation

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object BiometricLogin : Route("biometric_login")
    data object SignUp : Route("signup")
    data object ProfileSetup : Route("profile_setup")
    data object Home : Route("home")
    data object Map : Route("map")
    data object History : Route("history")
    data object Settings : Route("settings")
    data object PhraseSetup : Route("phrase_setup")
    data object SosActive : Route("sos_active")
    data object Alert : Route("alert/{eventId}") {
        fun of(id: String) = "alert/$id"
        const val ARG = "eventId"
    }
}
