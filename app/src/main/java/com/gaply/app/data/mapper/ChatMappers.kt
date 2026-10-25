package com.gaply.app.data.mapper

import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.ChatType
import com.gaply.app.domain.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Mappers del Módulo 5 · Chats: `chats/{chatId}` y su subcolección
 * `messages` (mismo patrón que `UserMappers`).
 *
 * `unreadCount` NO viaja a Firestore: es el valor de `unreadCounts[miUid]`
 * que se calcula en cada lectura.
 */

/**
 * Campo de Firestore con los miembros del chat. **Es el nombre que exigen
 * `firestore.rules`**: las reglas validan `resource.data.members` y
 * `request.resource.data.members`, así que renombrarlo sin tocar las reglas
 * rompe lectura, escritura y mensajes. El modelo Kotlin lo llama
 * `participants` (nombre interno) y este es el único punto de traducción.
 */
const val CAMPO_MIEMBROS = "members"

/** Documento del chat listo para `set(..., SetOptions.merge())`. */
fun Chat.aFirestoreMap(incluirVacios: Boolean): Map<String, Any> = buildMap {
    put("type", type.name)
    put(CAMPO_MIEMBROS, participants)
    put("title", title)
    put("photoUrl", photoUrl)
    put("lastMessage", lastMessage)
    put("updatedAt", Timestamp(java.util.Date(updatedAt)))
    activityId?.let { put("activityId", it) }
    if (incluirVacios) put("unreadCounts", emptyMap<String, Int>())
}

fun DocumentSnapshot.aChat(miUid: String?): Chat {
    val datos = data ?: return Chat(id = id)
    val participantes = (datos[CAMPO_MIEMBROS] as? List<*>)?.filterIsInstance<String>().orEmpty()
    @Suppress("UNCHECKED_CAST")
    val noLeidos = datos["unreadCounts"] as? Map<String, Number>
    return Chat(
        id = id,
        type = tipoDe(datos["type"], ChatType.DIRECT),
        participants = participantes,
        activityId = datos["activityId"] as? String,
        title = datos["title"] as? String ?: "",
        photoUrl = datos["photoUrl"] as? String ?: "",
        lastMessage = datos["lastMessage"] as? String ?: "",
        updatedAt = datos["updatedAt"].aMillis(),
        unreadCount = if (miUid == null) 0 else (noLeidos?.get(miUid)?.toInt() ?: 0),
    )
}

fun Message.aFirestoreMap(): Map<String, Any> = mapOf(
    "senderId" to senderId,
    "senderName" to senderName,
    "text" to text,
    "timestamp" to Timestamp(java.util.Date(timestamp)),
)

fun DocumentSnapshot.aMessage(): Message = Message(
    id = id,
    senderId = getString("senderId") ?: "",
    senderName = getString("senderName") ?: "",
    text = getString("text") ?: "",
    timestamp = get("timestamp").aMillis(),
)

private inline fun <reified T : Enum<T>> tipoDe(valor: Any?, porDefecto: T): T {
    val texto = valor as? String ?: return porDefecto
    return enumValues<T>().firstOrNull { it.name == texto } ?: porDefecto
}
