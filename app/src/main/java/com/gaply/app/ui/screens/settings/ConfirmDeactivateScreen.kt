package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreenDark
import com.gaply.app.ui.theme.GaplyTheme

/**
 * Módulo 2 · FragmentConfirmarDesactivar.
 * Confirma la desactivación temporal de la cuenta con la lista de
 * consecuencias (viñetas verdes) y el botón principal "Desactivar".
 */
@Composable
fun ConfirmDeactivateScreen(
    onBack: () -> Unit,
    onDeactivated: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: DeactivateViewModel = viewModel(
        factory = DeactivateViewModel.factory(context.appContainer()),
    )
    val s = LocalStrings.current

    val bullets = listOf(
        s.confirmDeactivateBulletPrivacy,
        s.confirmDeactivateBulletOutsideData,
        s.confirmDeactivateBulletDataKept,
        s.confirmDeactivateBulletRecovery,
    )

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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = s.confirmDeactivateTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = s.confirmDeactivateListTitle,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        bullets.forEach { bullet ->
            BulletItem(text = bullet)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        GaplyPrimaryButton(
            text = s.confirmDeactivateButton,
            onClick = {
                viewModel.deactivate(onDeactivated = onDeactivated)
            },
            loading = viewModel.loading,
            containerColor = BrandGreenDark,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    viewModel.errorMessage?.let { error ->
        AppDialog(
            style = DialogStyle.ERROR,
            title = if (error == DeactivateError.CONNECTION) {
                s.connectionErrorTitle
            } else {
                s.deactivateErrorTitle
            },
            message = when (error) {
                DeactivateError.CONNECTION -> s.connectionErrorMessage
                DeactivateError.PERMISSION -> s.deactivateReloginMsg
                DeactivateError.USER_NOT_FOUND -> s.noActiveSessionMsg
                DeactivateError.GENERIC -> s.deactivateErrorMsg
            },
            onDismiss = viewModel::consumeError,
        )
    }
}

/** Punto verde oscuro + texto del listado informativo. */
@Composable
private fun BulletItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(BrandGreenDark),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConfirmDeactivateScreenPreview() {
    GaplyTheme {
        ConfirmDeactivateScreen(onBack = {})
    }
}
