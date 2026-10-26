package com.gaply.app.data.mapper

import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.ChatType
import com.gaply.app.domain.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

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

/**
 * Última vez que cada uid abrió la conversación (`{ uid: millis }`). Solo lo
 * escribe el propio dueño al abrir el chat; con eso el badge del Descubrir
 * distingue un match nuevo de uno ya visto. Escritura permitida porque
 * `firestore.rules` deja actualizar `chats` a sus miembros.
 */
const val CAMPO_ULTIMA_LECTURA = "lastReadAt"

/** uids que destacaron el chat (`pinnedBy`) o lo quitaron de su lista (`hiddenFor`). */
const val CAMPO_DESTACADOS = "pinnedBy"
const val CAMPO_OCULTOS = "hiddenFor"

/**
 * Campos del chat para `set(..., SetOptions.merge())`.
 *
 * **Nunca** incluye `lastMessage`, `unreadCounts` ni `lastReadAt`: esos campos
 * solo los tocan quien escribe un mensaje o quien abre la conversación, así
 * que "asegurar" un chat (unirse o reabrirlo) jamás borra el historial. Si el
 * documento no existía, esos campos quedan ausentes y los mapeadores leen los
 * valores por defecto ("" y 0).
 *
 * @param miembros valor del campo `members`: la lista completa (chats directos)
 * o `FieldValue.arrayUnion(...)` para sumar asistentes sin pisar a los que ya
 * están (chats de grupo).
 */
fun Chat.aFirestoreMap(miembros: Any = participants): Map<String, Any> = buildMap {
    put("type", type.name)
    put(CAMPO_MIEMBROS, miembros)
    put("title", title)
    put("photoUrl", photoUrl)
    put("updatedAt", Timestamp(java.util.Date(updatedAt)))
    activityId?.let { put("activityId", it) }
}

fun DocumentSnapshot.aChat(miUid: String?): Chat {
    val datos = data ?: return Chat(id = id)
    val participantes = (datos[CAMPO_MIEMBROS] as? List<*>)?.filterIsInstance<String>().orEmpty()
    @Suppress("UNCHECKED_CAST")
    val noLeidos = datos["unreadCounts"] as? Map<String, Number>
    @Suppress("UNCHECKED_CAST")
    val ultimaLectura = datos[CAMPO_ULTIMA_LECTURA] as? Map<String, Any?>
    @Suppress("UNCHECKED_CAST")
    val destacados = datos[CAMPO_DESTACADOS] as? List<*>
    @Suppress("UNCHECKED_CAST")
    val ocultos = datos[CAMPO_OCULTOS] as? List<*>
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
        lastReadAt = if (miUid == null) {
            null
        } else {
            ultimaLectura?.get(miUid)?.aMillis()?.takeIf { it > 0L }
        },
        pinned = miUid != null && destacados?.contains(miUid) == true,
        hidden = miUid != null && ocultos?.contains(miUid) == true,
    )
}

/**
 * El timestamp lo pone el **servidor**: así el orden de la conversación nunca
 * depende del reloj de cada dispositivo (con `System.currentTimeMillis()` un
 * reloj atrasado metía el mensaje más nuevo arriba del más antiguo). Al
 * leerlo, Firestore devuelve una estimación mientras confirma.
 */
fun Message.aFirestoreMap(): Map<String, Any> = mapOf(
    "senderId" to senderId,
    "senderName" to senderName,
    "text" to text,
    "timestamp" to FieldValue.serverTimestamp(),
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
