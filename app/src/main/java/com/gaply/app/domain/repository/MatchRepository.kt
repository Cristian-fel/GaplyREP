package com.gaply.app.domain.repository

import com.gaply.app.domain.model.ChatMessage
import com.gaply.app.domain.model.ChatRoom
import com.gaply.app.domain.model.FeedCandidates
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.SwipeAction
import kotlinx.coroutines.flow.Flow

/**
 * Módulo Matches y Chats.
 *
 * Implementación en la nube: [com.gaply.app.data.repository.FirebaseMatchRepository]
 * (colecciones `swipes`, `matches` y `chats` de Firestore). Respaldo local:
 * [com.gaply.app.data.repository.InMemoryMatchRepository].
 *
 * IMPORTANTE: requiere reglas de Firestore para esas colecciones
 * (lectura/escritura de usuarios autenticados); sin ellas Firestore
 * responde PERMISSION_DENIED y la pantalla muestra su estado de error.
 */
interface MatchRepository {

    /**
     * Cartas para el mazo: personas (menos yo y las que ya vi) y actividades
     * (menos las mías, las que ya uní y las que ya vi).
     */
    suspend fun getCandidates(): RepoResult<FeedCandidates>

    /**
     * Registra un gesto sobre una carta. Si es una persona y la acción es
     * [SwipeAction.STAR] o [SwipeAction.LIKE] y la otra persona ya me había
     * dado, crea el match y su sala de chat (idempotente).
     *
     * @param targetId uid de la persona o id de la actividad.
     * @param isPerson true cuando la carta es un perfil, false si es actividad.
     */
    suspend fun registerSwipe(targetId: String, action: SwipeAction, isPerson: Boolean): RepoResult<String>

    /** Lista de conversaciones del usuario actual (orden: más reciente primero). */
    suspend fun getChats(): RepoResult<List<ChatRoom>>

    /**
     * Crea (o actualiza) el chat grupal de una actividad con id fijo
     * `act_<activityId>` — así nunca se duplica: si ya existe se reutiliza.
     *
     * @param joined true = agrega al usuario actual como miembro (arrayUnion);
     *               false = lo retira (arrayRemove, solo si la sala existe).
     * @return el chatId para navegar a la conversación.
     */
    suspend fun syncGroupChat(activityId: String, groupTitle: String, joined: Boolean): RepoResult<String>

    /**
     * Mensajes de una sala en vivo (snapshot listener de Firestore).
     * Emite la lista completa ante cada cambio; se cancela al cerrar el Flow.
     */
    fun observeMessages(chatId: String): Flow<List<ChatMessage>>

    /** Envía un mensaje y actualiza `lastMessage`/`lastAt` de la sala. */
    suspend fun sendMessage(chatId: String, text: String): RepoResult<String>
}
