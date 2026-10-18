package com.gaply.app.ui.screens.activities

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.ui.components.GaplyTextField
import com.gaply.app.ui.screens.register.RegisterData
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/**
 * Módulo de Actividades · Formulario "Nueva actividad" / "Editar actividad".
 *
 * Flujo: título → categoría → foto del lugar → ubicación → duración →
 * fecha/hora → descripción → "Publicar actividad", que guarda en el
 * [ActivityViewModel] compartido y navega al listado limpiando esta
 * pantalla de la pila.
 *
 * Con [activityId] en modo edición se precargan los datos actuales y el
 * botón pasa a "Guardar cambios", llamando a [ActivityViewModel.updateActivity].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivityScreen(
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onPublished: () -> Unit,
    activityId: String = "",
) {
    val viewModel = rememberActivityViewModel()
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    // En modo edición se precarga la actividad existente (una sola vez).
    val existing = remember(activityId) {
        if (activityId.isBlank()) null else viewModel.findById(activityId)
    }
    val isEditing = existing != null

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var category by remember {
        mutableStateOf(existing?.category ?: RegisterData.interests.first())
    }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var duration by remember { mutableStateOf(existing?.duration ?: ActivityFormat.DUR_1H) }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(existing?.imageUri?.let(Uri::parse)) }

    // Fecha + hora inicial: la actividad existente, o hoy a las 12:00.
    var dateMillis by remember {
        mutableStateOf(
            existing?.date?.toLongOrNull() ?: ActivityFormat.hoyAMediodia(),
        )
    }
    var startHour by remember { mutableStateOf(existing?.startTime ?: "12:00") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val timeRange = ActivityFormat.rangoHora(startHour, ActivityFormat.duracionMinutos(duration))

    // Selector de foto del lugar desde la galería (Photo Picker).
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    val canPublish = title.isNotBlank() && location.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // ---------------------------------------------------------- cabecera
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = s.closeCd,
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp),
                )
            }
            Text(
                text = if (isEditing) s.editActivityTitle else s.newActivityTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
            )
            ProfileAvatar(
                photoUrl = state.user?.profilePictureUrl.orEmpty(),
                fallbackLetter = state.user?.firstName?.firstOrNull()?.uppercaseChar()?.toString()
                    ?: state.user?.username?.firstOrNull()?.uppercaseChar()?.toString()
                    ?: "U",
                onClick = onOpenProfile,
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            FormLabel(text = s.eventTitleLabel)
            GaplyTextField(
                value = title,
                onValueChange = { title = it },
                label = s.eventTitleLabel,
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(20.dp))

            FormLabel(text = s.categoryLabel)
            CategoryDropdown(
                selected = category,
                onSelect = { category = it },
            )

            Spacer(modifier = Modifier.height(20.dp))

            FormLabel(text = s.activityPhotoLabel)
            PhotoPickerField(
                imageUri = imageUri,
                onPick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onClear = { imageUri = null },
            )

            Spacer(modifier = Modifier.height(20.dp))

            FormLabel(text = s.locationPlaceLabel)
            GaplyTextField(
                value = location,
                onValueChange = { location = it },
                label = s.locationPlacePlaceholder,
            )

            Spacer(modifier = Modifier.height(20.dp))

            FormLabel(text = s.durationLabel)
            DurationSegmented(
                selected = duration,
                onSelect = { duration = it },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateTimeCard(
                    icon = Icons.Filled.CalendarMonth,
                    title = s.dateLabel,
                    value = ActivityFormat.fechaCorta(dateMillis, s.javaLocale),
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                )
                DateTimeCard(
                    icon = Icons.Filled.Schedule,
                    title = s.timeSectionLabel,
                    value = timeRange,
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            FormLabel(text = s.descriptionOptionalLabel)
            GaplyTextField(
                value = description,
                onValueChange = { description = it },
                label = s.activityDescriptionPlaceholder,
                singleLine = false,
                minLines = 4,
                maxLines = 6,
                imeAction = androidx.compose.ui.text.input.ImeAction.Default,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val result = ActivityItem(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        category = category,
                        duration = duration,
                        date = dateMillis.toString(),
                        timeRange = timeRange,
                        location = location.trim(),
                        description = description.trim(),
                        imageUri = imageUri?.toString(),
                        defaultColorHex = existing?.defaultColorHex
                            ?: ActivityItem.DEFAULT_COLOR_HEX,
                        organizerInitial = existing?.organizerInitial
                            ?: viewModel.organizerInitial(),
                        attendees = existing?.attendees.orEmpty(),
                        isJoined = existing?.isJoined ?: false,
                        startTime = startHour,
                    )
                    if (isEditing) {
                        viewModel.updateActivity(result)
                    } else {
                        viewModel.publish(result)
                    }
                    onPublished()
                },
                enabled = canPublish,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = SurfaceWhite,
                    disabledContainerColor = Sage,
                    disabledContentColor = SurfaceWhite,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = if (isEditing) s.saveChanges else s.publishActivity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ------------------------------------------------------ selectores
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { picked ->
                            // El picker devuelve UTC; lo anclamos a la hora local actual.
                            val local = Instant.ofEpochMilli(picked)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                            dateMillis = local
                        }
                        showDatePicker = false
                    },
                ) { Text(s.accept, color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(s.cancel, color = BrandGreen)
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                colors = androidx.compose.material3.DatePickerDefaults.colors(
                    selectedDayContainerColor = BrandGreen,
                    todayDateBorderColor = BrandGreen,
                ),
            )
        }
    }

    if (showTimePicker) {
        val parts = startHour.split(":")
        val timePickerState = rememberTimePickerState(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 12,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            // false = reloj de 12 horas con selector AM / PM (mismo formato que el
            // selector de hora del formulario de horario de la app).
            is24Hour = false,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = s.selectTimeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            selectorColor = BrandGreen,
                            containerColor = SurfaceWhite,
                            timeSelectorSelectedContainerColor = Mint,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        startHour = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text(s.ok, color = BrandGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(s.cancel, color = BrandGreen)
                }
            },
        )
    }
}

// ===================================================================
// PIEZAS DEL FORMULARIO
// ===================================================================

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

/** Avatar circular de la cabecera: foto de perfil o avatar por defecto. */
@Composable
private fun ProfileAvatar(
    photoUrl: String,
    fallbackLetter: String,
    onClick: () -> Unit,
) {
    val s = LocalStrings.current
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Mint)
            .border(2.dp, BrandGreen, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = s.profilePhotoCd,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = s.tabProfile,
                tint = BrandGreen,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** Desplegable de categoría con chip verde (los 9 intereses de la app). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val s = LocalStrings.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Sage, RoundedCornerShape(10.dp))
                .background(SurfaceWhite)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(BrandGreen)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        // Se muestra la etiqueta localizada; se guarda el valor canónico.
                        text = s.interestLabel(selected),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = s.expandCategoriesCd,
                    tint = TextSecondary,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
        ) {
            RegisterData.interests.forEach { interest ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = s.interestLabel(interest),
                            fontWeight = if (interest == selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(interest)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Campo interactivo para elegir la foto del lugar desde la galería. */
@Composable
private fun PhotoPickerField(
    imageUri: Uri?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    val s = LocalStrings.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, Sage, RoundedCornerShape(16.dp))
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = s.placePhotoCd,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .clickable(onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = s.removePhotoCd,
                    tint = BrandGreen,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Mint),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = BrandGreen,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.tapToAddPlacePhoto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = s.defaultColorPhotoHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

/** Selector segmentado de duración: 30 min · 1 hora · 2 horas.
 *  El valor guardado es el canónico (en ES); la etiqueta se localiza. */
@Composable
private fun DurationSegmented(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val s = LocalStrings.current
    val options = listOf(
        ActivityFormat.DUR_30 to s.duration30,
        ActivityFormat.DUR_1H to s.duration1h,
        ActivityFormat.DUR_2H to s.duration2h,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Sage, RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, (canonical, label) ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Sage),
                )
            }
            val isSelected = canonical == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Mint else Color.Transparent)
                    .clickable { onSelect(canonical) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) BrandGreen else Color.Black,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

/** Tarjeta interactiva de Fecha / Hora (abre los selectores nativos). */
@Composable
private fun DateTimeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Sage, RoundedCornerShape(14.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BrandGreen,
            )
        }
    }
}
