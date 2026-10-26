package com.gaply.app.data.repository

import com.gaply.app.data.mapper.CAMPO_DESDE
import com.gaply.app.data.mapper.CAMPO_HASTA
import com.gaply.app.data.mapper.aFirestoreMap
import com.gaply.app.data.mapper.aMatchEntry
import com.gaply.app.data.mapper.aMatchProfile
import com.gaply.app.data.mapper.aScheduleClasses
import com.gaply.app.data.mapper.toUser
import com.gaply.app.domain.agent.FranjaHoraria
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.MatchEntry
import com.gaply.app.domain.model.MatchProfile
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.model.huecosDelHorario
import com.gaply.app.domain.repository.MatchesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Módulo 5 · Matches en la nube.
 *
 * Cada swipe es un documento de la colección global **`swipes/{swipeId}`**
 * con `{ fromUid, toId, targetId, kind, action, createdAt }`:
 *  - id determinista `swipe_{fromUid}_{targetId}` (repetir sobrescribe),
 *  - escritura solo de quien hace el swipe (`fromUid == yo`, lo exige la
 *    regla de `firestore.rules`),
 *  - lectura de lo mío o de lo dirigido a mí: así el match mutuo se detecta
 *    en el cliente buscando `toId == yo && fromUid == otro`.
 *
 * El mazo se arma con `users` (`.limit(40)`): cada documento aporta el perfil
 * y su `schedule`, del que se derivan los huecos libres con la misma lógica de
 * "Mis huecos" (`huecosDelHorario`).
 */
class FirebaseMatchesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : MatchesRepository {

    override suspend fun getMyEntries(): RepoResult<List<MatchEntry>> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val snapshot = firestore.collection(SWIPES)
            .where(Filter.equalTo(CAMPO_DESDE, uid))
            .get()
            .await()
        RepoResult.Success(
            snapshot.documents.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                val datos = doc.data as? Map<String, Any?> ?: return@mapNotNull null
                datos.aMatchEntry(doc.getString(CAMPO_HASTA) ?: doc.id)
            },
        )
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun saveEntry(entry: MatchEntry): RepoResult<MatchEntry> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val guardada = entry.copy(
            createdAt = entry.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
        )
        // Id determinista + merge: repetir el mismo swipe sobrescribe y no
        // deja documentos huérfanos con otro id.
        firestore.collection(SWIPES)
            .document(swipeId(uid, guardada.targetId))
            .set(guardada.aFirestoreMap(desde = uid), SetOptions.merge())
            .await()
        RepoResult.Success(guardada)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun getDeckCandidates(limit: Int): RepoResult<List<MatchProfile>> = try {
        val snapshot = firestore.collection(USERS)
            .limit(limit.toLong())
            .get()
            .await()
        RepoResult.Success(
            snapshot.documents.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                val datos = doc.data as? Map<String, Any?> ?: return@mapNotNull null
                val usuario = datos.toUser(doc.id)
                if (usuario.userId.isBlank()) return@mapNotNull null
                val huecos = huecosDe(datos["schedule"])
                usuario.aMatchProfile(huecos)
            },
        )
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun hasMutualLike(otherUid: String): RepoResult<Boolean> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        // Solo los dos filtros de igualdad: el de `toId == yo` es el que
        // permite leer el swipe del otro según las reglas (sin índice compuesto).
        val snapshot = firestore.collection(SWIPES)
            .where(Filter.equalTo(CAMPO_HASTA, uid))
            .where(Filter.equalTo(CAMPO_DESDE, otherUid))
            .limit(1)
            .get()
            .await()
        val accion = snapshot.documents.firstOrNull()?.getString(CAMPO_ACCION)
        RepoResult.Success(accion == "LIKE" || accion == "SUPER")
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun getPendingLikeUids(): RepoResult<List<String>> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)

        // A quién ya respondí (para no volver a ofrecerlo): lectura permitida
        // por `fromUid == yo`, el primer desdichado de la regla de swipes.
        val respondidos = firestore.collection(SWIPES)
            .where(Filter.equalTo(CAMPO_DESDE, uid))
            .limit(LIST_SWIPES.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { it.getString(CAMPO_HASTA) }
            .toSet()

        // Quién me dio like: lectura permitida por `toId == yo` (2º desdichado).
        RepoResult.Success(
            firestore.collection(SWIPES)
                .where(Filter.equalTo(CAMPO_HASTA, uid))
                .limit(LIST_SWIPES.toLong())
                .get()
                .await()
                .documents
                .sortedByDescending { (it.get(CAMPO_CREADO_EN) as? Number)?.toLong() ?: 0L }
                .mapNotNull { doc ->
                    val desde = doc.getString(CAMPO_DESDE)
                    if (desde.isNullOrBlank() || desde == uid || desde in respondidos) {
                        null
                    } else {
                        desde
                    }
                }
                .distinct(),
        )
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun getUserProfile(uid: String): RepoResult<User?> = try {
        val objetivo = uid.ifBlank { return RepoResult.Error(RepoError.EMPTY_FIELDS) }
        val doc = firestore.collection(USERS).document(objetivo).get().await()
        RepoResult.Success(
            if (doc.exists()) {
                doc.data?.let { it.toUser(doc.id) }
            } else {
                null
            },
        )
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    /** `schedule` del documento -> huecos libres de la semana (misma regla que "Mis huecos"). */
    private fun huecosDe(schedule: Any?): List<FranjaHoraria> =
        huecosDelHorario(
            clases = schedule.aScheduleClasses(),
            minimoMinutos = HuecoLibre.MINIMO_MINUTOS,
        )

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
        /** Colección global de swipes (tal como la declara `firestore.rules`). */
        private const val SWIPES = "swipes"
        private const val USERS = "users"
        private const val CAMPO_ACCION = "action"
        private const val CAMPO_CREADO_EN = "createdAt"

        /** Tope de swipes leídos en cada consulta (son igualdades: sin índice). */
        private const val LIST_SWIPES = 200

        /** Id determinístico: repetir un swipe sobrescribe el mismo documento. */
        private fun swipeId(desde: String, targetId: String): String = "swipe_${desde}_$targetId"
    }
}
