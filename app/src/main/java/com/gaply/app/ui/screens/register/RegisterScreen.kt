package com.gaply.app.ui.screens.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.theme.TextSecondary

@Composable
fun RegisterScreen(
    onExit: () -> Unit,
    onRegistered: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: RegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = RegisterViewModel.factory(context.appContainer()),
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.finishAndGoHome) {
        if (state.finishAndGoHome) {
            onRegistered()
            viewModel.consumeFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(
                onClick = {
                    if (state.step > 1) viewModel.onPreviousStep() else onExit()
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                )
            }
            Text(text = "Crear Cuenta", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.weight(1f))
            if (state.step == 3 || state.step == 4) {
                TextButton(onClick = viewModel::onSkipStep) {
                    Text(text = "Omitir", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Paso ${state.step} de 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = when (state.step) {
                        1 -> "Datos de la cuenta"
                        2 -> "Informaci\u00f3n personal"
                        3 -> "Prop\u00f3sito"
                        4 -> "Intereses"
                        else -> "Foto y biograf\u00eda"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.step / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state.step) {
            1 -> StepAccountContent(state = state, viewModel = viewModel)
            2 -> StepPersonalContent(state = state, viewModel = viewModel)
            3 -> StepGoalContent(state = state, viewModel = viewModel)
            4 -> StepInterestsContent(state = state, viewModel = viewModel)
            5 -> StepPhotoContent(state = state, viewModel = viewModel)
        }
    }

    when (state.dialog) {
        RegisterDialog.SAVED_PROFILE -> AppDialog(
            style = DialogStyle.SUCCESS,
            title = "\u00a1Tu perfil se ha guardado!",
            message = "\u00a1Tu perfil se ha guardado! La informaci\u00f3n de tu perfil se ha guardado correctamente.",
            onDismiss = viewModel::consumeDialog,
        )

        RegisterDialog.GALLERY_INFO -> AppDialog(
            style = DialogStyle.INFO,
            title = "Galer\u00eda de fotos",
            message = "En la versi\u00f3n final, este bot\u00f3n abrir\u00e1 tu galer\u00eda de fotos.",
            onDismiss = viewModel::consumeDialog,
        )

        RegisterDialog.CAMERA_INFO -> AppDialog(
            style = DialogStyle.INFO,
            title = "C\u00e1mara",
            message = "En la versi\u00f3n final, este bot\u00f3n abrir\u00e1 la c\u00e1mara de tu celular.",
            onDismiss = viewModel::consumeDialog,
        )

        RegisterDialog.ERROR_SAVE -> AppDialog(
            style = DialogStyle.ERROR,
            title = "\u00a1Error al guardar!",
            message = "\u00a1Error al guardar el perfil! Disculpa, int\u00e9ntalo m\u00e1s tarde.",
            onDismiss = viewModel::consumeDialog,
        )

        RegisterDialog.ERROR_CONNECTION -> AppDialog(
            style = DialogStyle.ERROR,
            title = "\u00a1Error de conexi\u00f3n!",
            message = "\u00a1Error de conexi\u00f3n! Comprueba tu conexi\u00f3n, int\u00e9ntalo m\u00e1s tarde.",
            onDismiss = viewModel::consumeDialog,
        )

        null -> Unit
    }
}
