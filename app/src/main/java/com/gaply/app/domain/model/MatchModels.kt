package com.gaply.app.domain.model

import com.gaply.app.domain.agent.FranjaHoraria
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Qué se está valorando con el swipe: un perfil o una actividad ("Flash Activity"). */
enum class MatchKind { PROFILE, ACTIVITY }

/** Acción del swipe sobre la carta visible. */
enum class MatchAction { LIKE, PASS, SUPER }

/**
 * Swipe persistido en `swipes/{swipeId}` (id `swipe_{fromUid}_{targetId}`).
 *
 * Sirve a la vez como "exclusión" (lo que ya no debe volver a aparecer en el
 * mazo) y como la mitad del match mutuo: si a mí me dieron LIKE y yo también
 * di LIKE, ambos clientes detectan la coincidencia sin Cloud Functions.
 * `fromUid`/`toId` son los campos que exigen las reglas de `firestore.rules`.
 */
data class MatchEntry(
    val targetId: String = "",
    val kind: MatchKind = MatchKind.PROFILE,
    val action: MatchAction = MatchAction.PASS,
    val createdAt: Long = 0L,
)

/**
 * Candidato del mazo de Matches.
 *
 * `age` es null cuando `birthDate` está vacío o no respeta el formato del
 * registro ("dd/MM/yyyy"): en ese caso la tarjeta no muestra edad en vez de
 * inventarla.
 *
 * `freeSlots` son los huecos libres derivados de su horario, `commonFreeSlots`
 * la intersección con los míos y `utilityScore`/`reasons` lo que decide el
 * agente de matches (null mientras no se evalúe).
 */
data class MatchProfile(
    val userId: String = "",
    val firstName: String = "",
    val username: String = "",
    val age: Int? = null,
    val university: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val interests: List<String> = emptyList(),
    val freeSlots: List<FranjaHoraria> = emptyList(),
    val commonFreeSlots: List<FranjaHoraria> = emptyList(),
    val utilityScore: Int? = null,
    val reasons: List<String> = emptyList(),
) {
    val displayName: String
        get() = firstName.ifBlank { username }.ifBlank { userId }

    /** Minutos totales de hueco que tenemos en común. */
    val commonMinutes: Int get() = commonFreeSlots.sumOf { it.duracionMinutos }

    companion object {

        /** Fecha de nacimiento del registro ("15/03/2006") -> edad; null si no se puede. */
        fun edadDesde(birthDate: String, hoy: LocalDate = LocalDate.now()): Int? {
            val texto = birthDate.trim()
            if (texto.isEmpty()) return null
            val nacimiento = runCatching {
                LocalDate.parse(texto, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }.getOrNull() ?: return null
            if (nacimiento.isAfter(hoy)) return null
            return ChronoUnit.YEARS.between(nacimiento, hoy).toInt()
        }
    }
}
