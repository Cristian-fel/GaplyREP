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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.domain.model.ChatRoom
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.OutlineGray
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextPrimary
import com.gaply.app.ui.theme.TextSecondary
import java.text.DateFormat
import java.util.Date

/**
 * Pestaña Chats · lista de conversaciones (personas con las que hay match).
 *
 * Cada fila abre la conversación con `onOpenChat(chatId)`; la lista se
 * recarga al entrar a la pestaña para que un match recién creado aparezca
 * sin reiniciar la app.
 */
@Composable
fun ChatsScreen(
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = rememberChatsViewModel()
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    // Refresco al entrar: si volviste de un match nuevo, la sala ya existe.
    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundApp),
    ) {
        when {
            state.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = BrandGreen)
            }

            state.error != null -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = "📡", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = s.connectionErrorTitle,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.tapToRetry,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.cargar() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            state.vacio -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = "💬", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = s.tabChats,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.chatsEmptyMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(state.salas, key = { it.id }) { sala ->
                    FilaChat(
                        sala = sala,
                        yo = state.yo,
                        onClick = { onOpenChat(sala.id) },
                    )
                }
            }
        }
    }
}

/** Fila: avatar de la otra persona + nombre + último mensaje + hora. */
@Composable
private fun FilaChat(
    sala: ChatRoom,
    yo: String,
    onClick: () -> Unit,
) {
    val esGrupo = sala.kind == "group"
    val otro = sala.memberIds.firstOrNull { it != yo }.orEmpty()
    val nombre = if (esGrupo) {
        sala.groupTitle.ifBlank { "—" }
    } else {
        sala.memberNames[otro].orEmpty().ifBlank { "—" }
    }
    val foto = if (esGrupo) "" else sala.memberPhotos[otro].orEmpty()
    val hora = if (sala.lastAt > 0L) {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(sala.lastAt))
    } else {
        ""
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
    ) {
        // Avatar (🎯 para el chat del evento, foto o persona para un dm)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (foto.isBlank()) BrandGreen else Mint),
            contentAlignment = Alignment.Center,
        ) {
            when {
                esGrupo -> Text(text = "🎯", fontSize = 22.sp)

                foto.isNotBlank() -> AsyncImage(
                    model = foto,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                else -> Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sala.lastMessage.ifBlank { "…" },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = hora,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}
