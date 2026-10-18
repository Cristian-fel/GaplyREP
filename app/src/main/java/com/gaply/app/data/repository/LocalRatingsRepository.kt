package com.gaply.app.data.repository

import com.gaply.app.domain.model.AppRating
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.RatingsRepository
import java.util.UUID

/**
 * Respaldo de la calificación en memoria, para probar sin conexión a Firebase
 * (se usa cuando AppContainer.USE_FIREBASE es false).
 */
class LocalRatingsRepository : RatingsRepository {

    private var ultima: AppRating? = null

    override suspend fun getRating(): RepoResult<AppRating?> = RepoResult.Success(ultima)

    override suspend fun saveRating(rating: AppRating): RepoResult<AppRating> {
        val id = rating.id.ifBlank { UUID.randomUUID().toString() }
        val guardada = rating.copy(id = id, createdAt = System.currentTimeMillis())
        ultima = guardada
        return RepoResult.Success(guardada)
    }
}
