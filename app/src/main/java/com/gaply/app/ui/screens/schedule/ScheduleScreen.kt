package com.gaply.app.ui.screens.schedule

import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.domain.model.FiltroFranja
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.ScheduleClass
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.SurfaceWhite

/**
 * Módulo 2B · "Mi horario" (pantalla 1 del Figma).
 *
 * Muestra las clases del día en bandas de color, los huecos libres entre
 * clases y el estado vacío con "Importar desde galería".
 */
@Composable
fun ScheduleScreen(
    onBack: () -> Unit,
    onAddClass: (String) -> Unit,
    onEditClass: (String) -> Unit,
    onOpenActivities: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ScheduleViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ScheduleViewModel.factory(context.appContainer()),
    )
    val s = LocalStrings.current
    viewModel.strings = s
    val state by viewModel.uiState.collectAsState()

    // Recarga cada vez que la pantalla vuelve a mostrarse: al regresar del
    // formulario con una clase creada, editada o eliminada, la lista debe
    // reflejar el cambio (antes quedaba desactualizada y confundía).
    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    var mostrarAvisoImport by remember { mutableStateOf(false) }
    val galeria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) mostrarAvisoImport = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 24.dp, bottom = 8.dp),
            ) {
                BotonVolver(onBack = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = s.mySchedule,
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandGreen,
                )
            }

            when {
                state.cargando -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = BrandGreen)
                }

                state.error != null -> EstadoError(
                    mensaje = when (state.error) {
                        ScheduleError.CONNECTION -> s.noConnectionSchedule
                        ScheduleError.PERMISSION -> s.firestoreReadSchedule
                        ScheduleError.LOAD_FAILED -> s.loadScheduleFailed
                        null -> ""
                    },
                    onReintentar = viewModel::cargar,
                )

                else -> ContenidoHorario(
                    state = state,
                    onDiaAnterior = { viewModel.cambiarDia(-1) },
                    onDiaSiguiente = { viewModel.cambiarDia(+1) },
                    onAddClass = onAddClass,
                    onEditClass = onEditClass,
                    onVerActividadesDeHueco = { hueco ->
                        // "Ver actividades para este hueco": filtra el
                        // listado por ese día y rango y lo abre.
                        val container = context.appContainer()
                        container.filtroFranja.value =
                            FiltroFranja(dia = state.fechaVisible, hueco = hueco)
                        container.filtroActividades.value = null
                        onOpenActivities()
                    },
                    onImportar = { galeria.launch("image/*") },
                )
            }
        }

        FloatingActionButton(
            onClick = { onAddClass(state.dia) },
            containerColor = BrandGreen,
            contentColor = SurfaceWhite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = s.addScheduleCd)
        }
    }

    if (mostrarAvisoImport) {
        AppDialog(
            style = DialogStyle.INFO,
            title = s.importFromGallery,
            message = s.importGalleryMsg,
            onDismiss = { mostrarAvisoImport = false },
        )
    }
}

// ===================================================================
// CONTENIDO
// ===================================================================

@Composable
private fun ContenidoHorario(
    state: ScheduleUiState,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onAddClass: (String) -> Unit,
    onEditClass: (String) -> Unit,
    onVerActividadesDeHueco: (HuecoLibre) -> Unit,
    onImportar: () -> Unit,
) {
    val s = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = state.mesCorto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        // Encabezado del día: flechas ‹ › para recorrer la semana y mirar todo
        // el horario sin salir de la pantalla (el Figma no las trae).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            IconButton(onClick = onDiaAnterior) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = s.prevDay,
                    tint = BrandGreen,
                )
            }
            Text(
                text = s.dayName(state.dia),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BrandGreen,
            )
            IconButton(onClick = onDiaSiguiente) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = s.nextDay,
                    tint = BrandGreen,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = state.fecha,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 12.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.clases.isEmpty()) {
            EstadoVacio(
                hayClasesEnOtrosDias = state.hayClasesOtrosDias,
                onImportar = onImportar,
            )
        } else {
            state.clases.forEach { clase ->
                ClaseBand(
                    clase = clase,
                    onEdit = { onEditClass(clase.id) },
                )

                state.huecos.find { it.inicioMinutos == clase.endMinutes }?.let { hueco ->
                    HuecoBand(
                        hueco = hueco,
                        onVerActividades = { onVerActividadesDeHueco(hueco) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

/** Banda de color con una clase: "Desarrollo de aplicaciones · 10:00 a.m. - 12:00 p.m." */
@Composable
private fun ClaseBand(
    clase: ScheduleClass,
    onEdit: () -> Unit,
) {
    val s = LocalStrings.current
    val color = remember(clase.color) { colorSeguro(clase.color) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clase.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = s.formatTimeRange(clase.startHour, clase.endHour),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                if (clase.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = s.notePrefix(clase.note),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black.copy(alpha = 0.75f),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.55f))
                    .clickable(onClick = onEdit)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = s.edit,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }
            }
        }
    }
}

/** Banda verde oscura del hueco: "2 horas libres · Ver actividades para este hueco". */
@Composable
private fun HuecoBand(
    hueco: HuecoLibre,
    onVerActividades: () -> Unit,
) {
    val s = LocalStrings.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandGreen)
            .clickable { onVerActividades() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Column {
            Text(
                text = s.freeDuration(hueco.duracionMinutos),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SurfaceWhite,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = s.viewActivitiesForSlot,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SurfaceWhite,
            )
        }
    }
}

/** Estado vacío: calendario, "Agregar Horario" e "Importar desde galería". */
@Composable
private fun EstadoVacio(
    hayClasesEnOtrosDias: Boolean,
    onImportar: () -> Unit,
) {
    val s = LocalStrings.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = BrandGreen,
            modifier = Modifier.size(96.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = s.addScheduleTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = s.tapPlusToAdd,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (hayClasesEnOtrosDias) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = s.dayFreeUseArrows,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = BrandGreen,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Mint)
                .clickable(onClick = onImportar)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = s.importFromGallery,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = BrandGreen,
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun EstadoError(
    mensaje: String,
    onReintentar: () -> Unit,
) {
    val s = LocalStrings.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = s.tapToRetry,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = BrandGreen,
            modifier = Modifier.clickable(onClick = onReintentar),
        )
    }
}

/** Botón de volver en círculo blanco con sombra (estilo del Figma). */
@Composable
private fun BotonVolver(onBack: () -> Unit) {
    val s = LocalStrings.current

    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .background(SurfaceWhite, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = s.back,
                tint = BrandGreen,
            )
        }
    }
}

/** Color parseado con seguridad: si el hex de Firestore es inválido se usa el verde de marca. */
private fun colorSeguro(hex: String): Color = try {
    Color(AndroidColor.parseColor(hex))
} catch (e: IllegalArgumentException) {
    BrandGreen
}
