package com.gaply.app.data.repository

import com.gaply.app.data.local.LocalUserStore
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.ChatIds
import com.gaply.app.domain.model.ChatType
import com.gaply.app.domain.model.Message
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.ChatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Respaldo de Chats en memoria (se usa cuando AppContainer.USE_FIREBASE es
 * false). Reproduce el esquema de Firestore (`chats` + `messages` +
 * `unreadCounts` por uid) con StateFlows, así la lista y los mensajes siguen
 * "en tiempo real" dentro del proceso: sirve para probar el módulo sin
 * conexión y sin consola de Firebase.
 */
class InMemoryChatsRepository(
    private val store: LocalUserStore,
) : ChatsRepository {

    private val chats = MutableStateFlow<Map<String, Chat>>(emptyMap())
    private val mensajes = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

    /** chatId -> uid -> no leídos (el equivalente al mapa `unreadCounts`). */
    private val noLeidos = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())

    /** chatId -> uid -> millis de la última vez que lo abrió (`lastReadAt`). */
    private val ultimaLectura = MutableStateFlow<Map<String, Map<String, Long>>>(emptyMap())

    private fun miUid(): String? = store.currentUser.value?.userId

    override fun observeChats(): Flow<RepoResult<List<Chat>>> =
        combine(chats, noLeidos, ultimaLectura) { mapa, leidos, lecturas ->
            val uid = miUid()
                ?: return@combine RepoResult.Success<List<Chat>>(emptyList())
            val usuarios = store.loadUsers().associateBy { it.userId }
            RepoResult.Success(
                mapa.values
                    .filter { uid in it.participants }
                    .sortedByDescending { it.updatedAt }
                    .take(LIST_CHATS)
                    .map { chat ->
                        chat.copy(
                            unreadCount = leidos[chat.id]?.get(uid) ?: 0,
                            lastReadAt = lecturas[chat.id]?.get(uid),
                        ).conPerspectiva(uid, usuarios)
                    },
            )
        }

    override fun observeMessages(chatId: String): Flow<RepoResult<List<Message>>> =
        mensajes.map { mapa ->
            RepoResult.Success(
                (mapa[chatId] ?: emptyList()).sortedByDescending { it.timestamp },
            )
        }

    override suspend fun sendMessage(
        chatId: String,
        senderName: String,
        text: String,
        members: List<String>,
    ): RepoResult<String> {
        val uid = miUid() ?: return RepoResult.Error(RepoError.PERMISSION)
        val contenido = text.trim()
        if (contenido.isEmpty()) return RepoResult.Error(RepoError.EMPTY_FIELDS)
        val chat = chats.value[chatId] ?: return RepoResult.Error(RepoError.UNKNOWN)

        val ahora = System.currentTimeMillis()
        val nuevo = Message(
            id = UUID.randomUUID().toString(),
            senderId = uid,
            senderName = senderName,
            text = contenido,
            timestamp = ahora,
        )
        val historial = (mensajes.value[chatId] ?: emptyList()) + nuevo
        mensajes.value = mensajes.value + (chatId to historial)
        chats.value = chats.value + (chatId to chat.copy(lastMessage = contenido, updatedAt = ahora))

        val otros = chat.participants.filter { it != uid }
        if (otros.isNotEmpty()) {
            val actual = noLeidos.value[chatId] ?: emptyMap()
            val acumulado = otros.fold(actual) { acc, otro ->
                acc + (otro to ((acc[otro] ?: 0) + 1))
            }
            noLeidos.value = noLeidos.value + (chatId to acumulado)
        }
        return RepoResult.Success(chatId)
    }

    override suspend fun markAsRead(chatId: String): RepoResult<Unit> {
        val uid = miUid() ?: return RepoResult.Error(RepoError.PERMISSION)
        val actual = noLeidos.value[chatId] ?: emptyMap()
        noLeidos.value = noLeidos.value + (chatId to (actual + (uid to 0)))
        val lecturas = ultimaLectura.value[chatId] ?: emptyMap()
        ultimaLectura.value = ultimaLectura.value + (chatId to (lecturas + (uid to System.currentTimeMillis())))
        return RepoResult.Success(Unit)
    }

    override suspend fun ensureDirectChat(
        otherUid: String,
        otherName: String,
        otherPhotoUrl: String,
    ): RepoResult<String> {
        val uid = miUid() ?: return RepoResult.Error(RepoError.PERMISSION)
        if (otherUid.isBlank() || otherUid == uid) return RepoResult.Error(RepoError.EMPTY_FIELDS)

        val chatId = ChatIds.direct(uid, otherUid)
        if (!chats.value.containsKey(chatId)) {
            chats.value = chats.value + (chatId to Chat(
                id = chatId,
                type = ChatType.DIRECT,
                participants = listOf(uid, otherUid).sorted(),
                title = otherName,
                photoUrl = otherPhotoUrl,
                updatedAt = System.currentTimeMillis(),
            ))
        }
        return RepoResult.Success(chatId)
    }

    override suspend fun ensureGroupChat(activity: ActivityItem): RepoResult<String> {
        val uid = miUid() ?: return RepoResult.Error(RepoError.PERMISSION)
        if (activity.id.isBlank()) return RepoResult.Error(RepoError.EMPTY_FIELDS)

        val chatId = ChatIds.group(activity.id)
        val participantes = (activity.attendeeIds + activity.ownerId)
            .filter { it.isNotBlank() }
            .plus(uid)
            .distinct()
        val existente = chats.value[chatId]
        val actualizado = if (existente == null) {
            Chat(
                id = chatId,
                type = ChatType.GROUP,
                participants = participantes,
                activityId = activity.id,
                title = activity.title,
                photoUrl = activity.imageUri.orEmpty(),
                updatedAt = System.currentTimeMillis(),
            )
        } else {
            existente.copy(
                participants = (existente.participants + participantes).distinct(),
                activityId = activity.id,
                title = activity.title,
                photoUrl = activity.imageUri.orEmpty(),
            )
        }
        chats.value = chats.value + (chatId to actualizado)
        return RepoResult.Success(chatId)
    }

    /**
     * El título de un chat directo depende de quién lo mire: aquí se rellena
     * con el otro participante (mismo criterio que en la nube).
     */
    private fun Chat.conPerspectiva(
        uid: String,
        usuarios: Map<String, User>,
    ): Chat {
        if (type != ChatType.DIRECT) return this
        val otro = participants.firstOrNull { it != uid } ?: return this
        val perfil = usuarios[otro] ?: return this
        val nombre = perfil.firstName.ifBlank { perfil.username }
        return copy(
            title = nombre.ifBlank { title },
            photoUrl = perfil.profilePictureUrl.ifBlank { photoUrl },
        )
    }

    private companion object {
        const val LIST_CHATS = 30
    }
}
