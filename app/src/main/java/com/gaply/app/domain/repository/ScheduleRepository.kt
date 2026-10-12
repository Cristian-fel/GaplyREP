package com.gaply.app.domain.repository

import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.ScheduleClass

/** Guarda y lee las clases del horario del usuario (Módulo 2B · Mi horario). */
interface ScheduleRepository {

    suspend fun getClasses(): RepoResult<List<ScheduleClass>>

    suspend fun saveClass(scheduleClass: ScheduleClass): RepoResult<ScheduleClass>

    suspend fun deleteClass(classId: String): RepoResult<String>
}
