package com.gaply.app.data.repository

import com.gaply.app.data.mapper.CAMPO_DESTACADOS
import com.gaply.app.data.mapper.CAMPO_MIEMBROS
import com.gaply.app.data.mapper.CAMPO_OCULTOS
import com.gaply.app.data.mapper.CAMPO_TIPO_MENSAJE
import com.gaply.app.data.mapper.CAMPO_ULTIMA_LECTURA
import com.gaply.app.data.mapper.aChat
import com.gaply.app.data.mapper.aFirestoreMap
import com.gaply.app.data.mapper.aMessage
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.ChatIds
import com.gaply.app.domain.model.ChatType
import com.gaply.app.domain.model.Message
import com.gaply.app.domain.model.MessageKind
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.ChatsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import android.util.Log

/**
 * Módulo 5 · Chats en la nube (`chats/{chatId}` + `messages` en Firestore).
 *
 * Lecturas en tiempo real con `addSnapshotListener` dentro de `callbackFlow`
 * (el listener se quita en `awaitClose`):
 *  - lista: `where(members array-contains yo).orderBy(updatedAt desc).limit(30)`;
 *  - mensajes: `orderBy(timestamp desc).limit(50)`.
 *
 * El campo de los miembros se llama `members` (no `participants`) porque
 * así lo exige `firestore.rules`; ver [CAMPO_MIEMBROS].
 *
 * La lista requiere un **índice compuesto** (`members` + `updatedAt`). Si aún
 * no existe, Firestore responde FAILED_PRECONDITION y aquí mismo se
 * cae a la consulta sin `orderBy` ordenando en cliente, para que la pantalla
 * nunca se quede rota a la espera de un índice.
 *
 * Escrituras:
 *  - `sendMessage`: un batch con el add del mensaje + la actualización de
 *    `lastMessage`, `updatedAt` e `unreadCounts` (increment para el resto).
 *  - `markAsRead`: pone `unreadCounts[miUid]` en 0 con merge.
 *
 * Alcance de este corte: solo texto, sin presencia/"en línea".
 */
class FirebaseChatsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ChatsRepository {

    /** uid -> (nombre, foto): evita releer el perfil en cada snapshot. */
    private val perfiles = ConcurrentHashMap<String, Pair<String, String>>()

    override fun observeChats(): Flow<RepoResult<List<Chat>>> =
        chatsSnapshot().map { resultado ->
            when (resultado) {
                is RepoResult.Success -> RepoResult.Success(enriquecer(resultado.data))
                is RepoResult.Error -> resultado
            }
        }

    override fun observeMessages(chatId: String): Flow<RepoResult<List<Message>>> = callbackFlow {
        if (chatId.isBlank()) {
            trySend(RepoResult.Error(RepoError.EMPTY_FIELDS))
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(CHATS).document(chatId).collection(MESSAGES)
            .orderBy(CAMPO_MARCA_TIEMPO, Query.Direction.DESCENDING)
            .limit(LIST_MENSAJES.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(RepoResult.Error(mapError(error)))
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                trySend(
                    RepoResult.Success(snapshot.documents.mapNotNull { it.aMessage() }),
                )
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(
        chatId: String,
        senderName: String,
        text: String,
        members: List<String>,
    ): RepoResult<String> {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val contenido = text.trim().takeIf { it.isNotEmpty() }
            ?: return RepoResult.Error(RepoError.EMPTY_FIELDS)
        if (chatId.isBlank()) return RepoResult.Error(RepoError.EMPTY_FIELDS)

        val ahora = System.currentTimeMillis()
        val chatRef = firestore.collection(CHATS).document(chatId)

        return try {
            // 1 · El mensaje: es lo que importa. Si esto falla, no se envió
            //      nada y el error se muestra tal cual.
            chatRef.collection(MESSAGES).document()
                .set(
                    Message(
                        senderId = uid,
                        senderName = senderName,
                        text = contenido,
                        timestamp = ahora,
                    ).aFirestoreMap(),
                )
                .await()

            // 2 · Vista previa y no leídos del resto: mejor esfuerzo. Si
            //      Firestore lo rechazara, el mensaje ya salió igual.
            try {
                val cambios = mutableMapOf<String, Any>(
                    "lastMessage" to contenido,
                    // Si el mensaje anterior era un aviso de sistema, vuelve a texto.
                    CAMPO_TIPO_MENSAJE to MessageKind.TEXT.name,
                    CAMPO_ACTUALIZADO to Timestamp(Date(ahora)),
                )
                members.filter { it.isNotBlank() && it != uid }.forEach {
                    cambios["$CAMPO_NO_LEIDOS.$it"] = FieldValue.increment(1)
                }
                chatRef.update(cambios).await()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Chat $chatId: mensaje enviado pero la vista previa no se actualizó", e)
            }

            RepoResult.Success(chatId)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "sendMessage falló en el chat $chatId", e)
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun markAsRead(chatId: String): RepoResult<Unit> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        // Deja en 0 mis no leídos y anota en qué momento lo abrí: con eso el
        // badge del Descubrir distingue un match nuevo de uno ya visto.
        firestore.collection(CHATS).document(chatId)
            .update(
                mapOf(
                    "$CAMPO_NO_LEIDOS.$uid" to 0,
                    "$CAMPO_ULTIMA_LECTURA.$uid" to Timestamp(Date(System.currentTimeMillis())),
                ),
            )
            .await()
        RepoResult.Success(Unit)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "markAsRead falló en el chat $chatId", e)
        RepoResult.Error(mapError(e))
    }

    override suspend fun hideChat(chatId: String): RepoResult<Unit> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        firestore.collection(CHATS).document(chatId)
            .update(CAMPO_OCULTOS, FieldValue.arrayUnion(uid))
            .await()
        RepoResult.Success(Unit)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "hideChat falló en el chat $chatId", e)
        RepoResult.Error(mapError(e))
    }

    override suspend fun setPinned(chatId: String, pinned: Boolean): RepoResult<Unit> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val transformacion = if (pinned) {
            FieldValue.arrayUnion(uid)
        } else {
            FieldValue.arrayRemove(uid)
        }
        firestore.collection(CHATS).document(chatId)
            .update(CAMPO_DESTACADOS, transformacion)
            .await()
        RepoResult.Success(Unit)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "setPinned falló en el chat $chatId", e)
        RepoResult.Error(mapError(e))
    }

    override suspend fun joinGroupChat(
        activity: ActivityItem,
        memberName: String,
    ): RepoResult<String> {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        // 1 · Entrar al chat ANTES del aviso: sin eso las reglas rechazan el
        //      mensaje porque aún no soy miembro.
        val chatId = when (val creado = ensureGroupChat(activity)) {
            is RepoResult.Error -> return creado
            is RepoResult.Success -> creado.data
        }
        // 2 · Aviso de sistema: cada cliente lo traduce al mostrarlo.
        return publicarAvisoDeSistema(
            chatId = chatId,
            kind = MessageKind.MEMBER_JOINED,
            targetUid = uid,
            targetName = memberName,
        )
    }

    override suspend fun leaveGroupChat(
        activityId: String,
        memberName: String,
    ): RepoResult<String> {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        if (activityId.isBlank()) return RepoResult.Error(RepoError.EMPTY_FIELDS)
        val chatId = ChatIds.group(activityId)

        // Si el chat todavía no existe (nadie entró nunca) no hay a quién
        // avisar: salir no es un error. Esta lectura cabe en las reglas porque
        // en este punto sigo siendo miembro.
        val existe = firestore.collection(CHATS).document(chatId).get().await()
        if (!existe.exists()) return RepoResult.Success(chatId)

        // 1 · El aviso MIENTRAS aún soy miembro (si no, las reglas lo niegan).
        val publicado = publicarAvisoDeSistema(
            chatId = chatId,
            kind = MessageKind.MEMBER_LEFT,
            targetUid = uid,
            targetName = memberName,
        )
        if (publicado is RepoResult.Error) return publicado

        // 2 · Salir de `members`: ya no veo ni escribo en esa conversación.
        try {
            firestore.collection(CHATS).document(chatId)
                .update(CAMPO_MIEMBROS, FieldValue.arrayRemove(uid))
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            // Mejor esfuerzo: el aviso ya está publicado; si esto falla, el
            // usuario sigue siendo miembro (puede salir de nuevo).
            Log.w(TAG, "No se pudo quitar a $uid de members en $chatId", e)
        }
        return RepoResult.Success(chatId)
    }

    /**
     * Aviso de sistema en un chat de grupo: se guarda estructurado (tipo +
     * a quién se refiere) y en `lastMessage` va el NOMBRE, no la frase, para
     * que cada cliente lo muestre en su idioma.
     */
    private suspend fun publicarAvisoDeSistema(
        chatId: String,
        kind: MessageKind,
        targetUid: String,
        targetName: String,
    ): RepoResult<String> {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        if (chatId.isBlank()) return RepoResult.Error(RepoError.EMPTY_FIELDS)
        val ahora = System.currentTimeMillis()
        val chatRef = firestore.collection(CHATS).document(chatId)

        return try {
            chatRef.collection(MESSAGES).document()
                .set(
                    Message(
                        senderId = uid,
                        senderName = targetName,
                        text = "",
                        timestamp = ahora,
                        kind = kind,
                        targetUid = targetUid,
                        targetName = targetName,
                    ).aFirestoreMap(),
                )
                .await()
            chatRef.update(
                mapOf(
                    "lastMessage" to targetName,
                    CAMPO_TIPO_MENSAJE to kind.name,
                    CAMPO_ACTUALIZADO to Timestamp(Date(ahora)),
                ),
            )
                .await()
            RepoResult.Success(chatId)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "publicarAvisoDeSistema falló en $chatId", e)
            RepoResult.Error(mapError(e))
        }
    }

    /**
     * Crea (o refresca) el chat directo **sin leerlo antes**: con
     * `firestore.rules` solo se pueden leer documentos donde ya soy miembro,
     * y leer uno que no existe devuelve PERMISSION_DENIED — que era justo el
     * error que salía al confirmar un match. El `set` con merge es
     * idempotente (el id determinístico evita duplicados) y solo toca campos
     * seguros: nunca `lastMessage`, `unreadCounts` ni `lastReadAt`.
     */
    override suspend fun ensureDirectChat(
        otherUid: String,
        otherName: String,
        otherPhotoUrl: String,
    ): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val destino = otherUid.takeIf { it.isNotBlank() && it != uid }
            ?: return RepoResult.Error(RepoError.EMPTY_FIELDS)

        val chatId = ChatIds.direct(uid, destino)
        firestore.collection(CHATS).document(chatId)
            .set(
                Chat(
                    id = chatId,
                    type = ChatType.DIRECT,
                    participants = listOf(uid, destino).sorted(),
                    title = otherName,
                    photoUrl = otherPhotoUrl,
                    updatedAt = System.currentTimeMillis(),
                ).aFirestoreMap(),
                SetOptions.merge(),
            )
            .await()
        RepoResult.Success(chatId)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    /**
     * Crea (o actualiza) el chat de grupo **sin lectura previa**, por el mismo
     * motivo que [ensureDirectChat]: leer un chat inexistente estaba vetado
     * por las reglas y el "unirse a la actividad" se caía con ese error.
     *
     * `arrayUnion` suma a los asistentes sin pisar a los que ya están, y al
     * no escribir `lastMessage`/`unreadCounts` reabrir el chat desde el detalle
     * de la actividad nunca borra el historial (y además cura chats antiguos
     * que no existían todavía).
     */
    override suspend fun ensureGroupChat(activity: ActivityItem): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val actividadId = activity.id.takeIf { it.isNotBlank() }
            ?: return RepoResult.Error(RepoError.EMPTY_FIELDS)

        val participantes = (activity.attendeeIds + activity.ownerId)
            .filter { it.isNotBlank() }
            .plus(uid)
            .distinct()

        val chatId = ChatIds.group(actividadId)
        firestore.collection(CHATS).document(chatId)
            .set(
                Chat(
                    id = chatId,
                    type = ChatType.GROUP,
                    participants = participantes,
                    activityId = actividadId,
                    title = activity.title,
                    photoUrl = activity.imageUri.orEmpty(),
                    updatedAt = System.currentTimeMillis(),
                ).aFirestoreMap(
                    miembros = FieldValue.arrayUnion(*participantes.toTypedArray()),
                ),
                SetOptions.merge(),
            )
            .await()
        RepoResult.Success(chatId)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    // ------------------------------------------------------------------
    // Listas en tiempo real
    // ------------------------------------------------------------------

    private fun chatsSnapshot(): Flow<RepoResult<List<Chat>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(RepoResult.Error(RepoError.PERMISSION))
            close()
            return@callbackFlow
        }
        val base = firestore.collection(CHATS).whereArrayContains(CAMPO_MIEMBROS, uid)
        var registration: ListenerRegistration? = null
        var sinIndice = false

        fun registrar(query: Query) {
            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!sinIndice &&
                        error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION
                    ) {
                        // Falta el índice compuesto: se reconsulta sin orderBy
                        // y el orden por updatedAt se hace aquí mismo.
                        sinIndice = true
                        registration?.remove()
                        registrar(base)
                        return@addSnapshotListener
                    }
                    trySend(RepoResult.Error(mapError(error)))
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                trySend(RepoResult.Success(ordenados(snapshot.documents.mapNotNull { it.aChat(uid) })))
            }
        }

        registrar(base.orderBy(CAMPO_ACTUALIZADO, Query.Direction.DESCENDING).limit(LIST_CHATS.toLong()))
        awaitClose { registration?.remove() }
    }

    /**
     * Los que "eliminé" salen de mi lista (siguen existiendo para los demás);
     * los destacados quedan arriba del todo y el resto por fecha, con tope.
     */
    private fun ordenados(chats: List<Chat>): List<Chat> = chats
        .filterNot { it.hidden }
        .sortedWith(
            compareByDescending<Chat> { it.pinned }
                .thenByDescending { it.updatedAt },
        )
        .take(LIST_CHATS)

    /**
     * El título/foto de un chat directo depende de quién lo mire (el creador
     * guardó al "otro" desde su perspectiva), así que cada cliente lo rellena
     * con los datos reales del otro participante.
     */
    private suspend fun enriquecer(chats: List<Chat>): List<Chat> {
        val uid = auth.currentUser?.uid ?: return chats
        return chats.map { chat ->
            if (chat.type != ChatType.DIRECT) chat
            else {
                val otro = chat.participants.firstOrNull { it != uid } ?: return@map chat
                val perfil = perfilDe(otro) ?: return@map chat
                chat.copy(
                    title = perfil.first.ifBlank { chat.title },
                    photoUrl = perfil.second.ifBlank { chat.photoUrl },
                )
            }
        }
    }

    private suspend fun perfilDe(uid: String): Pair<String, String>? {
        perfiles[uid]?.let { return it }
        return runCatching {
            val doc = firestore.collection(USERS).document(uid).get().await()
            val nombre = doc.getString("firstName")?.ifBlank { null }
                ?: doc.getString("username") ?: ""
            val datos = nombre to (doc.getString("profilePictureUrl") ?: "")
            if (nombre.isNotBlank()) perfiles[uid] = datos
            datos
        }.getOrNull()
    }

    private fun mapError(e: Exception): RepoError = when (e) {
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            -> RepoError.CONNECTION

            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            FirebaseFirestoreException.Code.UNAUTHENTICATED,
            -> RepoError.PERMISSION

            else -> RepoError.UNKNOWN
        }

        else -> RepoError.UNKNOWN
    }

    companion object {
        private const val TAG = "Gaply"
        private const val CHATS = "chats"
        private const val MESSAGES = "messages"
        private const val USERS = "users"
        private const val CAMPO_ACTUALIZADO = "updatedAt"
        private const val CAMPO_MARCA_TIEMPO = "timestamp"
        private const val CAMPO_NO_LEIDOS = "unreadCounts"

        /** Chats en la lista y mensajes visibles en una conversación. */
        private const val LIST_CHATS = 30
        private const val LIST_MENSAJES = 50
    }
}
