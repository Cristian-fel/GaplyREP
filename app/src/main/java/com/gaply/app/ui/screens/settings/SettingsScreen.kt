package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreenDark
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.GaplyTheme
import com.gaply.app.ui.theme.PlayfairDisplay
import com.gaply.app.ui.theme.TextSecondary

/**
 * Módulo 2 · FragmentConfiguracion.
 * Configuración general: cuenta y seguridad, preferencias (tema,
 * notificaciones, idioma), acerca de la app y cerrar sesión.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenChangePassword: () -> Unit = {},
    onOpenDeactivateOptions: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    onOpenRateApp: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SettingsViewModel.factory(context.appContainer()),
    )

    var showThemeDialog by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    val language by viewModel.language.collectAsState()
    val s = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        SettingsTopBar(
            title = s.settingsTitle,
            onBack = onBack,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ------------------------------------------------ Cuenta y Seguridad
        SettingsSectionCard(
            title = s.accountSecurity,
            headerIcon = Icons.Filled.Shield,
        ) {
            SettingsItemRow(
                icon = Icons.Filled.Key,
                title = s.changePasswords,
                subtitle = s.updateYourAccess,
                onClick = onOpenChangePassword,
            )
            SettingsItemDivider()
            SettingsItemRow(
                icon = Icons.Filled.Shield,
                title = s.deactivateDeleteAccount,
                subtitle = s.deactivateDeleteAccountSubtitle,
                onClick = onOpenDeactivateOptions,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------------------------------------------- Preferencias
        SettingsSectionCard(
            title = s.preferences,
            headerIcon = Icons.Filled.Settings,
        ) {
            SettingsItemRow(
                icon = Icons.Filled.DarkMode,
                title = s.theme,
                subtitle = s.enableDarkMode,
                onClick = { showThemeDialog = true },
            )
            SettingsItemDivider()
            SettingsItemRow(
                icon = Icons.Filled.Notifications,
                title = s.notifications,
                subtitle = s.configureAlerts,
                trailing = {
                    SettingsSwitch(checked = notifications, onCheckedChange = { notifications = it })
                },
            )
            SettingsItemDivider()
            SettingsItemRow(
                icon = Icons.Filled.Translate,
                title = s.language,
                subtitle = language.label,
                onClick = onOpenLanguage,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------------------------------------------------- Acerca
        SettingsSectionCard(
            title = s.aboutSection,
            headerIcon = Icons.Filled.Visibility,
        ) {
            SettingsItemRow(
                icon = Icons.Filled.Star,
                title = s.rateTheApp,
                onClick = onOpenRateApp,
            )
            SettingsItemDivider()
            SettingsItemRow(
                icon = Icons.Filled.Info,
                title = s.aboutGaply,
                onClick = onOpenAbout,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = s.copyrightText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            ),
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        GaplyOutlineButton(
            text = s.logout,
            onClick = {
                viewModel.signOut()
                onLogout()
            },
            borderColor = ErrorRed,
            contentColor = ErrorRed,
            shape = RoundedCornerShape(50),
            leadingIcon = Icons.AutoMirrored.Filled.Logout,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showThemeDialog) {
        AppDialog(
            style = DialogStyle.INFO,
            title = s.theme,
            message = s.themeSoonMessage,
            onDismiss = { showThemeDialog = false },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    GaplyTheme {
        SettingsScreen(onBack = {}, onLogout = {})
    }
}
