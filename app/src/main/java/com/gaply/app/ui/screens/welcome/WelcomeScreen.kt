package com.gaply.app.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.R
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandGreen)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_gaply),
            contentDescription = "Logo de Gaply",
            modifier = Modifier.size(180.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Encuentra algo que hacer entre tus clases",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(MaterialTheme.shapes.large)
                .background(color = Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "\uD83D\uDCDA", fontSize = 44.sp)
                    Text(text = "\uD83C\uDF93", fontSize = 56.sp)
                    Text(text = "\u2615", fontSize = 44.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Estudia, conecta y aprovecha tus huecos libres",
                    style = MaterialTheme.typography.bodySmall,
                    color = Mint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        GaplyPrimaryButton(
            text = "Inicia Sesi\u00f3n",
            onClick = onLogin,
            containerColor = Color.White,
            contentColor = BrandGreen,
        )

        Spacer(modifier = Modifier.height(12.dp))

        GaplyOutlineButton(
            text = "Crear Cuenta",
            onClick = onRegister,
            borderColor = Color.White,
            contentColor = Color.White,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
