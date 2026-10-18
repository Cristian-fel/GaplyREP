package com.gaply.app.ui.screens.activities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary
import java.time.LocalDate

/**
 * Módulo de Actividades · Listado "Actividades para tu hueco".
 *
 * Muestra el banner del hueco libre, el subtítulo "Según tus intereses"
 * y las actividades publicadas en el [ActivityViewModel] compartido.
 * Con la lista vacía muestra un estado vacío centrado.
 */
@Composable
fun ActivitiesListScreen(
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit,
) {
    val viewModel = rememberActivityViewModel()
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    // Error de guardado/borrado en el repositorio (p. ej. permisos de Firestore):
    // se muestra aquí porque es la pantalla de aterrizaje del módulo.
    state.error?.let { error ->
        AppDialog(
            style = DialogStyle.ERROR,
            title = s.saveErrorTitle,
            message = error,
            onDismiss = viewModel::clearError,
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ActivitiesTopSection(
            title = s.activitiesListTitle,
            // Banner dinámico: el mismo hueco libre real que muestra el Home.
            huecoTexto = huecoBannerDeHoy(),
            onBack = onBack,
        )

        InterestsSubtitle()

        // Solo las actividades de HOY: al cambiar el día en el dispositivo,
        // las de otras fechas dejan de mostrarse en este listado.
        val actividadesDeHoy = state.activities.filter {
            ActivityFormat.esDeFecha(it.date, LocalDate.now())
        }

        if (actividadesDeHoy.isEmpty()) {
            EmptyActivitiesState(
                message = s.activitiesEmptyListMsg,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(actividadesDeHoy, key = { it.id }) { activity ->
                    ActivityCard(
                        activity = activity,
                        onClick = { onOpenActivity(activity.id) },
                    )
                }
            }
        }
    }
}

/** Tarjeta redondeada del listado: imagen, chip de categoría y datos. */
@Composable
private fun ActivityCard(
    activity: ActivityItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                ActivityImage(
                    imageUri = activity.imageUri,
                    defaultColorHex = activity.defaultColorHex,
                    contentDescription = activity.title,
                    modifier = Modifier.fillMaxSize(),
                )

                // Etiqueta verde de categoría arriba a la izquierda.
                CategoryPill(
                    text = activity.category,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activity.timeRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activity.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}
