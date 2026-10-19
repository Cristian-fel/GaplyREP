package com.gaply.app.data.repository

import android.net.Uri
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.ActivityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Actividades en la nube: colección `activities` de Firestore, un documento
 * por actividad (id del documento = id de la actividad).
 *
 * Campos de control guardados junto a los datos del formulario:
 *  - `ownerId` / `ownerName`: quién la publicó (define "Tus actividades" y
 *    quién puede borrarla).
 *  - `attendeeIds`: uids de quien se unió (`isJoined` se calcula al leer).
 *  - `attendees`: nombres visibles de los asistentes.
 *  - `createdAt`: millis de publicación, para ordenar más reciente primero.
 *
 * `isJoined` y `isCreatedByMe` NO se guardan: se calculan en cada lectura con
 * el uid actual, así cada usuario ve su propia relación con la actividad.
 *
 * IMPORTANTE: requiere reglas de Firestore para la colección `activities`
 * (lectura autenticada, escritura del dueño). Sin ellas Firestore responde
 * PERMISSION_DENIED y la app muestra el error en pantalla.
 */
class FirebaseActivityRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ActivityRepository {

    override suspend fun getActivities(): RepoResult<List<ActivityItem>> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val snapshot = firestore.collection(ACTIVITIES)
            .orderBy(CAMPO_CREADO_EN, Query.Direction.DESCENDING)
            .get()
            .await()
        RepoResult.Success(
            snapshot.documents.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                val datos = doc.data as? Map<String, Any?> ?: return@mapNotNull null
                aActividad(doc.id, datos, uid)
            },
        )
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun save(activity: ActivityItem): RepoResult<ActivityItem> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        val id = activity.id.ifBlank { firestore.collection(ACTIVITIES).document().id }
        // El dueño se fija aquí (no en la UI): al editar una actividad ajena
        // no se puede "robar" cambiando ownerId desde el formulario.
        val guardada = activity.copy(
            id = id,
            ownerId = activity.ownerId.ifBlank { uid },
            ownerName = activity.ownerName.ifBlank { nombreDe(uid) },
            createdAt = activity.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
        )
        firestore.collection(ACTIVITIES).document(id)
            .set(guardada.aMapa(uid))
            .await()
        RepoResult.Success(guardada)
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun delete(activityId: String): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: RepoResult.Error(RepoError.PERMISSION)
        // Borrado en dos pasos para que las reglas puedan exigir ser el dueño.
        val doc = firestore.collection(ACTIVITIES).document(activityId).get().await()
        if (doc.getString(CAMPO_DUE_NO_ID) != uid) {
            RepoResult.Error(RepoError.PERMISSION)
        } else {
            firestore.collection(ACTIVITIES).document(activityId).delete().await()
            RepoResult.Success(activityId)
        }
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun join(activityId: String, attendeeName: String): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        // arrayUnion es idempotente: repetir "Unirme" no duplica ni el uid ni el nombre.
        firestore.collection(ACTIVITIES).document(activityId)
            .update(
                mapOf(
                    CAMPO_ASISTENTES_UIDS to FieldValue.arrayUnion(uid),
                    CAMPO_ASISTENTES_NOMBRES to FieldValue.arrayUnion(attendeeName),
                ),
            )
            .await()
        RepoResult.Success(activityId)
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun leave(activityId: String, attendeeName: String): RepoResult<String> {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        return try {
            // arrayRemove es la operación simétrica a arrayUnion: quita el uid
            // (siempre único) y el nombre del usuario que se va.
            firestore.collection(ACTIVITIES).document(activityId)
                .update(
                    mapOf(
                        CAMPO_ASISTENTES_UIDS to FieldValue.arrayRemove(uid),
                        CAMPO_ASISTENTES_NOMBRES to FieldValue.arrayRemove(attendeeName),
                    ),
                )
                .await()
            RepoResult.Success(activityId)
        } catch (e: Exception) {
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun uploadPlacePhoto(
        activityId: String,
        localUri: String,
    ): RepoResult<String> = try {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.PERMISSION)
        // Misma lógica que la foto de perfil: putFile + downloadUrl. La ruta
        // activities/{uid}/... coincide con las reglas de Storage del proyecto
        // (lectura para autenticados, escritura para el dueño).
        val ref = storage.reference.child("$ACTIVITIES/$uid/$activityId.jpg")
        ref.putFile(Uri.parse(localUri)).await()
        RepoResult.Success(ref.downloadUrl.await().toString())
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override suspend fun getUserPhotos(uids: List<String>): RepoResult<Map<String, String>> =
        try {
            val pedidos = uids.filter { it.isNotBlank() }.distinct().take(FOTOS_POR_LOTE)
            if (pedidos.isEmpty()) {
                RepoResult.Success(emptyMap())
            } else {
                // La foto de perfil vive en el documento del usuario:
                // `users/{uid}.profilePictureUrl` (mismo dato que usan Home y Perfil).
                val snapshot = firestore.collection(USERS)
                    .whereIn(FieldPath.documentId(), pedidos)
                    .get()
                    .await()
                RepoResult.Success(
                    snapshot.documents.mapNotNull { doc ->
                        doc.getString(CAMPO_FOTO_PERFIL)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { doc.id to it }
                    }.toMap(),
                )
            }
        } catch (e: Exception) {
            RepoResult.Error(mapError(e))
        }

    /** Nombre visible del usuario autenticado (para attendeeNames). */
    private fun nombreDe(uid: String): String =
        auth.currentUser?.displayName?.ifBlank { null }
            ?: auth.currentUser?.email?.substringBefore('@')
            ?: uid.take(8)

    private fun aActividad(id: String, datos: Map<String, Any?>, uid: String): ActivityItem {
        val uids = (datos[CAMPO_ASISTENTES_UIDS] as? List<*>)?.filterIsInstance<String>().orEmpty()
        val ownerId = datos[CAMPO_DUE_NO_ID] as? String ?: ""
        return ActivityItem(
            id = id,
            title = datos["title"] as? String ?: "",
            category = datos["category"] as? String ?: "",
            duration = datos["duration"] as? String ?: "",
            date = datos["date"] as? String ?: "",
            timeRange = datos["timeRange"] as? String ?: "",
            location = datos["location"] as? String ?: "",
            description = datos["description"] as? String ?: "",
            imageUri = datos["imageUri"] as? String,
            defaultColorHex = datos["defaultColorHex"] as? String
                ?: ActivityItem.DEFAULT_COLOR_HEX,
            organizerInitial = datos["organizerInitial"] as? String ?: "T",
            attendees = (datos[CAMPO_ASISTENTES_NOMBRES] as? List<*>)
                ?.filterIsInstance<String>().orEmpty(),
            attendeeIds = uids,
            ownerId = ownerId,
            ownerName = datos[CAMPO_DUE_NO_NOMBRE] as? String ?: "",
            createdAt = (datos[CAMPO_CREADO_EN] as? Number)?.toLong() ?: 0L,
            isJoined = uid in uids,
            isCreatedByMe = ownerId.isNotBlank() && ownerId == uid,
            startTime = datos["startTime"] as? String ?: "12:00",
        )
    }

    private fun ActivityItem.aMapa(uidActual: String): Map<String, Any?> = mapOf(
        "title" to title,
        "category" to category,
        "duration" to duration,
        "date" to date,
        "timeRange" to timeRange,
        "startTime" to startTime,
        "location" to location,
        "description" to description,
        "imageUri" to imageUri,
        "defaultColorHex" to defaultColorHex,
        "organizerInitial" to organizerInitial,
        CAMPO_DUE_NO_ID to ownerId.ifBlank { uidActual },
        CAMPO_DUE_NO_NOMBRE to ownerName,
        CAMPO_ASISTENTES_NOMBRES to attendees,
        CAMPO_ASISTENTES_UIDS to attendeeIds,
        CAMPO_CREADO_EN to (createdAt.takeIf { it > 0L } ?: System.currentTimeMillis()),
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
        private const val ACTIVITIES = "activities"
        private const val USERS = "users"
        private const val CAMPO_FOTO_PERFIL = "profilePictureUrl"
        /** Límite del whereIn de Firestore al pedir varias fotos de perfil. */
        private const val FOTOS_POR_LOTE = 10
        private const val CAMPO_DUE_NO_ID = "ownerId"
        private const val CAMPO_DUE_NO_NOMBRE = "ownerName"
        private const val CAMPO_ASISTENTES_UIDS = "attendeeIds"
        private const val CAMPO_ASISTENTES_NOMBRES = "attendees"
        private const val CAMPO_CREADO_EN = "createdAt"
    }
}
