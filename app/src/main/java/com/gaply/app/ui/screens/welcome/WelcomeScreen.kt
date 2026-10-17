package com.gaply.app.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.R
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.SuccessGreen
import com.gaply.app.ui.theme.SurfaceWhite

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {},
    accountMessage: String? = null,
) {
    var showAccountDialog by remember { mutableStateOf(!accountMessage.isNullOrBlank()) }
    val deleted = accountMessage == ACCOUNT_DELETED
    // Matriz de color para convertir la imagen negra original en líneas gris claro muy suave
    val invertToLightGrayMatrix = ColorMatrix(
        floatArrayOf(
            -1f,  0f,  0f, 0f, 245f, // Red: Invierte el negro a tenue gris casi blanco (245)
            0f, -1f,  0f, 0f, 245f, // Green
            0f,  0f, -1f, 0f, 245f, // Blue
            0f,  0f,  0f, 1f,   0f  // Conserva el canal Alpha transparente
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Marca de agua del edificio en la parte inferior (altura reducida para no colisionar con la ilustración)
        Image(
            painter = painterResource(id = R.drawable.bg_building_bottom),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .alpha(0.16f) // ajusta este valor (0f a 1f) según qué tan "desvanecida" la quieras
                .offset(y = 245.dp),   // súbelo o bájalo hasta que toque el borde
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo horizontal de Gaply
            Image(
                painter = painterResource(id = R.drawable.logo_gaply),
                contentDescription = "Logo de Gaply",
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(80.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Texto descriptivo
            Text(
                text = "Encuentra algo que hacer entre tus clases",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = BrandGreen,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ilustración principal
            Image(
                painter = painterResource(id = R.drawable.ilustration_main),
                contentDescription = "Ilustración principal",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Inicia Sesión
            GaplyPrimaryButton(
                text = "Inicia Sesión",
                onClick = onLogin,
                containerColor = BrandGreen,
                contentColor = Color.White,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Crear Cuenta
            GaplyOutlineButton(
                text = "Crear Cuenta",
                onClick = onRegister,
                borderColor = BrandGreen,
                contentColor = BrandGreen,
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showAccountDialog && accountMessage != null) {
        val isDeleted = accountMessage == ACCOUNT_DELETED
        AppDialog(
            style = if (isDeleted) DialogStyle.ERROR else DialogStyle.SUCCESS,
            title = if (isDeleted) "¡Cuenta eliminada!" else "¡Cuenta desactivada!",
            message = if (isDeleted) {
                "Tu cuenta y datos se han eliminado correctamente."
            } else {
                "Puedes reactivarla cuando quieras con tus mismos datos de acceso."
            },
            onDismiss = { showAccountDialog = false },
            showConfirmButton = false,
            icon = if (isDeleted) Icons.Filled.Delete else Icons.Filled.CheckCircle,
            iconTint = if (isDeleted) SurfaceWhite else SuccessGreen,
            iconBackgroundColor = if (isDeleted) ErrorRed else null,
        )
    }
}

/** Valores posibles del argumento accountMessage de Welcome. */
const val ACCOUNT_DELETED = "deleted"
const val ACCOUNT_DEACTIVATED = "deactivated"

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen()
    }
}