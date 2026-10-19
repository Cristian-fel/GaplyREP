package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.core.i18n.fieldError
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.components.GaplyTextField
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.GaplyTheme

/**
 * Módulo 2 · Cambiar contraseña.
 * Dos campos con contenedor estándar de la app y ojo de visibilidad;
 * al confirmar se muestra el modal "¡Contraseña actualizada!"
 * (borde verde y candado).
 */
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ChangePasswordViewModel = viewModel(
        factory = ChangePasswordViewModel.factory(context.appContainer()),
    )
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

        SettingsBackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = s.changePasswordTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        GaplyTextField(
            value = viewModel.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            label = s.newPasswordLabel,
            isPassword = true,
            errorText = viewModel.newPasswordError?.let { s.fieldError(it) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        GaplyTextField(
            value = viewModel.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = s.confirmPasswordLabel,
            isPassword = true,
            errorText = viewModel.confirmPasswordError?.let { s.fieldError(it) },
        )

        if (viewModel.needsReauth) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = s.changePasswordReloginMsg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyTextField(
                value = viewModel.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                label = s.currentPasswordLabel,
                isPassword = true,
                errorText = viewModel.currentPasswordError?.let { s.fieldError(it) },
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        GaplyPrimaryButton(
            text = s.changePasswordButton,
            onClick = viewModel::submit,
            enabled = viewModel.canSubmit,
            loading = viewModel.loading,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    viewModel.errorMessage?.let { error ->
        AppDialog(
            style = DialogStyle.ERROR,
            title = if (error == ChangePasswordError.CONNECTION) {
                s.connectionErrorTitle
            } else {
                s.changePasswordErrorTitle
            },
            message = when (error) {
                ChangePasswordError.CONNECTION -> s.connectionErrorMessage
                ChangePasswordError.PERMISSION -> s.changePasswordReloginMsg
                ChangePasswordError.USER_NOT_FOUND -> s.noActiveSessionMsg
                ChangePasswordError.GENERIC -> s.changePasswordErrorMsg
            },
            onDismiss = viewModel::consumeError,
        )
    }

    if (viewModel.success) {
        AppDialog(
            style = DialogStyle.SUCCESS,
            title = s.passwordUpdatedTitle,
            message = s.passwordUpdatedMsg,
            onDismiss = {
                viewModel.consumeSuccess()
                onBack()
            },
            showConfirmButton = false,
            icon = Icons.Filled.Lock,
            iconTint = BrandGreen,
            borderColor = BrandGreen,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ChangePasswordScreenPreview() {
    GaplyTheme {
        ChangePasswordScreen(onBack = {})
    }
}
