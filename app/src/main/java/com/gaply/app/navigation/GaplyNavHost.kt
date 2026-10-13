package com.gaply.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gaply.app.ui.screens.home.HomeScreen
import com.gaply.app.ui.screens.login.LoginScreen
import com.gaply.app.ui.screens.profile.edit.EditProfileScreen
import com.gaply.app.ui.screens.register.RegisterScreen
import com.gaply.app.ui.screens.reset.ResetPasswordScreen
import com.gaply.app.ui.screens.schedule.HuecosScreen
import com.gaply.app.ui.screens.schedule.ScheduleEditScreen
import com.gaply.app.ui.screens.schedule.ScheduleScreen
import com.gaply.app.ui.screens.settings.AccountOptionsScreen
import com.gaply.app.ui.screens.settings.ConfirmDeactivateScreen
import com.gaply.app.ui.screens.settings.SettingsScreen
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
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onOpenSchedule = { navController.navigate(Routes.SCHEDULE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.SCHEDULE) {
            ScheduleScreen(
                onBack = { navController.popBackStack() },
                onAddClass = { dia ->
                    navController.navigate(Routes.scheduleEdit(dia = dia))
                },
                onEditClass = { classId ->
                    navController.navigate(Routes.scheduleEdit(classId = classId))
                },
                onOpenHuecos = { dia, inicio, fin ->
                    navController.navigate(Routes.huecos(dia = dia, inicio = inicio, fin = fin))
                },
            )
        }

        composable(
            // El patrón DEBE declarar los query params con {llaves}: sin eso,
            // Navigation descarta classId/dia al navegar y el formulario siempre
            // abría una clase nueva (al guardar, se duplicaba en vez de editar).
            route = "${Routes.SCHEDULE_EDIT}?${Routes.ARG_CLASS_ID}={${Routes.ARG_CLASS_ID}}" +
                "&${Routes.ARG_DIA}={${Routes.ARG_DIA}}",
            arguments = listOf(
                navArgument(Routes.ARG_CLASS_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(Routes.ARG_DIA) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            ScheduleEditScreen(
                classId = entry.arguments?.getString(Routes.ARG_CLASS_ID).orEmpty(),
                diaInicial = entry.arguments?.getString(Routes.ARG_DIA).orEmpty(),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = "${Routes.HUECOS}?${Routes.ARG_DIA}={${Routes.ARG_DIA}}" +
                "&${Routes.ARG_INICIO}={${Routes.ARG_INICIO}}&${Routes.ARG_FIN}={${Routes.ARG_FIN}}",
            arguments = listOf(
                navArgument(Routes.ARG_DIA) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(Routes.ARG_INICIO) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(Routes.ARG_FIN) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            HuecosScreen(
                onBack = { navController.popBackStack() },
                diaInicial = entry.arguments?.getString(Routes.ARG_DIA).orEmpty(),
                inicioInicial = entry.arguments?.getString(Routes.ARG_INICIO).orEmpty(),
                finInicial = entry.arguments?.getString(Routes.ARG_FIN).orEmpty(),
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenDeactivateOptions = { navController.navigate(Routes.ACCOUNT_OPTIONS) },
            )
        }

        composable(Routes.ACCOUNT_OPTIONS) {
            AccountOptionsScreen(
                onBack = { navController.popBackStack() },
                onDeactivate = { navController.navigate(Routes.CONFIRM_DEACTIVATE) },
                onDeleteAccount = { navController.navigate(Routes.CONFIRM_DEACTIVATE) },
            )
        }

        composable(Routes.CONFIRM_DEACTIVATE) {
            ConfirmDeactivateScreen(
                onBack = { navController.popBackStack() },
                onConfirm = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
    }
}
