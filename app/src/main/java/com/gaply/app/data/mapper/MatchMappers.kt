package com.gaply.app.data.mapper

import com.gaply.app.domain.agent.FranjaHoraria
import com.gaply.app.domain.model.MatchAction
import com.gaply.app.domain.model.MatchEntry
import com.gaply.app.domain.model.MatchKind
import com.gaply.app.domain.model.MatchProfile
import com.gaply.app.domain.model.User
import com.google.firebase.Timestamp

/**
 * Mappers del Módulo 5 · Matches (mismo patrón que `UserMappers`): la
 * colección global `swipes` y el perfil del mazo.
 *
 * El documento de swipe vive en **`swipes/{swipeId}`** y su id es
 * `swipe_{fromUid}_{targetId}` (determinista: repetir el swipe sobrescribe).
 * Los campos `fromUid` / `toId` son los que validan las reglas de
 * `firestore.rules` (leer solo lo mío o lo dirigido a mí), así que no se
 * renombran sin tocar las reglas.
 */

/** Quién hizo el swipe (valida escritura y lectura en las reglas). */
const val CAMPO_DESDE = "fromUid"

/** Destino del swipe: uid del otro usuario o id de la actividad. */
const val CAMPO_HASTA = "toId"

fun MatchEntry.aFirestoreMap(desde: String): Map<String, Any> = mapOf(
    CAMPO_DESDE to desde,
    CAMPO_HASTA to targetId,
    "targetId" to targetId,
    "kind" to kind.name,
    "action" to action.name,
    "createdAt" to createdAt,
)

fun Map<String, Any?>.aMatchEntry(targetId: String): MatchEntry = MatchEntry(
    targetId = this["targetId"] as? String ?: targetId,
    kind = enumDe<MatchKind>(this["kind"], MatchKind.PROFILE),
    action = enumDe<MatchAction>(this["action"], MatchAction.PASS),
    createdAt = (this["createdAt"] as? Number)?.toLong() ?: 0L,
)

/**
 * Perfil de Firestore a carta del mazo: la edad sale de `birthDate`
 * ("dd/MM/yyyy") y queda en null si el campo está vacío o no tiene formato.
 *
 * @param huecosPropios huecos libres YA calculados a partir de `schedule`.
 */
fun User.aMatchProfile(huecosPropios: List<FranjaHoraria>): MatchProfile = MatchProfile(
    userId = userId,
    firstName = firstName,
    username = username,
    age = MatchProfile.edadDesde(birthDate),
    university = university,
    bio = bio,
    profilePictureUrl = profilePictureUrl,
    interests = interests,
    freeSlots = huecosPropios,
)

/** Timestamp de Firestore a millis (null -> 0). */
fun Any?.aMillis(): Long = (this as? Timestamp)?.toDate()?.time ?: (this as? Number)?.toLong() ?: 0L

/** Parseo tolerante de enums guardados como texto. */
private inline fun <reified T : Enum<T>> enumDe(valor: Any?, porDefecto: T): T {
    val texto = valor as? String ?: return porDefecto
    return enumValues<T>().firstOrNull { it.name == texto } ?: porDefecto
}
