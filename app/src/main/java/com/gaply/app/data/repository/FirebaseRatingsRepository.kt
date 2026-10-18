package com.gaply.app.data.repository

import com.gaply.app.domain.model.AppRating
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.RatingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Calificación de la app en la nube, guardada dentro del documento del usuario:
 * `users/{uid}` con el campo `rating` ({ stars, comment, createdAt, id }).
 *
 * Al igual que el horario, se escribe con `SetOptions.merge()` para tocar solo
 * ese campo sin borrar el resto del perfil, y vive en `users/{uid}` (y no en una
 * subcolección) porque las reglas de Firestore del proyecto cubren ese documento.
 */
class FirebaseRatingsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : RatingsRepository {

    override suspend fun getRating(): RepoResult<AppRating?> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val documento = firestore.collection(USERS).document(uid).get().await()
        RepoResult.Success(aCalificacion(documento.get(CAMPO_RATING) as? Map<*, *>))
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun saveRating(rating: AppRating): RepoResult<AppRating> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val id = rating.id.ifBlank { firestore.collection(USERS).document().id }
        val guardada = rating.copy(id = id, createdAt = System.currentTimeMillis())
        firestore.collection(USERS).document(uid)
            .set(mapOf(CAMPO_RATING to guardada.aMapa()), SetOptions.merge())
            .await()
        RepoResult.Success(guardada)
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    private fun aCalificacion(datos: Map<*, *>?): AppRating? = datos?.let {
        AppRating(
            id = it["id"] as? String ?: "",
            stars = (it["stars"] as? Number)?.toInt() ?: 0,
            comment = it["comment"] as? String ?: "",
            createdAt = (it["createdAt"] as? Number)?.toLong() ?: 0L,
        )
    }

    private fun AppRating.aMapa(): Map<String, Any> = mapOf(
        "id" to id,
        "stars" to stars,
        "comment" to comment,
        "createdAt" to createdAt,
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
        private const val USERS = "users"
        private const val CAMPO_RATING = "rating"
    }
}
