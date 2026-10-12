package com.gaply.app.data.repository

import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.ScheduleClass
import com.gaply.app.domain.repository.ScheduleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Horario en la nube, guardado dentro del documento del usuario:
 * `users/{uid}` con el campo `schedule` (mapa { classId: { ...campos... } }).
 *
 * Se escribe con `SetOptions.merge()` para tocar unicamente ese campo y no
 * tocar el resto del perfil. Esta aqui y no en la subcoleccion
 * `users/{uid}/schedule` porque las reglas de Firestore del proyecto cubren el
 * documento `users/{uid}` (el registro y el login ya lo escriben) pero no sus
 * subcolecciones: ahi Firestore responde PERMISSION_DENIED y el guardado del
 * Modulo 2B se caia. Si algun dia se publican reglas para subcolecciones, se
 * pueden mover estas tres funciones al esquema anterior sin tocar la app.
 */
class FirebaseScheduleRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ScheduleRepository {

    override suspend fun getClasses(): RepoResult<List<ScheduleClass>> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val mapa = mapaHorario(uid)
        RepoResult.Success(
            mapa.mapNotNull { (id, datos) ->
                (datos as? Map<*, *>)?.let { aClase(id, it) }
            },
        )
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun saveClass(scheduleClass: ScheduleClass): RepoResult<ScheduleClass> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val id = scheduleClass.id.ifBlank { firestore.collection(USERS).document().id }
        val guardada = scheduleClass.copy(id = id)
        escribirHorario(uid, mapaHorario(uid) + (id to guardada.aMapa()))
        RepoResult.Success(guardada)
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun deleteClass(classId: String): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        // FieldValue.delete() sobre la ruta "schedule.<id>" es la única forma
        // garantizada de quitar la llave del mapa: reescribir el mapa sin ella
        // con SetOptions.merge() no la elimina (Firestore mezcla los mapas y la
        // clase reaparecía al recargar).
        firestore.collection(USERS).document(uid)
            .update(mapOf("$CAMPO_HORARIO.$classId" to FieldValue.delete()))
            .await()
        RepoResult.Success(classId)
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    /** Lee el mapa { classId: campos } del documento del usuario. */
    @Suppress("UNCHECKED_CAST")
    private suspend fun mapaHorario(uid: String): Map<String, Any> {
        val documento = firestore.collection(USERS).document(uid).get().await()
        return documento.get(CAMPO_HORARIO) as? Map<String, Any> ?: emptyMap()
    }

    /** Escribe solo el campo del horario; con merge el perfil queda intacto. */
    private suspend fun escribirHorario(uid: String, mapa: Map<String, Any>) {
        firestore.collection(USERS).document(uid)
            .set(mapOf(CAMPO_HORARIO to mapa), SetOptions.merge())
            .await()
    }

    private fun aClase(id: String, datos: Map<*, *>): ScheduleClass = ScheduleClass(
        id = id,
        title = datos["title"] as? String ?: "",
        day = datos["day"] as? String ?: "Lunes",
        startDate = datos["startDate"] as? String ?: "",
        endDate = datos["endDate"] as? String ?: "",
        startHour = datos["startHour"] as? String ?: "08:00",
        endHour = datos["endHour"] as? String ?: "10:00",
        note = datos["note"] as? String ?: "",
        color = datos["color"] as? String ?: ScheduleClass.COLOR_POR_DEFECTO,
    )

    private fun ScheduleClass.aMapa(): Map<String, Any> = mapOf(
        "title" to title,
        "day" to day,
        "startDate" to startDate,
        "endDate" to endDate,
        "startHour" to startHour,
        "endHour" to endHour,
        "note" to note,
        "color" to color,
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
        private const val CAMPO_HORARIO = "schedule"
    }
}
