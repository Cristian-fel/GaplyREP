package com.gaply.app.data.repository

import com.gaply.app.data.mapper.toUser
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.ChatMessage
import com.gaply.app.domain.model.ChatRoom
import com.gaply.app.domain.model.FeedCandidates
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.SwipeAction
import com.gaply.app.domain.repository.MatchRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Matches y chats en la nube.
 *
 * Colecciones nuevas de Firestore:
 *  - `swipes`: un documento por gesto (✕/★/♥) con `fromUid`, `toType`
 *    ("user"/"activity"), `toId`, `action` ("pass"/"star"/"like") y `createdAt`.
 *  - `matches`: un documento por match mutuo con `uids` = [a, b] y `chatId`.
 *  - `chats`: sala de conversación con `members` (uids), `memberNames` /
 *    `memberPhotos` desnormalizados para pintar la lista sin leer perfiles,
 *    y `lastMessage` / `lastAt` del último mensaje.
 *    Los mensajes viven en la subcolección `chats/{chatId}/messages`.
 *
 * El chat entre dos personas usa id determinista `dm_<uidA>_<uidB>` (ordenado),
 * así un match simultáneo de los dos lados no crea dos chats duplicados.
 *
 * IMPORTANTE: requiere reglas de Firestore para `swipes`, `matches` y `chats`
 * (lectura autenticada, escritura del involucrado). Sin ellas Firestore
 * responde PERMISSION_DENIED y la pantalla muestra su estado de error.
 */
class FirebaseMatchRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : MatchRepository {

    /** Cartas del mazo: personas (menos yo y lo que ya vi) y actividades (menos mías/unidas/vistas). */
    override suspend fun getCandidates(): RepoResult<FeedCandidates> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)

        // Gestos ya hechos: no se vuelve a mostrar ninguna de esas cartas.
        val vistos = firestore.collection(SWIPES)
            .whereEqualTo(CAMPO_DE, uid)
            .get()
            .await()
            .documents
            .mapNotNull { it.getString(CAMPO_A) }
            .toSet()

        val gente = firestore.collection(USERS)
            .get()
            .await()
            .documents
            .filter { it.id != uid && it.id !in vistos }
            .mapNotNull { doc ->
                val datos = doc.data ?: return@mapNotNull null
                datos.toUser(doc.id)
            }

        val actividades = firestore.collection(ACTIVITIES)
            .get()
            .await()
            .documents
            .filter { it.id !in vistos }
            .mapNotNull { doc ->
                val datos = doc.data ?: return@mapNotNull null
                aActividad(doc.id, datos, uid)
            }
            .filter { !it.isJoined && !it.isCreatedByMe }

        RepoResult.Success(FeedCandidates(people = gente, activities = actividades))
    } catch (e: FirebaseFirestoreException) {
        RepoResult.Error(mapError(e))
    } catch (e: Exception) {
        RepoResult.Error(RepoError.UNKNOWN)
    }

    /**
     * Registra el gesto y, si es ♥/★ sobre una persona que ya me había dado ♥/★,
     * crea `matches` + `chats` (idempotente por id determinista).
     *
     * @return chatId si nació el match, cadena vacía si aún no es mutuo.
     */
    override suspend fun registerSwipe(
        targetId: String,
        action: SwipeAction,
        isPerson: Boolean,
    ): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)

        firestore.collection(SWIPES).add(
            mapOf(
                CAMPO_DE to uid,
                CAMPO_A to targetId,
                CAMPO_TIPO to (if (isPerson) "user" else "activity"),
                CAMPO_ACCION to action.name.lowercase(),
                CAMPO_CREADO_EN to System.currentTimeMillis(),
            ),
        ).await()

        val chatId = if (isPerson && action != SwipeAction.PASS) {
            // ¿La otra persona ya me había dado ♥/★? Entonces hay match.
            val reciprocos = firestore.collection(SWIPES)
                .whereEqualTo(CAMPO_DE, targetId)
                .whereEqualTo(CAMPO_A, uid)
                .whereEqualTo(CAMPO_TIPO, "user")
                .whereIn(CAMPO_ACCION, listOf(SwipeAction.LIKE.name.lowercase(), SwipeAction.STAR.name.lowercase()))
                .get()
                .await()
                .size() > 0

            if (reciprocos) crearChatMutuo(uid, targetId) else ""
        } else {
            ""
        }

        RepoResult.Success(chatId)
    } catch (e: FirebaseFirestoreException) {
        RepoResult.Error(mapError(e))
    } catch (e: Exception) {
        RepoResult.Error(RepoError.UNKNOWN)
    }

    /** Chats donde soy miembro, del más reciente al más viejo. */
    override suspend fun getChats(): RepoResult<List<ChatRoom>> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)

        val salas = firestore.collection(CHATS)
            .whereArrayContains(CAMPO_MIEMBROS, uid)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val datos = doc.data ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                ChatRoom(
                    id = doc.id,
                    kind = datos[CAMPO_TIPO_SALA] as? String ?: "dm",
                    groupTitle = datos[CAMPO_TITULO_GRUPO] as? String ?: "",
                    memberIds = (datos[CAMPO_MIEMBROS] as? List<*>)?.filterIsInstance<String>().orEmpty(),
                    memberNames = (datos[CAMPO_NOMBRES] as? Map<String, String>).orEmpty(),
                    memberPhotos = (datos[CAMPO_FOTOS] as? Map<String, String>).orEmpty(),
                    lastMessage = datos[CAMPO_ULTIMO_MSJ] as? String ?: "",
                    lastAt = (datos[CAMPO_ULTIMO_EN] as? Number)?.toLong() ?: 0L,
                )
            }
            .sortedByDescending { it.lastAt }

        RepoResult.Success(salas)
    } catch (e: FirebaseFirestoreException) {
        RepoResult.Error(mapError(e))
    } catch (e: Exception) {
        RepoResult.Error(RepoError.UNKNOWN)
    }

    /** Mensajes de la sala en vivo: cada cambio en Firestore re-emite la lista. */
    override fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registro = firestore.collection(CHATS).document(chatId)
            .collection(MESSAGES)
            .orderBy(CAMPO_CREADO_EN, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val mensajes = snapshot.documents.mapNotNull { doc ->
                    val datos = doc.data ?: return@mapNotNull null
                    ChatMessage(
                        id = doc.id,
                        senderId = datos[CAMPO_EMISOR] as? String ?: "",
                        senderName = datos[CAMPO_EMISOR_NOMBRE] as? String ?: "",
                        text = datos[CAMPO_TEXTO] as? String ?: "",
                        createdAt = (datos[CAMPO_CREADO_EN] as? Number)?.toLong() ?: 0L,
                    )
                }
                trySend(mensajes)
            }
        awaitClose { registro.remove() }
    }

    /** Escribe el mensaje y actualiza la vista previa (lastMessage/lastAt) de la sala. */
    override suspend fun sendMessage(chatId: String, text: String): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val ahora = System.currentTimeMillis()

        firestore.collection(CHATS).document(chatId)
            .collection(MESSAGES)
            .add(
                mapOf(
                    CAMPO_EMISOR to uid,
                    CAMPO_EMISOR_NOMBRE to nombrePropio(uid),
                    CAMPO_TEXTO to text,
                    CAMPO_CREADO_EN to ahora,
                ),
            )
            .await()

        firestore.collection(CHATS).document(chatId)
            .set(
                mapOf(
                    CAMPO_ULTIMO_MSJ to text,
                    CAMPO_ULTIMO_EN to ahora,
                    CAMPO_ULTIMO_EMISOR to uid,
                ),
                SetOptions.merge(),
            )
            .await()

        RepoResult.Success(chatId)
    } catch (e: FirebaseFirestoreException) {
        RepoResult.Error(mapError(e))
    } catch (e: Exception) {
        RepoResult.Error(RepoError.UNKNOWN)
    }

    /**
     * Chat grupal de la actividad: id fijo `act_<activityId>` (si ya existe
     * se reutiliza, nunca se duplica) con `members` sincronizado a la lista
     * de asistentes: [joined] = true agrega al usuario actual (arrayUnion),
     * false lo retira (arrayRemove, solo si la sala ya existe).
     */
    override suspend fun syncGroupChat(
        activityId: String,
        groupTitle: String,
        joined: Boolean,
    ): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val chatId = "act_$activityId"
        val sala = firestore.collection(CHATS).document(chatId)

        if (joined) {
            sala.set(
                mapOf(
                    CAMPO_TIPO_SALA to "group",
                    CAMPO_ACTIVITY_ID to activityId,
                    CAMPO_TITULO_GRUPO to groupTitle,
                    CAMPO_MIEMBROS to FieldValue.arrayUnion(uid),
                    CAMPO_ULTIMO_MSJ to "",
                    CAMPO_ULTIMO_EN to 0L,
                ),
                SetOptions.merge(),
            ).await()
        } else if (sala.get().await().exists()) {
            sala.set(
                mapOf(CAMPO_MIEMBROS to FieldValue.arrayRemove(uid)),
                SetOptions.merge(),
            ).await()
        }

        RepoResult.Success(chatId)
    } catch (e: FirebaseFirestoreException) {
        RepoResult.Error(mapError(e))
    } catch (e: Exception) {
        RepoResult.Error(RepoError.UNKNOWN)
    }

    // ---------------------------------------------------------------------
    // Internos
    // ---------------------------------------------------------------------

    private var uidNombreCache: String? = null
    private var nombreCache: String? = null

    /** Nombre visible del usuario actual para firmar mensajes (cacheado por uid). */
    private suspend fun nombrePropio(uid: String): String {
        if (uidNombreCache == uid && !nombreCache.isNullOrBlank()) return nombreCache!!
        val datos = firestore.collection(USERS).document(uid).get().await().data
        val nombre = listOfNotNull(
            (datos?.get("firstName") as? String)?.trim(),
            (datos?.get("lastName") as? String)?.trim(),
        ).filter { it.isNotEmpty() }.joinToString(" ")
            .ifBlank { auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore('@') ?: "" }
        uidNombreCache = uid
        nombreCache = nombre
        return nombre
    }

    /**
     * Crea (o reutiliza, por id determinista) la sala dm entre [yo] y [otro]
     * más su documento de `matches`. Devuelve el chatId.
     */
    private suspend fun crearChatMutuo(yo: String, otro: String): String {
        val chatId = "dm_${listOf(yo, otro).sorted().joinToString("_")}"

        // Nombres y fotos desnormalizados para pintar la lista de conversaciones.
        val perfiles = firestore.collection(USERS)
            .whereIn(FieldPath.documentId(), listOf(yo, otro))
            .get()
            .await()
            .documents
        val nombres = mutableMapOf<String, String>()
        val fotos = mutableMapOf<String, String>()
        for (doc in perfiles) {
            val datos = doc.data ?: continue
            val nombre = listOfNotNull(
                (datos["firstName"] as? String)?.trim(),
                (datos["lastName"] as? String)?.trim(),
            ).filter { it.isNotEmpty() }.joinToString(" ")
            if (nombre.isNotEmpty()) nombres[doc.id] = nombre
            (datos["profilePictureUrl"] as? String)?.takeIf { it.isNotBlank() }?.let { fotos[doc.id] = it }
        }

        firestore.collection(CHATS).document(chatId).set(
            mapOf(
                CAMPO_MIEMBROS to listOf(yo, otro),
                CAMPO_NOMBRES to nombres,
                CAMPO_FOTOS to fotos,
                CAMPO_CREADO_EN to System.currentTimeMillis(),
                CAMPO_ULTIMO_MSJ to "",
                CAMPO_ULTIMO_EN to 0L,
            ),
            SetOptions.merge(), // no pisa lastMessage si el chat ya existía
        ).await()

        firestore.collection(MATCHES).document(chatId).set(
            mapOf(
                CAMPO_MIEMBROS to listOf(yo, otro),
                CAMPO_CHAT_ID to chatId,
                CAMPO_CREADO_EN to System.currentTimeMillis(),
            ),
        ).await()

        return chatId
    }

    /** Mapea un documento de `activities` a [ActivityItem] (isJoined/isCreatedByMe calculados con [uid]). */
    private fun aActividad(
        id: String,
        datos: Map<String, Any?>,
        uid: String,
    ): ActivityItem {
        val attendeeIds = (datos["attendeeIds"] as? List<*>)?.filterIsInstance<String>().orEmpty()
        val ownerId = datos["ownerId"] as? String ?: ""
        return ActivityItem(
            id = id,
            title = datos["title"] as? String ?: "",
            category = datos["category"] as? String ?: "",
            duration = datos["duration"] as? String ?: "",
            date = datos["date"] as? String ?: "",
            timeRange = datos["timeRange"] as? String ?: "",
            location = datos["location"] as? String ?: "",
            description = datos["description"] as? String ?: "",
            imageUri = datos["imageUri"] as? String,
            defaultColorHex = datos["defaultColorHex"] as? String ?: ActivityItem.DEFAULT_COLOR_HEX,
            organizerInitial = datos["organizerInitial"] as? String ?: "T",
            attendees = (datos["attendees"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
            attendeeIds = attendeeIds,
            ownerId = ownerId,
            ownerName = datos["ownerName"] as? String ?: "",
            createdAt = (datos["createdAt"] as? Number)?.toLong() ?: 0L,
            isJoined = uid in attendeeIds,
            isCreatedByMe = uid == ownerId,
            startTime = datos["startTime"] as? String ?: "12:00",
        )
    }

    private fun mapError(e: FirebaseFirestoreException): RepoError = when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> RepoError.PERMISSION
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
        -> RepoError.CONNECTION
        else -> RepoError.UNKNOWN
    }

    private companion object {
        private const val SWIPES = "swipes"
        private const val MATCHES = "matches"
        private const val CHATS = "chats"
        private const val MESSAGES = "messages"
        private const val USERS = "users"
        private const val ACTIVITIES = "activities"

        private const val CAMPO_DE = "fromUid"
        private const val CAMPO_A = "toId"
        private const val CAMPO_TIPO = "toType"
        private const val CAMPO_ACCION = "action"
        private const val CAMPO_CREADO_EN = "createdAt"
        private const val CAMPO_MIEMBROS = "members"
        private const val CAMPO_NOMBRES = "memberNames"
        private const val CAMPO_FOTOS = "memberPhotos"
        private const val CAMPO_ULTIMO_MSJ = "lastMessage"
        private const val CAMPO_ULTIMO_EN = "lastAt"
        private const val CAMPO_ULTIMO_EMISOR = "lastSenderId"
        private const val CAMPO_EMISOR = "senderId"
        private const val CAMPO_EMISOR_NOMBRE = "senderName"
        private const val CAMPO_TEXTO = "text"
        private const val CAMPO_CHAT_ID = "chatId"
        private const val CAMPO_TIPO_SALA = "kind"
        private const val CAMPO_TITULO_GRUPO = "groupTitle"
        private const val CAMPO_ACTIVITY_ID = "activityId"
    }
}
