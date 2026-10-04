package com.gaply.app.ui.screens.reset

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.components.GaplyTextField
import com.gaply.app.ui.theme.TextSecondary

@Composable
fun ResetPasswordScreen(
    identifier: String,
    onBack: () -> Unit,
    onPasswordUpdated: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ResetPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ResetPasswordViewModel.factory(context.appContainer(), identifier),
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.successHandled) {
        if (state.successHandled) {
            onPasswordUpdated()
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
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                )
            }
            Text(
                text = "Recuperar contraseña",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Crea una nueva contraseña",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Ingresa y confirma tu nueva contraseña para volver a iniciar sesión.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        GaplyTextField(
            value = state.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            label = "Nueva Contraseña",
            isPassword = true,
            errorText = state.newPasswordError,
        )

        Spacer(modifier = Modifier.height(12.dp))

        GaplyTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirmar Contraseña",
            isPassword = true,
            errorText = state.confirmPasswordError,
            imeAction = ImeAction.Done,
            onImeAction = viewModel::onSavePassword,
        )

        Spacer(modifier = Modifier.height(24.dp))

        GaplyPrimaryButton(
            text = "Guardar Contraseña",
            onClick = viewModel::onSavePassword,
            loading = state.isLoading,
        )
    }

    when (state.dialog) {
        ResetDialog.SUCCESS -> AppDialog(
            style = DialogStyle.SUCCESS,
            title = "¡Contraseña actualizada!",
            message = "¡Contraseña actualizada! Tu contraseña se ha cambiado con éxito. Ya puedes iniciar sesión con tus nuevos datos.",
            onDismiss = viewModel::consumeDialog,
        )

        ResetDialog.ERROR -> AppDialog(
            style = DialogStyle.ERROR,
            title = "¡Error al guardar su contraseña!",
            message = "¡Error al guardar su contraseña! Disculpa, inténtalo más tarde.",
            onDismiss = viewModel::consumeDialog,
        )

        null -> Unit
    }
}
