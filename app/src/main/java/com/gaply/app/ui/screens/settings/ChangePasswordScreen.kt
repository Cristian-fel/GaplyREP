package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.core.i18n.fieldError
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.BrandGreenDark
import com.gaply.app.ui.theme.GaplyTheme
import com.gaply.app.ui.theme.PlayfairDisplay
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextPrimary

/**
 * Módulo 2 · Cambiar contraseña.
 * Dos campos con subrayado y ojo de visibilidad; al confirmar se muestra
 * el modal "¡Contraseña actualizada!" (borde verde y candado).
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

        UnderlinePasswordField(
            label = s.newPasswordLabel,
            value = viewModel.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            errorText = viewModel.newPasswordError?.let { s.fieldError(it) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        UnderlinePasswordField(
            label = s.confirmPasswordLabel,
            value = viewModel.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
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

            UnderlinePasswordField(
                label = s.currentPasswordLabel,
                value = viewModel.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                errorText = viewModel.currentPasswordError?.let { s.fieldError(it) },
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        GaplyPrimaryButton(
            text = s.changePasswordButton,
            onClick = viewModel::submit,
            enabled = viewModel.canSubmit,
            loading = viewModel.loading,
            containerColor = if (viewModel.canSubmit) BrandGreenDark else Sage,
            contentColor = SurfaceWhite,
            shape = RoundedCornerShape(50),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
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

/**
 * Campo de contraseña con etiqueta en negrita, texto con subrayado
 * e icono de ojo, replicando el estilo de los mockups.
 */
@Composable
private fun UnderlinePasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    val s = LocalStrings.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
            ),
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
                cursorBrush = SolidColor(BrandGreen),
                visualTransformation = if (visible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) s.hidePassword else s.showPassword,
                    tint = TextPrimary,
                )
            }
        }

        HorizontalDivider(color = TextPrimary, thickness = 1.5.dp)

        if (errorText != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ChangePasswordScreenPreview() {
    GaplyTheme {
        ChangePasswordScreen(onBack = {})
    }
}
