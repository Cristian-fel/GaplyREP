package com.gaply.app.ui.screens.chats

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

/**
 * Módulo 5 · Pestaña "Chats" (embebida en la Home, sin ruta propia).
 *
 * Lista las conversaciones del usuario en tiempo real: avatar, nombre,
 * etiqueta "Grupo" cuando corresponde, vista previa del último mensaje,
 * tiempo relativo y badge de no leídos. Tocar una fila abre `ChatDetail`.
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundApp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = s.tabChats,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (state.phase) {
            ChatsPhase.LOADING -> EstadoCargando(s)

            ChatsPhase.EMPTY -> EstadoVacio(
                icono = Icons.Filled.ChatBubble,
                mensaje = s.chatsEmptyMsg,
            )

            ChatsPhase.ERROR -> EstadoVacio(
                icono = Icons.Filled.ChatBubble,
                mensaje = mensajeDe(state.error ?: RepoError.UNKNOWN, s),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(state.chats, key = { it.id }) { chat ->
                    ChatFila(chat = chat, onClick = { onOpenChat(chat.id) })
                    HorizontalDivider(color = Mint)
                }
            }
        }
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

/** Fila de la lista: avatar, nombre + tag, última frase, hora y no leídos. */
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
        InitialAvatar(
            initial = chat.title.take(1),
            photoUrl = chat.photoUrl,
            sizeDp = 50,
        )
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
                if (chat.type == ChatType.GROUP) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Mint)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = s.chatGroupTag,
                            color = BrandGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
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

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = tiempoRelativo(chat.updatedAt, s),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
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

@Composable
private fun EstadoCargando(s: AppStrings) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
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

/** "ahora" / "hace 5 min" / "hace 3 h" / "ayer" / "hace 4 días" (vía i18n). */
internal fun tiempoRelativo(millis: Long, s: AppStrings): String {
    if (millis <= 0L) return ""
    val transcurrido = System.currentTimeMillis() - millis
    val minutos = (transcurrido / 60_000L).toInt().coerceAtLeast(0)
    return when {
        minutos < 1 -> s.timeJustNow
        minutos < 60 -> s.timeAgoMinutes(minutos)
        else -> {
            val horas = minutos / 60
            if (horas < 24) s.timeAgoHours(horas) else s.timeAgoDays((horas / 24).coerceAtLeast(1))
        }
    }
}
