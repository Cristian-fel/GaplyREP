package com.gaply.app.domain.repository

import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.Message
import com.gaply.app.domain.model.RepoResult
import kotlinx.coroutines.flow.Flow

/**
 * Módulo 5 · Chats · Repositorio.
 *
 * Contrato para las conversaciones de Gaply: la implementación real es
 * [com.gaply.app.data.repository.FirebaseChatsRepository] (`chats/{chatId}` +
 * `messages` en Firestore, con listeners en tiempo real) y la de respaldo en
 * memoria es [com.gaply.app.data.repository.InMemoryChatsRepository].
 *
 * Alcance de este corte: solo mensajes de texto y sin presencia/"en línea".
 */
interface ChatsRepository {

    /** Chats del usuario actual en tiempo real (más reciente primero, tope 30). */
    fun observeChats(): Flow<RepoResult<List<Chat>>>

    /** Mensajes de una conversación en tiempo real (los 50 más recientes, más reciente primero). */
    fun observeMessages(chatId: String): Flow<RepoResult<List<Message>>>

    /**
     * Envía un mensaje de texto. El mensaje se escribe solo (si eso falla, no
     * se envió nada); la vista previa (`lastMessage`/`updatedAt`) y los no
     * leídos del resto son mejor esfuerzo con [members] (los entrega quien
     * llama: así no hay que releer el chat, que las reglas solo dejan leer
     * cuando ya soy miembro).
     */
    suspend fun sendMessage(
        chatId: String,
        senderName: String,
        text: String,
        members: List<String>,
        senderPhotoUrl: String = "",
    ): RepoResult<String>

    /** Pone en 0 mi contador de no leídos al abrir la conversación. */
    suspend fun markAsRead(chatId: String): RepoResult<Unit>

    /**
     * "Eliminar chat": solo lo saca de MI lista (`hiddenFor`). Es local a
     * propósito, como en WhatsApp: no borra los mensajes ni afecta a los
     * demás participantes.
     */
    suspend fun hideChat(chatId: String): RepoResult<Unit>

    /**
     * "Destacar chat": el chat se queda arriba del todo aunque otros tengan
     * mensajes más recientes. También es por usuario (`pinnedBy`).
     */
    suspend fun setPinned(chatId: String, pinned: Boolean): RepoResult<Unit>

    /**
     * Crea (si no existe) el chat directo con id determinístico
     * `direct_{uidA}_{uidB}`: así la carrera de ambos clientes nunca lo duplica.
     *
     * @param otherName, @param otherPhotoUrl datos del otro usuario para el título.
     */
    suspend fun ensureDirectChat(
        otherUid: String,
        otherName: String,
        otherPhotoUrl: String,
    ): RepoResult<String>

    /**
     * Crea o actualiza el chat de grupo de una actividad (id `act_{activityId}`)
     * con `members` = dueño + asistentes (nombre del campo fijado por
     * `firestore.rules`). Idempotente: no pisa el último mensaje si el chat
     * ya existía. No publica avisos de sistema (lo usa publicar/editar).
     */
    suspend fun ensureGroupChat(activity: ActivityItem): RepoResult<String>

    /**
     * Entrada de un miembro al chat de grupo: **primero** me hace participante
     * y enseguida publica el aviso de sistema "<nombre> se unió", porque las
     * reglas solo dejan escribir mensajes siendo miembro.
     */
    suspend fun joinGroupChat(activity: ActivityItem, memberName: String): RepoResult<String>

    /**
     * Salida de un miembro del chat de grupo: publica el aviso "<nombre> salió"
     * **mientras aún soy miembro** y enseguida me quita de `members` (a partir
     * de ahí no veo esa conversación, igual que al salirme de la actividad).
     */
    suspend fun leaveGroupChat(activityId: String, memberName: String): RepoResult<String>
}
