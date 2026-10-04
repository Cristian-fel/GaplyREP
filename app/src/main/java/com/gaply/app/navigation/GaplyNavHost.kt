package com.gaply.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gaply.app.ui.screens.home.HomeScreen
import com.gaply.app.ui.screens.login.LoginScreen
import com.gaply.app.ui.screens.register.RegisterScreen
import com.gaply.app.ui.screens.reset.ResetPasswordScreen
import com.gaply.app.ui.screens.welcome.WelcomeScreen

@Composable
fun GaplyNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME,
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onLogin = { navController.navigate(Routes.LOGIN) },
                onRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onRegister = { navController.navigate(Routes.REGISTER) },
                onForgotPassword = { identifier ->
                    navController.navigate(Routes.reset(identifier))
                },
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = "${Routes.RESET}?${Routes.ARG_IDENTIFIER}={${Routes.ARG_IDENTIFIER}}",
            arguments = listOf(
                navArgument(Routes.ARG_IDENTIFIER) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            ResetPasswordScreen(
                identifier = entry.arguments?.getString(Routes.ARG_IDENTIFIER).orEmpty(),
                onBack = { navController.popBackStack() },
                onPasswordUpdated = { navController.popBackStack() },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onExit = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
