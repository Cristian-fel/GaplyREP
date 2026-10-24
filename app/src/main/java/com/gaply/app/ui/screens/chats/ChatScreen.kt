package com.gaply.app.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.domain.model.ChatMessage
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextPrimary
import com.gaply.app.ui.theme.TextSecondary
import java.text.DateFormat
import java.util.Date

/**
 * Conversación de un match: cabecera con la otra persona, burbujas en vivo
 * (snapshot listener de Firestore) y caja de texto para escribir.
 */
@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit,
) {
    val viewModel = rememberChatViewModel(chatId)
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    var texto by remember { mutableStateOf("") }
    val listaState = rememberLazyListState()

    // Al llegar un mensaje nuevo, baja al final de la conversación.
    LaunchedEffect(state.mensajes.size) {
        if (state.mensajes.isNotEmpty()) {
            listaState.animateScrollToItem(state.mensajes.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
            .imePadding(),
    ) {
        // Cabecera: atrás + avatar + nombre
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back,
                    tint = TextPrimary,
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (state.foto.isBlank()) BrandGreen else Mint),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    // Chat del evento: icono de actividad en lugar de avatar.
                    state.esGrupo -> Text(text = "🎯", fontSize = 22.sp)

                    state.foto.isNotBlank() -> AsyncImage(
                        model = state.foto,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )

                    else -> Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = SurfaceWhite,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = state.titulo.ifBlank { "…" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
            )
        }

        when {
            state.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = BrandGreen)
            }

            else -> LazyColumn(
                state = listaState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.mensajes, key = { it.id }) { mensaje ->
                    Burbuja(
                        mensaje = mensaje,
                        soyYo = mensaje.senderId == state.yo,
                        mostrarNombre = state.esGrupo,
                    )
                }
            }
        }

        // Caja de texto + botón enviar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text(text = s.chatHint) },
                maxLines = 4,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (texto.isNotBlank()) {
                        viewModel.enviar(texto)
                        texto = ""
                    }
                },
                enabled = texto.isNotBlank(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = s.rateSendButton,
                    tint = if (texto.isNotBlank()) BrandGreen else TextSecondary,
                )
            }
        }
    }
}

/** Burbuja de mensaje: mía = verde a la derecha, de la otra persona = blanca a la izquierda. */
@Composable
private fun Burbuja(
    mensaje: ChatMessage,
    soyYo: Boolean,
    mostrarNombre: Boolean = false,
) {
    val hora = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(mensaje.createdAt))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (soyYo) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (soyYo) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            // En grupos, quién escribió el mensaje (los míos no llevan nombre).
            if (mostrarNombre && !soyYo && mensaje.senderName.isNotBlank()) {
                Text(
                    text = mensaje.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                )
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (soyYo) 16.dp else 4.dp,
                            bottomEnd = if (soyYo) 4.dp else 16.dp,
                        ),
                    )
                    .background(if (soyYo) BrandGreen else SurfaceWhite)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = mensaje.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (soyYo) Color.White else TextPrimary,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hora,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}
