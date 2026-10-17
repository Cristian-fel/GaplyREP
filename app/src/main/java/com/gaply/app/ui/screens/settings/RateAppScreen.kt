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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreenDark
import com.gaply.app.ui.theme.GaplyTheme
import com.gaply.app.ui.theme.PlayfairDisplay
import com.gaply.app.ui.theme.StarGold
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

/**
 * Módulo 2 · Calificar la app.
 * Cinco estrellas, campo opcional de motivos y botón "Enviar";
 * al enviar se muestra el modal de agradecimiento.
 */
@Composable
fun RateAppScreen(
    onBack: () -> Unit,
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

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
            text = "Califica esta aplicación",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
            ),
            color = BrandGreenDark,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu opinión nos ayuda a mejorar",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ---------------------------------------------------------- Estrellas
        Row(verticalAlignment = Alignment.CenterVertically) {
            for (star in 1..5) {
                IconButton(onClick = { rating = star }) {
                    Icon(
                        imageVector = if (star <= rating) {
                            Icons.Filled.Star
                        } else {
                            Icons.Outlined.StarBorder
                        },
                        contentDescription = "Estrella $star",
                        tint = StarGold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --------------------------------------------------------- Motivos
        Text(
            text = "Motivos (opcional):",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
            ),
            color = BrandGreenDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = {
                Text(
                    text = "Cuéntanos qué te gustó o qué\npodemos mejorar...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                focusedBorderColor = BrandGreenDark,
                unfocusedBorderColor = BrandGreenDark,
            ),
        )

        Spacer(modifier = Modifier.height(32.dp))

        GaplyPrimaryButton(
            text = "Enviar",
            onClick = { sent = true },
            containerColor = BrandGreenDark,
            contentColor = Color.White,
            shape = RoundedCornerShape(50),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (sent) {
        AppDialog(
            style = DialogStyle.SUCCESS,
            title = "¡Gracias por tu opinión!",
            message = if (rating > 0) {
                "Nos alegra que hayas calificado Gaply con $rating ${if (rating == 1) "estrella" else "estrellas"}."
            } else {
                "Leeremos tus comentarios para seguir mejorando."
            },
            onDismiss = {
                sent = false
                onBack()
            },
            showConfirmButton = false,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RateAppScreenPreview() {
    GaplyTheme {
        RateAppScreen(onBack = {})
    }
}
