package com.gaply.app.ui.screens.reset

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gaply.app.R
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

    Box(modifier = Modifier.fillMaxSize()) {

        // Imagen de fondo colocada bien abajo (offset desplaza la base para que quede tras el botón)
        Image(
            painter = painterResource(id = R.drawable.bg_building_bottom),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .alpha(0.08f)
                .offset(y = 120.dp)
        )

        // Contenido interactivo por encima de la imagen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // Contenido superior
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
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
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Recupera tu contraseña",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ingresa tu correo electrónico y te enviaremos un enlace para crear una nueva contraseña.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                GaplyTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Correo electrónico",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    onImeAction = viewModel::onSendEmail,
                    errorText = state.emailError,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Botón anclado abajo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GaplyPrimaryButton(
                    text = "Enviar Correo",
                    onClick = viewModel::onSendEmail,
                    loading = state.isLoading,
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    when (state.dialog) {
        ResetDialog.SUCCESS -> AppDialog(
            style = DialogStyle.SUCCESS,
            title = "¡Correo enviado!",
            message = "Hemos enviado un enlace a tu correo para que crees una nueva contraseña. Revisa tu bandeja de entrada.",
            onDismiss = viewModel::consumeDialog,
        )

        ResetDialog.ERROR -> AppDialog(
            style = DialogStyle.ERROR,
            title = "No pudimos enviar el correo",
            message = "Verifica tu correo o tu conexión e inténtalo más tarde.",
            onDismiss = viewModel::consumeDialog,
        )

        null -> Unit
    }
}