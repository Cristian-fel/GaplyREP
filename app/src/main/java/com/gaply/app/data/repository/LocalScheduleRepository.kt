package com.gaply.app.data.repository

import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.ScheduleClass
import com.gaply.app.domain.repository.ScheduleRepository
import java.util.UUID

/**
 * Respaldo del horario en memoria, para probar sin conexión a Firebase
 * (se usa cuando AppContainer.USE_FIREBASE es false).
 */
class LocalScheduleRepository : ScheduleRepository {

    private val clases = mutableListOf<ScheduleClass>()

    override suspend fun getClasses(): RepoResult<List<ScheduleClass>> =
        RepoResult.Success(clases.sortedBy { it.day })

    override suspend fun saveClass(scheduleClass: ScheduleClass): RepoResult<ScheduleClass> {
        val id = scheduleClass.id.ifBlank { UUID.randomUUID().toString() }
        val guardada = scheduleClass.copy(id = id)
        clases.removeAll { it.id == id }
        clases += guardada
        return RepoResult.Success(guardada)
    }

    override suspend fun deleteClass(classId: String): RepoResult<String> {
        clases.removeAll { it.id == classId }
        return RepoResult.Success(classId)
    }
}
