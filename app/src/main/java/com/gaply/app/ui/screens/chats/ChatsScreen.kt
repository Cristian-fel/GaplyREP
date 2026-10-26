package com.gaply.app.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.ChatType
import com.gaply.app.domain.model.RepoError
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.mensajeDe
import com.gaply.app.ui.screens.activities.InitialAvatar
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.OutlineGray
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextPrimary
import com.gaply.app.ui.theme.TextSecondary
import java.text.Normalizer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Modos del menú de los tres puntos de la cabecera de Chats. */
private enum class ModoChats { ELIMINAR, DESTACAR }

/**
 * Módulo 5 · Pestaña "Chats" (embebida en la Home, sin ruta propia).
 *
 * Estilo WhatsApp: **cabecera fija** con el nombre de la app y un buscador de
 * chats/personas que no se va con el scroll; debajo, la lista en tiempo real
 * con avatar (y su distintivo), nombre, último mensaje, fecha a la derecha y
 * badge de no leídos. Tocar una fila abre `ChatDetail`.
 */
@Composable
fun ChatsScreen(
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: ChatsViewModel = viewModel(
        factory = ChatsViewModel.factory(context.appContainer()),
    )
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current
    var consulta by remember { mutableStateOf("") }
    var modo by remember { mutableStateOf<ModoChats?>(null) }
    var chatAEliminar by remember { mutableStateOf<Chat?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundApp),
    ) {
        CabeceraChats(
            consulta = consulta,
            onConsultaChange = { consulta = it },
            modo = modo,
            onElegirModo = { nuevoModo -> modo = nuevoModo },
            onCancelarModo = { modo = null },
            s = s,
        )

        // Filtrado en cliente sobre nombre y última frase (sin tildes ni
        // mayúsculas); la lista completa sigue viva en el estado. En modo
        // eliminar/destacar se ignora la búsqueda para no esconder el objetivo.
        val visibles = when {
            modo != null -> state.chats
            consulta.isBlank() -> state.chats
            else -> state.chats.filter { coincideCon(it, consulta) }
        }

        when {
            state.phase == ChatsPhase.LOADING -> EstadoCargando(s, Modifier.weight(1f))

            state.phase == ChatsPhase.ERROR -> EstadoVacio(
                icono = Icons.Filled.ChatBubble,
                mensaje = mensajeDe(state.error ?: RepoError.UNKNOWN, s),
                modifier = Modifier.weight(1f),
            )

            state.chats.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.ChatBubble,
                mensaje = s.chatsEmptyMsg,
                modifier = Modifier.weight(1f),
            )

            visibles.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.Search,
                mensaje = s.searchNoResults,
                modifier = Modifier.weight(1f),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(visibles, key = { it.id }) { chat ->
                    ChatFila(
                        chat = chat,
                        onClick = {
                            when (modo) {
                                // Tocar la fila es la acción elegida en el menú.
                                ModoChats.ELIMINAR -> {
                                    chatAEliminar = chat
                                    modo = null
                                }

                                ModoChats.DESTACAR -> {
                                    viewModel.alternarDestacado(chat)
                                    modo = null
                                }

                                null -> onOpenChat(chat.id)
                            }
                        },
                    )
                    HorizontalDivider(color = Mint)
                }
            }
        }
    }

    // Confirmación de "Eliminar chat": solo sale de MI lista (los mensajes y
    // los demás participantes no se tocan).
    chatAEliminar?.let { chat ->
        AlertDialog(
            onDismissRequest = { chatAEliminar = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = SurfaceWhite,
            title = {
                Text(
                    text = s.deleteChatConfirmTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
            },
            text = {
                Text(
                    text = s.deleteChatConfirmMsg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarChat(chat.id)
                        chatAEliminar = null
                    },
                ) {
                    Text(s.delete, color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatAEliminar = null }) {
                    Text(s.cancel, color = BrandGreen)
                }
            },
        )
    }

    state.error?.let { error ->
        AppDialog(
            style = DialogStyle.WARNING,
            title = s.upsTitle,
            message = mensajeDe(error, s),
            onDismiss = viewModel::limpiarError,
        )
    }
}

/**
 * Cabecera fija de la pestaña (fuera del `LazyColumn`: no se va con el
 * scroll): nombre de la app + buscador + menú de tres puntos con las
 * acciones "Eliminar chat" y "Destacar chat".
 *
 * Al elegir una acción el listado entra en modo selección: la cabecera
 * cambia su título por el de la acción y muestra cómo salir de él.
 */
@Composable
private fun CabeceraChats(
    consulta: String,
    onConsultaChange: (String) -> Unit,
    modo: ModoChats?,
    onElegirModo: (ModoChats) -> Unit,
    onCancelarModo: () -> Unit,
    s: AppStrings,
) {
    var menuAbierto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (modo) {
                    ModoChats.ELIMINAR -> s.deleteChatOption
                    ModoChats.DESTACAR -> s.pinChatOption
                    null -> s.appTitle
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
            )
            if (modo == null) {
                Box {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = s.expandOptionsCd,
                            tint = Color.Black,
                        )
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(s.deleteChatOption) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                )
                            },
                            onClick = {
                                menuAbierto = false
                                onElegirModo(ModoChats.ELIMINAR)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(s.pinChatOption) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                )
                            },
                            onClick = {
                                menuAbierto = false
                                onElegirModo(ModoChats.DESTACAR)
                            },
                        )
                    }
                }
            } else {
                IconButton(onClick = onCancelarModo) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = s.close,
                        tint = Color.Black,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (modo != null) {
            // En modo selección el buscador se sustituye por la indicación.
            Text(
                text = s.pickChatHint,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(BackgroundApp)
                    .border(1.dp, OutlineGray, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = s.searchChatsHint,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = consulta,
                    onValueChange = onConsultaChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    cursorBrush = SolidColor(BrandGreen),
                    modifier = Modifier.weight(1f),
                    decorationBox = { caja ->
                        if (consulta.isEmpty()) {
                            Text(
                                text = s.searchChatsHint,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                            )
                        }
                        caja()
                    },
                )
                if (consulta.isNotEmpty()) {
                    IconButton(
                        onClick = { onConsultaChange("") },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = s.close,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider(color = Mint)
}

/**
 * Fila de la lista: avatar con el distintivo del tipo (evento = chat de
 * actividad, persona = chat directo), nombre, última frase y no leídos.
 */
@Composable
private fun ChatFila(chat: Chat, onClick: () -> Unit) {
    val s = LocalStrings.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        AvatarConTipo(chat = chat)
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = true),
                )
                if (chat.pinned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = s.pinChatOption,
                        tint = Sage,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = chat.lastMessage.ifBlank { s.chatNoMessages },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Derecha del todo: fecha del último mensaje (hora si es hoy) y, debajo,
        // el contador de no leídos.
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = fechaDelChat(chat.updatedAt, s),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.End,
            )
            if (chat.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(BrandGreen)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        color = SurfaceWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * Avatar del chat con un distintivo pequeño abajo a la derecha: [Icons.Filled.Event]
 * para el chat de una actividad y [Icons.Filled.Person] para una persona.
 */
@Composable
private fun AvatarConTipo(chat: Chat) {
    Box {
        InitialAvatar(
            initial = chat.title.take(1),
            photoUrl = chat.photoUrl,
            sizeDp = 50,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 3.dp, y = 3.dp)
                .size(20.dp)
                .background(SurfaceWhite, CircleShape)
                .border(1.dp, Mint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (chat.type == ChatType.GROUP) {
                    Icons.Filled.Event
                } else {
                    Icons.Filled.Person
                },
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun EstadoCargando(s: AppStrings, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = BrandGreen)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = s.loadingData, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EstadoVacio(
    icono: ImageVector,
    mensaje: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Sage,
            modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Fecha a la derecha de cada chat: **hora** si el último mensaje es de hoy
 * ("2:14 p.m.") y **fecha** en caso contrario ("5 de octubre"), siempre en el
 * idioma activo. Sin mensajes recientes no se muestra nada.
 */
private fun fechaDelChat(millis: Long, s: AppStrings): String {
    if (millis <= 0L) return ""
    val instante = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    return if (instante.toLocalDate() == LocalDate.now()) {
        s.formatTime("%02d:%02d".format(instante.hour, instante.minute))
    } else {
        s.formatMonthDay(instante.toLocalDate())
    }
}

/** Búsqueda del buscador: nombre del chat o última frase, sin tildes ni mayúsculas. */
private fun coincideCon(chat: Chat, consulta: String): Boolean {
    val busqueda = normalizar(consulta)
    if (busqueda.isBlank()) return true
    return normalizar(chat.title).contains(busqueda) ||
        normalizar(chat.lastMessage).contains(busqueda)
}

private fun normalizar(texto: String): String =
    Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
