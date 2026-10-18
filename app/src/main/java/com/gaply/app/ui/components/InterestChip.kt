package com.gaply.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite

/**
 * Pastilla de interés con icono.
 * Se usa en el registro, en la pestaña Perfil y en Editar Perfil.
 */
@Composable
fun InterestPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val icon = interestIconFor(label)

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = s.interestLabel(label),
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp,
                maxLines = 1,
            )
        },
        leadingIcon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Mint.copy(alpha = 0.35f),
            labelColor = BrandGreen,
            iconColor = BrandGreen,
            selectedContainerColor = BrandGreen,
            selectedLabelColor = SurfaceWhite,
            selectedLeadingIconColor = SurfaceWhite,
        ),
        border = BorderStroke(
            width = if (selected) 0.dp else 1.dp,
            color = if (selected) Color.Transparent else Sage,
        ),
        modifier = modifier,
    )
}

/** Icono que acompaña a cada interés. */
fun interestIconFor(label: String): ImageVector? {
    val clean = label.lowercase().trim()
    return when {
        clean.contains("deporte") -> Icons.Filled.SportsSoccer
        clean.contains("música") || clean.contains("musica") -> Icons.Filled.MusicNote
        clean.contains("videojuego") || clean.contains("gaming") || clean.contains("juego") ->
            Icons.Filled.SportsEsports

        clean.contains("arte") -> Icons.Filled.Palette
        clean.contains("mesa") || clean.contains("casino") -> Icons.Filled.Casino
        clean.contains("película") || clean.contains("pelicula") || clean.contains("cine") ->
            Icons.Filled.Movie

        clean.contains("estudio") || clean.contains("estudiar") || clean.contains("lectura") ->
            Icons.Filled.School

        clean.contains("evento") || clean.contains("fiesta") -> Icons.Filled.Celebration
        clean.contains("baile") || clean.contains("running") || clean.contains("correr") ->
            Icons.Filled.Nightlife

        clean.contains("fotograf") -> Icons.Filled.CameraAlt
        clean.contains("viaje") -> Icons.Filled.Flight
        clean.contains("cocina") || clean.contains("comida") || clean.contains("café") ||
            clean.contains("cafe") -> Icons.Filled.Restaurant

        clean.contains("programa") || clean.contains("código") || clean.contains("codigo") ->
            Icons.Filled.Code

        clean.contains("ciencia") -> Icons.Filled.Science
        clean.contains("moda") -> Icons.Filled.Checkroom
        else -> Icons.Filled.School
    }
}
