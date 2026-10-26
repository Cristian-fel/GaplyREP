package com.gaply.app.domain.repository

import com.gaply.app.domain.model.MatchEntry
import com.gaply.app.domain.model.MatchProfile
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User

/**
 * Módulo 5 · Matches · Repositorio.
 *
 * Contrato para persistir los "swipes" del mazo: la implementación real es
 * [com.gaply.app.data.repository.FirebaseMatchesRepository] (colección global
 * `swipes/{swipe_{fromUid}_{targetId}}`) y la de respaldo en memoria es
 * [com.gaply.app.data.repository.LocalMatchesRepository].
 *
 * El match mutuo se resuelve 100% en el cliente: al dar LIKE se busca el
 * swipe que el otro me hizo (`toId == yo && fromUid == otro`) y si es
 * LIKE|SUPER ya hay chat.
 */
interface MatchesRepository {

    /** Mias entradas de swipe (sirven de exclusión del mazo y de mitad del match). */
    suspend fun getMyEntries(): RepoResult<List<MatchEntry>>

    /** Persiste la acción sobre una carta (id del doc = targetId, se sobrescribe). */
    suspend fun saveEntry(entry: MatchEntry): RepoResult<MatchEntry>

    /**
     * Candidatos crudos del mazo (el filtrado de "yo + ya swipeados" lo hace
     * la UI). Cada perfil ya trae sus `freeSlots` derivados de su `schedule`,
     * con la misma lógica de huecos que usa "Mis huecos".
     */
    suspend fun getDeckCandidates(limit: Int): RepoResult<List<MatchProfile>>

    /** true si [otherUid] ya me dio LIKE o SUPER (match mutuo). */
    suspend fun hasMutualLike(otherUid: String): RepoResult<Boolean>

    /** Perfil de otro usuario para la vista previa (foto, bio, intereses). */
    suspend fun getUserProfile(uid: String): RepoResult<User?>
}
