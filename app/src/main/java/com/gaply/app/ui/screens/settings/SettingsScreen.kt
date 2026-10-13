package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.GaplyTheme
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
    onOpenDeactivateOptions: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SettingsViewModel.factory(context.appContainer()),
    )

    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var infoDialog by remember { mutableStateOf<String?>(null) }

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
            title = "Configuración",
            onBack = onBack,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ------------------------------------------------ Cuenta y Seguridad
        SettingsSectionCard(title = "Cuenta y Seguridad") {
            SettingsItemRow(
                icon = Icons.Filled.Lock,
                title = "Cambiar contraseñas",
                subtitle = "Actualiza tu acceso",
                onClick = { infoDialog = "cambiar contraseña" },
            )
            SettingsItemDivider()
            SettingsItemRow(
                icon = Icons.Filled.Shield,
                title = "Desactivar y Eliminar Cuenta",
                subtitle = "Doble factor de autenticación",
                onClick = onOpenDeactivateOptions,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------------------------------------------- Preferencias
        SettingsSectionCard(title = "Preferencias") {
            SettingsItemRow(
                icon = Icons.Filled.DarkMode,
                title = "Tema",
                subtitle = "Activa el modo oscuro",
                trailing = {
                    SettingsSwitch(checked = darkMode, onCheckedChange = { darkMode = it })
                },
            )
            SettingsItemDivider()
            SettingsItemRow(
                icon = Icons.Filled.Notifications,
                title = "Notificaciones",
                subtitle = "Configura tus alertas",
                trailing = {
                    SettingsSwitch(checked = notifications, onCheckedChange = { notifications = it })
                },
            )
            SettingsItemDivider()
            SettingsItemRow(
                icon = Icons.Filled.ChatBubbleOutline,
                title = "Idioma",
                subtitle = "Español (ES)",
                onClick = { infoDialog = "idioma" },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------------------------------------------------- Acerca
        SettingsSectionCard(title = "Acerca") {
            SettingsItemRow(
                icon = Icons.Filled.Star,
                title = "Calificar la app",
                onClick = { infoDialog = "calificar la app" },
            )
            SettingsItemDivider()
            SettingsItemRow(
                icon = Icons.Filled.Info,
                title = "Acerca de Gaply",
                onClick = { infoDialog = "acerca de Gaply" },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "© Gaply 2026 - Version 1.0",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        GaplyOutlineButton(
            text = "Cerrar sesión",
            onClick = {
                viewModel.signOut()
                onLogout()
            },
            borderColor = ErrorRed,
            contentColor = ErrorRed,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    infoDialog?.let { feature ->
        AppDialog(
            style = DialogStyle.INFO,
            title = "Próximamente",
            message = "La opción de $feature estará disponible en la versión final.",
            onDismiss = { infoDialog = null },
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
