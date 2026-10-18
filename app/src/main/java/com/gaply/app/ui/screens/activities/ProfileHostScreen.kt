package com.gaply.app.ui.screens.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.ui.screens.profile.ProfileScreen

/**
 * Módulo de Actividades · Host de la pantalla de Perfil.
 *
 * La Perfil del proyecto vive como pestaña del Home y no tenía ruta propia.
 * Este host la reutiliza TAL CUAL (sin modificarla) para poder navegar a
 * ella desde el avatar del formulario "Nueva actividad".
 */
@Composable
fun ProfileHostScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSchedule: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val user by context.appContainer().authRepository.currentUser.collectAsState()
    val s = LocalStrings.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back,
                )
            }
            Text(
                text = s.tabProfile,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        ProfileScreen(
            user = user,
            onLogout = onLogout,
            onEditProfile = onEditProfile,
            onOpenSettings = onOpenSettings,
            onOpenSchedule = onOpenSchedule,
        )
    }
}
