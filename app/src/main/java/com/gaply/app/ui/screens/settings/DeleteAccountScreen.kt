package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreenDark
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.GaplyTheme
import com.gaply.app.ui.theme.PlayfairDisplay
import com.gaply.app.ui.theme.SurfaceWhite

/**
 * Módulo 2 · Eliminar cuenta (permanente).
 * Lista las consecuencias en rojo y ejecuta la eliminación real
 * vía [DeleteAccountViewModel]; al terminar se vuelve a Welcome.
 */
@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: DeleteAccountViewModel = viewModel(
        factory = DeleteAccountViewModel.factory(context.appContainer()),
    )
    val s = LocalStrings.current

    val warnings = listOf(
        s.deleteWarningPermanent,
        s.deleteWarningProfileData,
        s.deleteWarningActivity,
        s.deleteWarningIrreversible,
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = s.deleteAccountConfirmTitle,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        warnings.forEachIndexed { index, warning ->
            WarningItem(
                text = warning,
                triangleIcon = index == 2,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        GaplyPrimaryButton(
            text = s.deleteAccountButton,
            onClick = {
                viewModel.deleteAccount(onDeleted = onDeleted)
            },
            loading = viewModel.loading,
            containerColor = BrandGreenDark,
            contentColor = SurfaceWhite,
            shape = RoundedCornerShape(50),
            leadingIcon = Icons.Filled.DeleteForever,
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
            title = if (error == DeleteAccountError.CONNECTION) {
                s.connectionErrorTitle
            } else {
                s.deleteAccountErrorTitle
            },
            message = when (error) {
                DeleteAccountError.CONNECTION -> s.connectionErrorMessage
                DeleteAccountError.PERMISSION -> s.deleteAccountReloginMsg
                DeleteAccountError.USER_NOT_FOUND -> s.noActiveSessionMsg
                DeleteAccountError.GENERIC -> s.deleteAccountErrorMsg
            },
            onDismiss = viewModel::consumeError,
        )
    }
}

/** Fila de advertencia: icono rojo (círculo o triángulo) + texto. */
@Composable
private fun WarningItem(
    text: String,
    triangleIcon: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = if (triangleIcon) {
                Icons.Filled.Warning
            } else {
                Icons.Filled.Error
            },
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(26.dp),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = PlayfairDisplay,
                fontSize = 18.sp,
                lineHeight = 26.sp,
            ),
            color = Color.Black,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DeleteAccountScreenPreview() {
    GaplyTheme {
        DeleteAccountScreen(onBack = {}, onDeleted = {})
    }
}
