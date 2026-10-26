package com.gaply.app.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.ChatType
import com.gaply.app.domain.model.Message
import com.gaply.app.domain.model.MessageKind
import com.gaply.app.domain.model.User
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.InterestPill
import com.gaply.app.ui.mensajeDe
import com.gaply.app.ui.screens.activities.ActivityFormat
import com.gaply.app.ui.screens.activities.InitialAvatar
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.OutlineGray
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextPrimary
import com.gaply.app.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Módulo 5 · Conversación (`Routes.CHAT_DETAIL`).
 *
 * Top bar con la foto y el nombre del otro usuario (directo) o de la
 * actividad y su estado (grupo); burbujas de texto con el nombre del emisor
 * encima cuando cambia (solo en grupo); campo de entrada inferior con botón
 * verde de enviar y auto-scroll al último mensaje.
 *
 * Alcance de este corte: solo texto, sin imágenes ni presencia.
 */
@Composable
fun ChatDetailScreen(
    chatId: String,
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: ChatDetailViewModel = viewModel(
        factory = ChatDetailViewModel.factory(context.appContainer(), chatId),
    )
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    var texto by remember { mutableStateOf("") }
    val lista = rememberLazyListState()

    // Textos activos (nombre del emisor cuando el perfil no tiene nombre).
    LaunchedEffect(s) { viewModel.strings = s }

    // Siempre al final: al abrir y cuando entra un mensaje nuevo.
    LaunchedEffect(state.mensajes.size) {
        if (state.mensajes.isNotEmpty()) {
            lista.animateScrollToItem(state.mensajes.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
            .systemBarsPadding()
            .imePadding(),
    ) {
        TopBarChat(
            title = state.chat?.title.orEmpty(),
            photoUrl = state.chat?.photoUrl.orEmpty(),
            esGrupo = state.chat?.type == ChatType.GROUP,
            actividad = state.actividad,
            onBack = onBack,
            // Tocar el logo/nombre de un chat de grupo abre la info de la
            // actividad (su pantalla de detalle: fecha, lugar, asistentes).
            onOpenInfo = state.actividad?.let { actividad ->
                { onOpenActivity(actividad.id) }
            },
        )
        HorizontalDivider(color = Mint)

        when {
            // weight(1f) (nunca fillMaxSize): si el estado vacío/cargando se
            // lleva toda la altura con fillMaxSize, la barra de entrada que
            // viene después queda medida con 0 dp y no se puede tocar.
            state.phase == ChatDetailPhase.LOADING -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = s.loadingData, color = TextSecondary)
                }
            }

            state.mensajes.isEmpty() -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = s.chatNoMessages,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }

            else -> LazyColumn(
                state = lista,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(state.mensajes, key = { _, mensaje -> mensaje.id }) { indice, mensaje ->
                    if (mensaje.esAvisoDeSistema()) {
                        // "X se unió / salió": centrado y tocable → vista previa.
                        AvisoDeSistema(
                            mensaje = mensaje,
                            onClick = { viewModel.verPerfil(mensaje.targetUid) },
                        )
                    } else {
                        val anterior = state.mensajes.getOrNull(indice - 1)
                        Burbuja(
                            mensaje = mensaje,
                            esMia = mensaje.esMio(state.miUid),
                            mostrarEmisor = anterior?.senderId != mensaje.senderId,
                            esGrupo = state.chat?.type == ChatType.GROUP,
                        )
                    }
                }
            }
        }

        BarraDeEntrada(
            texto = texto,
            enviando = state.enviando,
            onTextoChange = { texto = it },
            onEnviar = {
                viewModel.enviar(texto)
                texto = ""
            },
            s = s,
        )
    }

    state.error?.let { error ->
        AppDialog(
            style = DialogStyle.ERROR,
            title = s.upsTitle,
            message = mensajeDe(error, s),
            onDismiss = viewModel::limpiarError,
        )
    }

    // Vista previa del perfil al tocar un aviso de sistema (entró/salió).
    state.perfilPreview?.let { perfil ->
        PerfilPreviewDialog(perfil = perfil, onDismiss = viewModel::cerrarPerfil)
    }
}

/** Barra superior: regreso, avatar, nombre y estado de la actividad (grupo). */
@Composable
private fun TopBarChat(
    title: String,
    photoUrl: String,
    esGrupo: Boolean,
    actividad: ActivityItem?,
    onBack: () -> Unit,
    onOpenInfo: (() -> Unit)? = null,
) {
    val s = LocalStrings.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(horizontal = 6.dp, vertical = 8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = s.back,
                tint = TextPrimary,
            )
        }

        // Logo + nombre del chat: en un chat de actividad abre su info
        // (pantalla de detalle con fecha, lugar y asistentes).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onOpenInfo != null) {
                        Modifier.clickable(onClick = onOpenInfo)
                    } else {
                        Modifier
                    },
                )
                .padding(vertical = 2.dp),
        ) {
            InitialAvatar(
                initial = title.take(1),
                photoUrl = photoUrl,
                sizeDp = 40,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Solo los chats de grupo muestran el estado del evento.
                if (esGrupo) {
                    estadoDeActividad(actividad, s)?.let { estado ->
                        Text(
                            text = estado,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (onOpenInfo != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = s.activitiesDetailTitle,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Burbuja de un mensaje: en grupo, el avatar y el nombre del emisor aparecen
 * encima cuando cambian respecto al mensaje anterior.
 */
@Composable
private fun Burbuja(
    mensaje: Message,
    esMia: Boolean,
    mostrarEmisor: Boolean,
    esGrupo: Boolean,
) {
    val s = LocalStrings.current

    Row(
        horizontalArrangement = if (esMia) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (!esMia && esGrupo && mostrarEmisor) {
            InitialAvatar(
                initial = mensaje.senderName.take(1),
                photoUrl = null,
                sizeDp = 28,
                backgroundColor = Mint,
                textColor = BrandGreen,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (esMia) Alignment.End else Alignment.Start) {
            if (!esMia && mostrarEmisor && esGrupo) {
                Text(
                    text = mensaje.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 6.dp, bottom = 3.dp),
                )
            }
            val forma = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (esMia) 18.dp else 4.dp,
                bottomEnd = if (esMia) 4.dp else 18.dp,
            )
            Box(
                modifier = Modifier
                    .clip(forma)
                    .background(if (esMia) BrandGreen else SurfaceWhite)
                    // Las ajenas llevan borde para verse sobre el fondo gris.
                    .then(if (esMia) Modifier else Modifier.border(1.dp, Mint, forma))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = mensaje.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (esMia) SurfaceWhite else TextPrimary,
                )
            }

            // Hora a la que se envió, en el idioma activo ("2:14 p.m.").
            if (mensaje.timestamp > 0L) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = horaDelMensaje(mensaje.timestamp, s),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }
        }
    }
}

/**
 * Aviso de sistema del grupo ("X se unió" / "X salió"): centrado, en color
 * menta y tocable para abrir la vista previa del perfil de esa persona.
 */
@Composable
private fun AvisoDeSistema(mensaje: Message, onClick: () -> Unit) {
    val s = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Mint)
                .clickable(enabled = mensaje.targetUid.isNotBlank(), onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 7.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (mensaje.kind == MessageKind.MEMBER_JOINED) {
                        Icons.Filled.PersonAdd
                    } else {
                        Icons.Filled.PersonRemove
                    },
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (mensaje.kind) {
                        MessageKind.MEMBER_JOINED -> s.systemMemberJoined(mensaje.targetName)
                        MessageKind.MEMBER_LEFT -> s.systemMemberLeft(mensaje.targetName)
                        else -> mensaje.text
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandGreen,
                )
            }
        }
    }
}

/**
 * Vista previa del perfil de quien entró o salió del grupo (foto, nombre,
 * universidad, biografía e intereses) sin salir de la conversación.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PerfilPreviewDialog(perfil: User, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val nombre = perfil.firstName.ifBlank { perfil.username }
        .ifBlank { s.profileFallbackUsername }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = s.profilePreviewTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen,
                )
                Spacer(modifier = Modifier.height(16.dp))
                InitialAvatar(
                    initial = nombre.take(1),
                    photoUrl = perfil.profilePictureUrl,
                    sizeDp = 84,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
                if (perfil.university.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = perfil.university,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                if (perfil.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = perfil.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
                if (perfil.interests.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = s.interestsTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterHorizontally,
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        perfil.interests.forEach { interes ->
                            InterestPill(
                                label = interes,
                                selected = true,
                                onClick = {},
                                modifier = Modifier.height(32.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text(text = s.close, color = BrandGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Campo de texto + botón de enviar (icono de flecha, verde de la marca). */
@Composable
private fun BarraDeEntrada(
    texto: String,
    enviando: Boolean,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    s: AppStrings,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(BackgroundApp)
                .border(1.dp, OutlineGray, RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 24.dp),
        ) {
            if (texto.isEmpty()) {
                Text(
                    text = s.messageInputHint,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            BasicTextField(
                value = texto,
                onValueChange = { if (it.length <= MAX_CARACTERES) onTextoChange(it) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                cursorBrush = SolidColor(BrandGreen),
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        IconButton(
            onClick = onEnviar,
            enabled = texto.isNotBlank() && !enviando,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (texto.isNotBlank() && !enviando) BrandGreen else Mint),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = s.sendMessageCd,
                tint = if (texto.isNotBlank() && !enviando) SurfaceWhite else Sage,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** "2:14 p.m." en el idioma activo, a partir de los millis del mensaje. */
private fun horaDelMensaje(millis: Long, s: AppStrings): String {
    val hora = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime()
    return s.formatTime("%02d:%02d".format(hora.hour, hora.minute))
}

/**
 * Estado del evento para el top bar de un chat de grupo: "Activa hoy" si la
 * fecha es hoy; si no, "Programada: 22/10/2026". Sin fecha legible no se
 * muestra nada.
 */
private fun estadoDeActividad(actividad: ActivityItem?, s: AppStrings): String? {
    actividad ?: return null
    val millis = actividad.date.toLongOrNull()
        ?: runCatching {
            LocalDate.parse(actividad.date)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull() ?: return null

    val fecha = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return if (fecha == LocalDate.now()) {
        s.chatActivityActive
    } else {
        s.chatActivityScheduled(ActivityFormat.fechaCorta(millis, s.javaLocale))
    }
}

private const val MAX_CARACTERES = 1000
