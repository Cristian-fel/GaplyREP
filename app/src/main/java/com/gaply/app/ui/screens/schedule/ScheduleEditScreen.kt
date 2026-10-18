package com.gaply.app.ui.screens.schedule

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.domain.model.ScheduleClass
import com.gaply.app.ui.components.GaplyTextField
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Módulo 2B · formulario "Horario" (pantalla 2 del Figma).
 *
 * Crea o edita una clase: título, día, fechas, horas, nota y color. El botón
 * ✓ guarda y el ✕ regresa sin guardar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    classId: String,
    diaInicial: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ScheduleEditViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ScheduleEditViewModel.factory(context.appContainer()),
    )
    val s = LocalStrings.current
    val state by viewModel.uiState.collectAsState()

    var mostrarDia by remember { mutableStateOf(false) }
    var mostrarFechaInicio by remember { mutableStateOf(false) }
    var mostrarFechaFin by remember { mutableStateOf(false) }
    var mostrarHoraInicio by remember { mutableStateOf(false) }
    var mostrarHoraFin by remember { mutableStateOf(false) }
    var mostrarColor by remember { mutableStateOf(false) }
    var mostrarEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargar(classId = classId, diaPorDefecto = diaInicial)
    }

    LaunchedEffect(state.guardado) {
        if (state.guardado) onSaved()
    }

    LaunchedEffect(state.eliminado) {
        if (state.eliminado) onSaved()
    }

    if (mostrarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarEliminar = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = s.deleteClassTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            text = {
                Text(
                    text = s.deleteClassMessage(state.titulo.ifBlank { s.thisClass }),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarEliminar = false
                    viewModel.eliminar()
                }) {
                    Text(text = s.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEliminar = false }) {
                    Text(text = s.cancel, color = BrandGreen)
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = s.closeWithoutSaving,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = s.scheduleFormTitle,
                style = MaterialTheme.typography.titleLarge,
                color = BrandGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            if (!state.esNuevo) {
                IconButton(
                    onClick = { mostrarEliminar = true },
                    enabled = !state.guardando && !state.eliminado,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = s.deleteClassTitle,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            IconButton(
                onClick = viewModel::guardar,
                enabled = !state.guardando && !state.eliminado,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = s.save,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (state.cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = BrandGreen)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            state.error?.let { error ->
                Text(
                    text = when (error) {
                        ScheduleEditError.CONNECTION -> s.noConnectionSchedule
                        ScheduleEditError.READ_PERMISSION -> s.firestoreReadSchedule
                        ScheduleEditError.WRITE_PERMISSION -> s.firestoreWriteRules
                        ScheduleEditError.DELETE_PERMISSION -> s.firestoreDeleteRules
                        ScheduleEditError.LOAD_FAILED -> s.loadScheduleFailed
                        ScheduleEditError.LOAD_GENERIC -> s.loadGenericFailed
                        ScheduleEditError.SAVE_GENERIC -> s.saveGenericFailed
                        ScheduleEditError.DELETE_GENERIC -> s.deleteGenericFailed
                        ScheduleEditError.TITLE_REQUIRED -> s.writeTitleError
                        ScheduleEditError.END_BEFORE_START -> s.endMustBeAfterStart
                        ScheduleEditError.CLASS_MISSING -> s.classNoLongerExists
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            Text(
                text = s.titleLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            GaplyTextField(
                value = state.titulo,
                onValueChange = viewModel::onTitulo,
                label = "",
                placeholder = s.titlePlaceholder,
            )

            Spacer(modifier = Modifier.height(24.dp))

            TarjetaDeSeleccion {
                FilaDeSeleccion(
                    etiqueta = s.dayLabel,
                    valor = s.dayName(state.dia),
                    onClick = { mostrarDia = true },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                FilaDeSeleccion(
                    etiqueta = s.startLabel,
                    valor = state.fechaInicio,
                    onClick = { mostrarFechaInicio = true },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                FilaDeSeleccion(
                    etiqueta = s.endLabel,
                    valor = state.fechaFin,
                    onClick = { mostrarFechaFin = true },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = s.timeSectionLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            TarjetaDeSeleccion {
                FilaDeSeleccion(
                    etiqueta = s.startsLabel,
                    valor = s.formatTime(state.horaInicio),
                    onClick = { mostrarHoraInicio = true },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                FilaDeSeleccion(
                    etiqueta = s.endsLabel,
                    valor = s.formatTime(state.horaFin),
                    onClick = { mostrarHoraFin = true },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = s.addNotesTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            GaplyTextField(
                value = state.nota,
                onValueChange = viewModel::onNota,
                label = "",
                placeholder = s.notesPlaceholder,
                singleLine = false,
                maxLines = 3,
            )

            Spacer(modifier = Modifier.height(24.dp))

            BotonColor(
                color = state.color,
                onClick = { mostrarColor = true },
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (mostrarDia) {
        DialogoDeDia(
            seleccionado = state.dia,
            onSeleccionar = {
                viewModel.onDia(it)
                mostrarDia = false
            },
            onCerrar = { mostrarDia = false },
        )
    }

    if (mostrarFechaInicio) {
        DialogoDeFecha(
            fecha = state.fechaInicio,
            onAceptar = {
                viewModel.onFechaInicio(it)
                mostrarFechaInicio = false
            },
            onCerrar = { mostrarFechaInicio = false },
        )
    }

    if (mostrarFechaFin) {
        DialogoDeFecha(
            fecha = state.fechaFin,
            onAceptar = {
                viewModel.onFechaFin(it)
                mostrarFechaFin = false
            },
            onCerrar = { mostrarFechaFin = false },
        )
    }

    if (mostrarHoraInicio) {
        DialogoDeHora(
            hora = state.horaInicio,
            onAceptar = {
                viewModel.onHoraInicio(it)
                mostrarHoraInicio = false
            },
            onCerrar = { mostrarHoraInicio = false },
        )
    }

    if (mostrarHoraFin) {
        DialogoDeHora(
            hora = state.horaFin,
            onAceptar = {
                viewModel.onHoraFin(it)
                mostrarHoraFin = false
            },
            onCerrar = { mostrarHoraFin = false },
        )
    }

    if (mostrarColor) {
        DialogoDeColor(
            seleccionado = state.color,
            onSeleccionar = {
                viewModel.onColor(it)
                mostrarColor = false
            },
            onCerrar = { mostrarColor = false },
        )
    }
}

// ===================================================================
// PIEZAS DEL FORMULARIO
// ===================================================================

/** Tarjeta redondeada con borde que agrupa las filas Día / Inicio / Fin. */
@Composable
private fun TarjetaDeSeleccion(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, Sage),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
            content()
        }
    }
}

/** Fila "Día ........... Lunes" (el valor en verde, como en el Figma). */
@Composable
private fun FilaDeSeleccion(
    etiqueta: String,
    valor: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrandGreen,
        )
    }
}

/** Píldora menta "Color" con el color elegido y la flecha. */
@Composable
private fun BotonColor(
    color: String,
    onClick: () -> Unit,
) {
    val s = LocalStrings.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Mint,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = s.colorLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(colorSeguro(color)),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "▶",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ===================================================================
// SELECTORES
// ===================================================================

/** "Seleccione un día": lista centrada con el día elegido resaltado. */
@Composable
private fun DialogoDeDia(
    seleccionado: String,
    onSeleccionar: (String) -> Unit,
    onCerrar: () -> Unit,
) {
    val s = LocalStrings.current

    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = s.selectDay,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ScheduleViewModel.DIAS_ORDEN.forEach { dia ->
                    val elegido = dia == seleccionado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (elegido) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    Color.Transparent
                                },
                            )
                            .clickable { onSeleccionar(dia) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = s.dayName(dia),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (elegido) FontWeight.Bold else FontWeight.Normal,
                            color = if (elegido) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text(text = s.cancel, color = BrandGreen)
            }
        },
    )
}

/** Calendario (DatePicker de Material 3) para Inicio y Fin. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoDeFecha(
    fecha: String,
    onAceptar: (String) -> Unit,
    onCerrar: () -> Unit,
) {
    val s = LocalStrings.current
    val fechaActual = remember(fecha) {
        ScheduleEditViewModel.parsearFecha(fecha) ?: LocalDate.now()
    }
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = fechaActual.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )

    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = estado.selectedDateMillis
                    if (millis != null) {
                        val seleccion = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onAceptar(ScheduleEditViewModel.formatearFecha(seleccion))
                    } else {
                        onCerrar()
                    }
                },
            ) {
                Text(text = s.ok, color = BrandGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text(text = s.cancel, color = BrandGreen)
            }
        },
    ) {
        DatePicker(state = estado)
    }
}

/** Reloj (TimePicker de Material 3) para Comienza y Finaliza. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoDeHora(
    hora: String,
    onAceptar: (String) -> Unit,
    onCerrar: () -> Unit,
) {
    val s = LocalStrings.current
    val partes = hora.split(":")
    val estado = rememberTimePickerState(
        initialHour = partes.getOrNull(0)?.toIntOrNull() ?: 8,
        initialMinute = partes.getOrNull(1)?.toIntOrNull() ?: 0,
    )

    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = s.selectTime,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = estado)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAceptar("%02d:%02d".format(estado.hour, estado.minute)) },
            ) {
                Text(text = s.ok, color = BrandGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text(text = s.cancel, color = BrandGreen)
            }
        },
    )
}

/** Paleta del Figma: 15 colores en 3 columnas. */
@Composable
private fun DialogoDeColor(
    seleccionado: String,
    onSeleccionar: (String) -> Unit,
    onCerrar: () -> Unit,
) {
    val s = LocalStrings.current

    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = s.selectColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ScheduleClass.COLORES.chunked(3).map { fila ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        fila.map { color ->
                            val elegido = color == seleccionado
                            Box(
                                modifier = Modifier
                                    .size(if (elegido) 76.dp else 68.dp)
                                    .clip(CircleShape)
                                    .background(colorSeguro(color))
                                    .clickable { onSeleccionar(color) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (elegido) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = s.selected,
                                        tint = SurfaceWhite,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCerrar,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                ),
            ) {
                Text(text = s.close, color = SurfaceWhite)
            }
        },
    )
}

/** Color parseado con seguridad: si el hex es inválido se usa el verde de marca. */
private fun colorSeguro(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    BrandGreen
}
