package com.gaply.app.domain.repository

import com.gaply.app.domain.model.AppRating
import com.gaply.app.domain.model.RepoResult

/**
 * Guarda y lee las calificaciones de la app (Módulo 2 · Calificar la aplicación).
 */
interface RatingsRepository {

    /** Devuelve la última calificación enviada por el usuario, o null si aún no ha dado ninguna. */
    suspend fun getRating(): RepoResult<AppRating?>

    /** Guarda la calificación (estrellas + motivos) y devuelve la calificación persistida. */
    suspend fun saveRating(rating: AppRating): RepoResult<AppRating>
}
