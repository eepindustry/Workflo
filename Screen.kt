package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Welcome : Screen("welcome")
    object Dashboard : Screen("dashboard")
    object Quiz : Screen("quiz")
}
