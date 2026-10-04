package com.gaply.app.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.components.GaplyTextField
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: (identifier: String) -> Unit,
    onLoggedIn: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = LoginViewModel.factory(context.appContainer()),
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onLoggedIn()
            viewModel.consumeNavigation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                )
            }
            Text(text = "Inicia Sesión", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bienvenido de nuevo",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Ingresa a tu cuenta para continuar con Gaply.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        GaplyTextField(
            value = state.identifier,
            onValueChange = viewModel::onIdentifierChange,
            label = "Usuario",
            errorText = state.identifierError,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
        )

        Spacer(modifier = Modifier.height(12.dp))

        GaplyTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Contraseña",
            isPassword = true,
            errorText = state.passwordError,
            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
            onImeAction = viewModel::onLogin,
        )

        if (state.credentialsError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.credentialsError.orEmpty(),
                color = ErrorRed,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Olvidaste tu contraseña?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onForgotPassword(state.identifier) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = state.receiveMarketing,
                onCheckedChange = viewModel::onMarketingToggle,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
            )
            Text(
                text = "Si no deseas recibir comunicaciones de marketing sobre nuestros productos y servicios, marca esta casilla.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GaplyPrimaryButton(
            text = "Ingresar",
            onClick = viewModel::onLogin,
            loading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "¿Aún no tienes cuenta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = "Regístrate",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onRegister),
            )
        }
    }

    when (state.dialog) {
        LoginDialog.CONNECTION -> AppDialog(
            style = DialogStyle.ERROR,
            title = "¡Error de conexión!",
            message = "¡Error de conexión! Comprueba tu conexión, inténtalo más tarde.",
            onDismiss = viewModel::consumeDialog,
        )

        LoginDialog.USER_NOT_FOUND -> AppDialog(
            style = DialogStyle.ERROR,
            title = "¡Cuenta no encontrada!",
            message = "No se han encontrado usuarios.",
            onDismiss = viewModel::consumeDialog,
        )

        null -> Unit
    }
}
