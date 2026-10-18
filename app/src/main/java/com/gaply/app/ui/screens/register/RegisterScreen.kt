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
import com.gaply.app.core.i18n.LocalStrings
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
    val s = LocalStrings.current

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
                    contentDescription = s.back,
                )
            }
            Text(text = s.createAccount, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.weight(1f))
            if (state.step == 3 || state.step == 4) {
                TextButton(onClick = viewModel::onSkipStep) {
                    Text(text = s.skip, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = s.stepOfTotal(state.step, 5),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = when (state.step) {
                        1 -> s.stepAccountData
                        2 -> s.stepPersonalInfo
                        3 -> s.stepPurpose
                        4 -> s.stepInterests
                        else -> s.stepPhotoBio
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
            title = s.profileSavedTitle,
            message = s.profileSavedMsg,
            onDismiss = viewModel::consumeDialog,
        )

        RegisterDialog.GALLERY_INFO -> AppDialog(
            style = DialogStyle.INFO,
            title = s.galleryInfoTitle,
            message = s.galleryInfoMsg,
            onDismiss = viewModel::consumeDialog,
        )

        RegisterDialog.CAMERA_INFO -> AppDialog(
            style = DialogStyle.INFO,
            title = s.cameraInfoTitle,
            message = s.cameraInfoMsg,
            onDismiss = viewModel::consumeDialog,
        )

        RegisterDialog.ERROR_SAVE -> AppDialog(
            style = DialogStyle.ERROR,
            title = s.saveErrorTitle,
            message = s.saveErrorMsg,
            onDismiss = viewModel::consumeDialog,
        )

        RegisterDialog.ERROR_CONNECTION -> AppDialog(
            style = DialogStyle.ERROR,
            title = s.connectionErrorTitle,
            message = s.connectionErrorMessage,
            onDismiss = viewModel::consumeDialog,
        )

        null -> Unit
    }
}
