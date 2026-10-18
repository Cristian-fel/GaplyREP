package com.gaply.app.data.repository

import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.ActivityRepository
import java.util.UUID

/**
 * Respaldo de las actividades en memoria, para probar sin conexión a Firebase
 * (se usa cuando AppContainer.USE_FIREBASE es false).
 */
class LocalActivityRepository : ActivityRepository {

    private val actividades = mutableListOf<ActivityItem>()

    override suspend fun getActivities(): RepoResult<List<ActivityItem>> =
        RepoResult.Success(actividades.sortedByDescending { it.createdAt })

    override suspend fun save(activity: ActivityItem): RepoResult<ActivityItem> {
        val id = activity.id.ifBlank { UUID.randomUUID().toString() }
        val guardada = activity.copy(
            id = id,
            createdAt = activity.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
        )
        actividades.removeAll { it.id == id }
        actividades += guardada
        return RepoResult.Success(guardada)
    }

    override suspend fun delete(activityId: String): RepoResult<String> {
        actividades.removeAll { it.id == activityId }
        return RepoResult.Success(activityId)
    }

    override suspend fun join(activityId: String, attendeeName: String): RepoResult<String> {
        val uid = "local-user"
        val indice = actividades.indexOfFirst { it.id == activityId }
        if (indice >= 0) {
            val actual = actividades[indice]
            actividades[indice] = actual.copy(
                attendeeIds = (actual.attendeeIds + uid).distinct(),
                attendees = (actual.attendees + attendeeName).distinct(),
                isJoined = true,
            )
        }
        return RepoResult.Success(activityId)
    }
}
