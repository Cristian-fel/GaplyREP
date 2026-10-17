package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.R
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.GaplyTheme
import com.gaply.app.ui.theme.OutlineGray
import com.gaply.app.ui.theme.PlayfairDisplay
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

/**
 * Módulo 2 · Acerca de Gaply.
 * Logo de la marca, tarjeta con la misión de la app y pie de versión.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        SettingsTopBar(
            title = "Acerca de Gaply",
            onBack = onBack,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_gaply),
            contentDescription = "Logo de Gaply",
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(120.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, OutlineGray),
        ) {
            Text(
                text = "Gaply transforma la experiencia universitaria " +
                    "al eliminar las barreras de comunicación " +
                    "dentro del campus.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                ),
                color = TextSecondary,
                modifier = Modifier.padding(24.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "© Gaply 2026 - Version 1.0",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
            ),
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AboutScreenPreview() {
    GaplyTheme {
        AboutScreen(onBack = {})
    }
}
