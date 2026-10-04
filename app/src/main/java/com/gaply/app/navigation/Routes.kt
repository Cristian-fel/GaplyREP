package com.gaply.app.navigation

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val RESET = "reset"
    const val REGISTER = "register"
    const val HOME = "home"

    const val ARG_IDENTIFIER = "identifier"

    fun reset(identifier: String): String =
        "$RESET?$ARG_IDENTIFIER=${android.net.Uri.encode(identifier)}"
}
