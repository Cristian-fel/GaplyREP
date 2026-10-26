package com.gaply.app.data.repository

import com.gaply.app.data.local.LocalUserStore
import com.gaply.app.data.mapper.aMatchProfile
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.MatchAction
import com.gaply.app.domain.model.MatchEntry
import com.gaply.app.domain.model.MatchProfile
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.model.huecosDelHorario
import com.gaply.app.domain.repository.MatchesRepository
import com.gaply.app.domain.repository.ScheduleRepository

/**
 * Respaldo de Matches en memoria (se usa cuando AppContainer.USE_FIREBASE es
 * false), pensado para probar el mazo sin conexión.
 *
 * Las entradas se guardan por uid: en local caben varios usuarios en el mismo
 * dispositivo, y así un "Me gusta" de uno se puede detectar como match mutuo
 * cuando se entra con la otra cuenta (mismo criterio que en la nube).
 *
 * El horario local es único para el dispositivo, así que todos los candidatos
 * comparten los mismos huecos libres y el agente decide por intereses.
 */
class LocalMatchesRepository(
    private val store: LocalUserStore,
    private val scheduleRepository: ScheduleRepository,
) : MatchesRepository {

    /** uid -> (targetId -> entrada). */
    private val entradas = mutableMapOf<String, MutableMap<String, MatchEntry>>()

    private fun miUid(): String? = store.currentUser.value?.userId

    override suspend fun getMyEntries(): RepoResult<List<MatchEntry>> {
        val uid = miUid() ?: return RepoResult.Success(emptyList())
        return RepoResult.Success(entradas[uid]?.values?.toList().orEmpty())
    }

    override suspend fun saveEntry(entry: MatchEntry): RepoResult<MatchEntry> {
        val uid = miUid() ?: return RepoResult.Error(RepoError.PERMISSION)
        val guardada = entry.copy(
            createdAt = entry.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
        )
        // fromUid se guarda aquí (en la nube lo escribe el mapper): hace falta
        // para saber quién me dio like y aún no respondí.
        entradas.getOrPut(uid) { mutableMapOf() }[guardada.targetId] =
            guardada.copy(fromUid = uid)
        return RepoResult.Success(guardada)
    }

    override suspend fun getDeckCandidates(limit: Int): RepoResult<List<MatchProfile>> {
        val huecos = when (val clases = scheduleRepository.getClasses()) {
            is RepoResult.Success ->
                huecosDelHorario(clases.data, HuecoLibre.MINIMO_MINUTOS)

            is RepoResult.Error -> emptyList()
        }
        return RepoResult.Success(
            store.loadUsers()
                .filter { it.userId.isNotBlank() && it.userId != miUid() }
                .take(limit)
                .map { it.aMatchProfile(huecos) },
        )
    }

    override suspend fun hasMutualLike(otherUid: String): RepoResult<Boolean> {
        val uid = miUid() ?: return RepoResult.Error(RepoError.PERMISSION)
        val accion = entradas[otherUid]?.get(uid)?.action
        return RepoResult.Success(accion == MatchAction.LIKE || accion == MatchAction.SUPER)
    }

    override suspend fun getUserProfile(uid: String): RepoResult<User?> =
        RepoResult.Success(
            uid.ifBlank { null }?.let { id -> store.loadUsers().find { it.userId == id } },
        )

    override suspend fun getPendingLikeUids(): RepoResult<List<String>> {
        val uid = miUid() ?: return RepoResult.Success(emptyList())
        val respondidos = entradas[uid]?.values?.map { it.targetId }?.toSet() ?: emptySet()
        return RepoResult.Success(
            entradas.entries
                .filter { it.key != uid }
                .flatMap { (_, mapa) -> mapa.values }
                .filter { it.targetId == uid && it.fromUid.isNotBlank() && it.fromUid !in respondidos }
                .sortedByDescending { it.createdAt }
                .map { it.fromUid }
                .distinct(),
        )
    }
}
