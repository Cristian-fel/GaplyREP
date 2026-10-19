package com.gaply.app.domain.repository

import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.RepoResult

/**
 * Módulo de Actividades · Repositorio.
 *
 * Contrato para persistir las actividades publicadas: la implementación real
 * es [com.gaply.app.data.repository.FirebaseActivityRepository] (colección
 * `activities` en Firestore) y la de respaldo en memoria es
 * [com.gaply.app.data.repository.LocalActivityRepository].
 */
interface ActivityRepository {

    /** Lista pública de actividades (más reciente primero). */
    suspend fun getActivities(): RepoResult<List<ActivityItem>>

    /** Crea o actualiza una actividad (mismo id = set, se sobrescribe el documento). */
    suspend fun save(activity: ActivityItem): RepoResult<ActivityItem>

    /** Elimina una actividad por id. */
    suspend fun delete(activityId: String): RepoResult<String>

    /**
     * Une al usuario actual a la actividad: +1 uid en `attendeeIds` y +1 nombre
     * en `attendees` (solo la primera vez).
     */
    suspend fun join(activityId: String, attendeeName: String): RepoResult<String>

    /**
     * Sale de la actividad (deshace "Unirme"): quita el uid de `attendeeIds`
     * y el nombre de `attendees` con arrayRemove.
     */
    suspend fun leave(activityId: String, attendeeName: String): RepoResult<String>

    /**
     * Sube la foto del lugar a Storage (ruta `activities/{uid}/{activityId}.jpg`)
     * y devuelve su downloadUrl, que queda guardada en el documento y se ve
     * desde cualquier perfil.
     */
    suspend fun uploadPlacePhoto(activityId: String, localUri: String): RepoResult<String>

    /**
     * Fotos de perfil (`profilePictureUrl`) de los usuarios [uids], para
     * mostrar los avatares reales del organizador y los asistentes en el
     * detalle (la inicial es solo respaldo).
     */
    suspend fun getUserPhotos(uids: List<String>): RepoResult<Map<String, String>>
}
