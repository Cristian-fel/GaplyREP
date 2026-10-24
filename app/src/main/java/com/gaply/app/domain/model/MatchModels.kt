package com.gaply.app.domain.model

/**
 * Módulo Matches · Modelos del mazo de cartas y de las conversaciones.
 *
 * El mazo (deck) alterna dos tipos de carta —[FeedItem.Person] y
 * [FeedItem.Activity]— y cada gesto del usuario se registra con
 * [SwipeAction] en la colección `swipes` de Firestore.
 */

/** Acción del usuario sobre una carta (los tres botones de abajo). */
enum class SwipeAction {
    /** ✕ Descartar: la carta no vuelve a mostrarse. */
    PASS,

    /** ★ Me interesa: se guarda; si es recíproco con una persona, hay match. */
    STAR,

    /** ♥ Me gusta: si es recíproco con una persona, nace el chat; en una actividad, se une. */
    LIKE,
}

/** Carta del mazo: una persona del campus o una actividad creada por la comunidad. */
sealed class FeedItem {
    abstract val id: String

    /** Perfil de otra persona (colección `users`). */
    data class Person(val user: User) : FeedItem() {
        override val id: String get() = user.userId
    }

    /** Actividad pública (colección `activities`), excluida si ya es mía o me uní. */
    data class Activity(val activity: ActivityItem) : FeedItem() {
        override val id: String get() = activity.id
    }
}

/**
 * Candidatos del mazo ya filtrados en el repositorio: sin mí, sin lo que ya
 * pasé/guardé/uní. La UI los mezcla alternando persona–actividad.
 */
data class FeedCandidates(
    val people: List<User> = emptyList(),
    val activities: List<ActivityItem> = emptyList(),
)

/**
 * Sala de conversación. `kind` = "dm" (dos personas, id determinista
 * `dm_<uidA>_<uidB>`) o "group" (chat de una actividad, id `act_<activityId>`
 * y `groupTitle` = título del evento).
 *
 * `memberNames` / `memberPhotos` se desnormalizan aquí para que la lista
 * de conversaciones no tenga que leer cada perfil en cada carga
 * (en grupo no se usan: el título lleva el nombre de la actividad).
 */
data class ChatRoom(
    val id: String = "",
    val kind: String = "dm",
    val groupTitle: String = "",
    val memberIds: List<String> = emptyList(),
    val memberNames: Map<String, String> = emptyMap(),
    val memberPhotos: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastAt: Long = 0L,
)

/** Mensaje de una sala (subcolección `messages` dentro del chat). */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAt: Long = 0L,
)
