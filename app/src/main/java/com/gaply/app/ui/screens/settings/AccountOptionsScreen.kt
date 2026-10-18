package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.GaplyTheme
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

/**
 * Módulo 2 · FragmentEliminarDesactivar.
 * Elige entre desactivar la cuenta (temporal, reversible) o
 * eliminarla permanentemente (fondo rojo pastel, irreversible).
 */
@Composable
fun AccountOptionsScreen(
    onBack: () -> Unit,
    onDeactivate: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
) {
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = s.deleteOrDeactivateTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = s.deleteOrDeactivateDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ------------------------------------------- Opción 1: Desactivar
        AccountOptionCard(
            title = s.deactivateOptionTitle,
            description = s.deactivateOptionDesc,
            containerColor = SurfaceWhite,
            titleColor = MaterialTheme.colorScheme.onSurface,
            descriptionColor = TextSecondary,
            arrowTint = TextSecondary,
            border = BorderStroke(1.dp, Sage),
            onClick = onDeactivate,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -------------------------------- Opción 2: Eliminar permanentemente
        AccountOptionCard(
            title = s.deleteOptionTitle,
            description = s.deleteOptionDesc,
            containerColor = ErrorRed.copy(alpha = 0.08f),
            titleColor = ErrorRed,
            descriptionColor = TextSecondary,
            arrowTint = ErrorRed,
            border = null,
            onClick = onDeleteAccount,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Tarjeta de opción con título serif, descripción y flecha hacia la derecha. */
@Composable
private fun AccountOptionCard(
    title: String,
    description: String,
    containerColor: androidx.compose.ui.graphics.Color,
    titleColor: androidx.compose.ui.graphics.Color,
    descriptionColor: androidx.compose.ui.graphics.Color,
    arrowTint: androidx.compose.ui.graphics.Color,
    border: BorderStroke?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = descriptionColor,
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = arrowTint,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AccountOptionsScreenPreview() {
    GaplyTheme {
        AccountOptionsScreen(onBack = {})
    }
}
