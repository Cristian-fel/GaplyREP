package com.gaply.app.domain.model

/** Conversación de Gaply: directa (match) o de grupo (actividad). */
enum class ChatType { DIRECT, GROUP }

/**
 * Documento `chats/{chatId}`.
 *
 * @param id        `direct_{uidA}_{uidB}` (uids ordenados) o `act_{activityId}`.
 * @param activityId solo para [ChatType.GROUP].
 * @param title     nombre mostrado (el otro usuario o el título de la actividad).
 * @param unreadCount no viaja a Firestore: se calcula al leer `unreadCounts[miUid]`.
 * @param lastReadAt  idem: `lastReadAt[miUid]` (millis de la última vez que lo
 *                    abrí). Null = nunca lo abrí, que es lo que cuenta un chat
 *                    directo como "match nuevo" en el badge del Descubrir.
 * @param pinned      idem: `pinnedBy` contiene mi uid → el chat se queda arriba
 *                    del todo aunque otros tengan mensajes más recientes.
 * @param hidden      idem: `hiddenFor` contiene mi uid → "eliminar chat" solo
 *                    lo saca de MI lista (los demás y los mensajes siguen igual).
 * @param lastMessageKind para el último mensaje: TEXT (normal) o un aviso de
 *                    sistema; así la lista lo traduce en el idioma de quien
 *                    lo mira en lugar de guardar la frase ya escrita.
 */
data class Chat(
    val id: String = "",
    val type: ChatType = ChatType.DIRECT,
    val participants: List<String> = emptyList(),
    val activityId: String? = null,
    val title: String = "",
    val photoUrl: String = "",
    val lastMessage: String = "",
    val lastMessageKind: MessageKind = MessageKind.TEXT,
    val updatedAt: Long = 0L,
    val unreadCount: Int = 0,
    val lastReadAt: Long? = null,
    val pinned: Boolean = false,
    val hidden: Boolean = false,
) {
    /** Un match recién creado al que todavía no entré (alimenta el badge). */
    val esMatchNuevo: Boolean
        get() = type == ChatType.DIRECT && lastReadAt == null
}

/** Clase de un mensaje: texto normal o un aviso de sistema del grupo. */
enum class MessageKind { TEXT, MEMBER_JOINED, MEMBER_LEFT }

/** Mensaje de texto de `chats/{chatId}/messages/{messageId}`. */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    /** Solo TEXT es conversación; los demás son avisos de entrada/salida. */
    val kind: MessageKind = MessageKind.TEXT,
    /** A quién se refiere el aviso de sistema (uid y nombre visibles). */
    val targetUid: String = "",
    val targetName: String = "",
) {
    /** true si lo escribí yo (la UI pinta distinto mis burbujas). */
    fun esMio(miUid: String): Boolean = senderId.isNotBlank() && senderId == miUid

    /** true si es un aviso de sistema (se pinta centrado y es tocable). */
    fun esAvisoDeSistema(): Boolean = kind != MessageKind.TEXT
}

/** Convenciones de id compartidas por los repositorios de chats y actividades. */
object ChatIds {

    /** Id determinístico de un chat directo: sin importar quién lo cree, ambos ven el mismo. */
    fun direct(uidA: String, uidB: String): String {
        val (primero, segundo) = listOf(uidA, uidB).sorted()
        return "direct_${primero}_${segundo}"
    }

    /** Id determinístico del chat de grupo de una actividad. */
    fun group(activityId: String): String = "act_$activityId"
}
