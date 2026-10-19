package com.gaply.app.ui.screens.activities

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary
import java.time.ZoneId

/**
 * Módulo de Actividades · Detalle de la actividad ("Actividades").
 *
 * Renderiza los datos reales de la actividad seleccionada (foto o bloque
 * de color, título, fecha/hora, ubicación y descripción), el cartel de
 * "próximamente" para Chat y Cómo llegar, organizador/asistentes y el
 * botón "Unirme" / "Ya está dentro".
 *
 * Si la actividad fue creada por el usuario actual ([ActivityItem.isCreatedByMe])
 * se muestran además las acciones Editar y Cancelar / Eliminar.
 */
@Composable
fun ActivityDetailScreen(
    activityId: String,
    onBack: () -> Unit,
    onEditActivity: (String) -> Unit = {},
    onDeleted: () -> Unit = {},
) {
    val viewModel = rememberActivityViewModel()
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    val activity = state.activities.find { it.id == activityId }

    var showComingSoon by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ActivitiesTopSection(
            title = s.activitiesDetailTitle,
            // Banner dinámico: el mismo hueco libre real que muestra el Home.
            huecoTexto = huecoBannerDeHoy(),
            onBack = onBack,
        )

        if (activity == null) {
            EmptyActivitiesState(message = s.activityUnavailableMsg)
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Foto del lugar (o bloque de color sólido) con bordes redondeados.
            ActivityImage(
                imageUri = activity.imageUri,
                defaultColorHex = activity.defaultColorHex,
                contentDescription = activity.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp)),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = activity.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fecha y hora con ícono de calendario.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = fechaLarga(activity.date, s.javaLocale),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                    Text(
                        text = activity.timeRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = Sage)

            Spacer(modifier = Modifier.height(14.dp))

            // Ubicación con ícono de pin.
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = activity.location,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                )
            }

            if (activity.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recuadro verde claro: Chat del evento + Cómo llegar (próximamente).
            ComingSoonCard(onClick = { showComingSoon = true })

            Spacer(modifier = Modifier.height(16.dp))

            // Organizador y asistentes.
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = s.organizerLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InitialAvatar(
                        initial = activity.organizerInitial,
                        // Foto real de perfil (users/{uid}); la inicial es respaldo.
                        photoUrl = state.fotos[activity.ownerId],
                        sizeDp = 40,
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .width(1.dp)
                        .height(56.dp)
                        .background(Sage),
                )

                Column {
                    Text(
                        text = s.attendeesCount(activity.attendees.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AttendeeStack(
                        attendees = activity.attendees,
                        attendeeIds = activity.attendeeIds,
                        fotos = state.fotos,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Acciones de actividad propia: Editar y Cancelar / Eliminar.
            if (activity.isCreatedByMe) {
                OwnerActionsCard(
                    onEdit = { onEditActivity(activity.id) },
                    onDelete = { showDeleteConfirm = true },
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón Unirme / Ya está dentro (estando dentro, al tocarlo pregunta si salir).
            Button(
                onClick = {
                    if (activity.isJoined) showLeaveConfirm = true
                    else viewModel.join(activity.id)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = SurfaceWhite,
                    disabledContainerColor = BrandGreen,
                    disabledContentColor = SurfaceWhite,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (activity.isJoined) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.alreadyJoined,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = s.joinActivity,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showComingSoon) {
        AppDialog(
            style = DialogStyle.INFO,
            title = s.comingSoonTitle,
            message = s.comingSoonMsg,
            confirmText = s.close,
            onDismiss = { showComingSoon = false },
        )
    }

    // Confirmación de salir de una actividad a la que se unió el usuario.
    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = SurfaceWhite,
            title = {
                Text(
                    text = s.leaveActivityTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
            },
            text = {
                Text(
                    text = s.leaveActivityConfirmMsg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.leave(activityId)
                        showLeaveConfirm = false
                    },
                ) {
                    Text(s.confirmLeaveActivity, color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) {
                    Text(s.back, color = BrandGreen)
                }
            },
        )
    }

    // Confirmación de cancelación / eliminación de la actividad propia.
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = SurfaceWhite,
            title = {
                Text(
                    text = s.cancelActivityTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
            },
            text = {
                Text(
                    text = s.cancelActivityConfirmMsg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteActivity(activityId)
                        showDeleteConfirm = false
                        onDeleted()
                    },
                ) {
                    Text(s.confirmCancelActivity, color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(s.back, color = BrandGreen)
                }
            },
        )
    }
}

/** Fecha en milisegundos (texto) u "yyyy-MM-dd" -> "Sábado, 18 de octubre de 2026" (según idioma). */
private fun fechaLarga(date: String, locale: java.util.Locale): String {
    val millis = date.toLongOrNull()
        ?: runCatching {
            java.time.LocalDate.parse(date)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    return millis?.let { ActivityFormat.fechaLarga(it, locale) } ?: date
}

/** Recuadro verde claro con "Chat del evento" y "Cómo llegar" (próximamente). */
@Composable
private fun ComingSoonCard(onClick: () -> Unit) {
    val s = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Mint)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ) {
            Text(
                text = s.eventChat,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = s.eventChat,
                tint = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BrandGreen.copy(alpha = 0.35f))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = s.locationLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = SurfaceWhite,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp,
                    vertical = 6.dp,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = s.howToGetThere,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

/**
 * Círculos superpuestos de los asistentes: foto de perfil real de cada uno
 * (misma fuente que Home y Perfil) y la inicial como respaldo si no hay foto.
 */
@Composable
private fun AttendeeStack(
    attendees: List<String>,
    attendeeIds: List<String>,
    fotos: Map<String, String>,
) {
    if (attendees.isEmpty()) {
        val s = LocalStrings.current
        Text(
            text = s.beFirstToJoin,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        return
    }

    val palette = listOf(BrandGreen, Mint, Sage)
    Row {
        attendees.take(5).forEachIndexed { index, attendee ->
            val photoUrl = fotos[attendeeIds.getOrNull(index)]
            InitialAvatar(
                initial = attendee.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                photoUrl = photoUrl,
                sizeDp = 34,
                backgroundColor = palette[index % palette.size],
                textColor = if (palette[index % palette.size] == Mint) BrandGreen else SurfaceWhite,
                modifier = Modifier.offset(x = if (index == 0) 0.dp else (-10).dp),
            )
        }
    }
}

/** Acciones de actividad propia en el detalle: Editar y Cancelar / Eliminar. */
@Composable
private fun OwnerActionsCard(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val s = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Mint.copy(alpha = 0.5f))
            .padding(12.dp),
    ) {
        Text(
            text = s.manageYourActivity,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = BrandGreen,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onEdit,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, BrandGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen),
                modifier = Modifier.weight(1f).height(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(s.edit, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onDelete,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, ErrorRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                modifier = Modifier.weight(1f).height(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(s.cancel, fontWeight = FontWeight.Bold)
            }
        }
    }
}
