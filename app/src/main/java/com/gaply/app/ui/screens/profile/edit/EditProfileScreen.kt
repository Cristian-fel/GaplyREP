package com.gaply.app.ui.screens.profile.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.TextSecondary

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: EditProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = EditProfileViewModel.factory(context.appContainer()),
    )
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current
    androidx.compose.runtime.SideEffect { viewModel.strings = s }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
            viewModel.consumeSaved()
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
                    if (state.step > 1) viewModel.onPreviousStep() else onBack()
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back,
                )
            }
            Text(text = s.editProfileTitle, style = MaterialTheme.typography.titleLarge)
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = s.stepOfTotal(state.step, EditProfileViewModel.TOTAL_STEPS),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(EditProfileViewModel.TOTAL_STEPS) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .background(
                                color = if (index < state.step) BrandGreen else Mint,
                                shape = RoundedCornerShape(4.dp),
                            ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.loadingProfile) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = BrandGreen)
            }
        } else {
            when (state.step) {
                1 -> EditStepBasicsContent(state = state, viewModel = viewModel)
                2 -> EditStepDetailsContent(state = state, viewModel = viewModel)
                else -> EditStepInterestsContent(state = state, viewModel = viewModel)
            }
        }
    }

    if (state.showPreview) {
        EditProfilePreview(state = state, onDismiss = viewModel::onTogglePreview)
    }

    when (state.dialog) {
        EditProfileDialog.SAVED -> AppDialog(
            style = DialogStyle.SUCCESS,
            title = s.changesSavedTitle,
            message = s.changesSavedMsg,
            onDismiss = viewModel::consumeDialog,
        )

        EditProfileDialog.ERROR_SAVE -> AppDialog(
            style = DialogStyle.ERROR,
            title = s.saveErrorTitle,
            message = s.editSaveErrorMsg,
            onDismiss = viewModel::consumeDialog,
        )

        EditProfileDialog.ERROR_CONNECTION -> AppDialog(
            style = DialogStyle.ERROR,
            title = s.connectionErrorTitle,
            message = s.connectionErrorMessage,
            onDismiss = viewModel::consumeDialog,
        )

        null -> Unit
    }
}
