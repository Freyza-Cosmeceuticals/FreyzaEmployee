package com.freyza.employee

private object Route {
    const val HOME = "home"
    const val LOGIN = "login"
    const val ENTRY = "entry"
}

sealed class Screen(val route: String) {
    object Home : Screen(Route.HOME)
    object Login : Screen(Route.LOGIN)
    object Entry : Screen(Route.ENTRY)
}
