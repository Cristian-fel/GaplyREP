package com.gaply.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.R
import com.gaply.app.core.i18n.AppLanguage
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreenDark
import com.gaply.app.ui.theme.GaplyTheme
import com.gaply.app.ui.theme.OutlineGray
import com.gaply.app.ui.theme.PlayfairDisplay
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextPrimary

/**
 * Módulo 2 · Idioma.
 * Lista de idiomas con la bandera de cada uno a la izquierda y radio a la derecha;
 * el seleccionado muestra la marca verde. Se confirma con
 * "Guardar idioma".
 */

/** Bandera (vectorial) asociada a cada idioma. */
private fun AppLanguage.flagRes(): Int = when (this) {
    AppLanguage.ES -> R.drawable.flag_es
    AppLanguage.EN -> R.drawable.flag_en
    AppLanguage.FR -> R.drawable.flag_fr
    AppLanguage.DE -> R.drawable.flag_de
    AppLanguage.IT -> R.drawable.flag_it
    AppLanguage.PT -> R.drawable.flag_br
    AppLanguage.JA -> R.drawable.flag_ja
    AppLanguage.ZH -> R.drawable.flag_zh
}
@Composable
fun LanguageScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: LanguageViewModel = viewModel(
        factory = LanguageViewModel.factory(context.appContainer()),
    )
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

        SettingsTopBar(
            title = s.language,
            onBack = onBack,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, OutlineGray),
        ) {
            viewModel.languages.forEachIndexed { index, language ->
                if (index > 0) HorizontalDivider(color = OutlineGray)
                LanguageRow(
                    language = language.label,
                    flagRes = language.flagRes(),
                    selected = language == viewModel.selected,
                    onClick = { viewModel.select(language) },
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        GaplyPrimaryButton(
            text = s.saveLanguage,
            onClick = {
                viewModel.save()
                onBack()
            },
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Fila: bandera del idioma, nombre y radio (verde con check si está activo). */
@Composable
private fun LanguageRow(
    language: String,
    flagRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = flagRes),
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, OutlineGray, RoundedCornerShape(8.dp)),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = language,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Normal,
            ),
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        // Radio de selección
        if (selected) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(BrandGreenDark),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = LocalStrings.current.selected,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, OutlineGray, CircleShape),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LanguageScreenPreview() {
    GaplyTheme {
        LanguageScreen(onBack = {})
    }
}
