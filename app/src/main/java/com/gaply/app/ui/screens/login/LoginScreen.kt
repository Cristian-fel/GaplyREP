package com.gaply.app.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gaply.app.R
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.core.i18n.fieldError
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
    val s = LocalStrings.current

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onLoggedIn()
            viewModel.consumeNavigation()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Imagen de fondo desvanecida (edificio) anclada al fondo
        Image(
            painter = painterResource(id = R.drawable.bg_building_bottom),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .alpha(0.10f)
                .offset(y = 245.dp),
        )

        // Contenido scrollable completo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = s.loginTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            GaplyTextField(
                value = state.identifier,
                onValueChange = viewModel::onIdentifierChange,
                label = s.usernameLabel,
                errorText = state.identifierError?.let { s.fieldError(it) },
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = s.passwordLabel,
                isPassword = true,
                errorText = state.passwordError?.let { s.fieldError(it) },
                imeAction = ImeAction.Done,
                onImeAction = viewModel::onLogin,
            )

            if (state.credentialsError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (state.credentialsError) {
                        CredentialsError.EMAIL_NOT_FOUND -> s.noAccountWithEmailMsg
                        CredentialsError.WRONG_PASSWORD -> s.wrongPasswordMsg
                        CredentialsError.GENERIC -> s.loginFailedMsg
                        null -> ""
                    },
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = s.forgotPassword,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onForgotPassword(state.identifier) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            GaplyPrimaryButton(
                text = s.loginButton,
                onClick = viewModel::onLogin,
                loading = state.isLoading,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = s.noAccountYet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Text(
                    text = s.registerLink,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onRegister),
                )
            }

            // Espaciado previo al checkbox según el layout de Figma
            Spacer(modifier = Modifier.height(240.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = state.receiveMarketing,
                    onCheckedChange = viewModel::onMarketingToggle,
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = s.marketingOptOut,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }

    when (state.dialog) {
        LoginDialog.CONNECTION -> AppDialog(
            style = DialogStyle.ERROR,
            title = s.connectionErrorTitle,
            message = s.connectionErrorMessage,
            onDismiss = viewModel::consumeDialog,
        )

        LoginDialog.USER_NOT_FOUND -> AppDialog(
            style = DialogStyle.WARNING,
            title = s.userNotFoundTitle,
            message = s.userNotFoundMsg,
            onDismiss = viewModel::consumeDialog,
        )

        null -> Unit
    }
}