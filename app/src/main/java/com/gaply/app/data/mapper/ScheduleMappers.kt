package com.gaply.app.data.mapper

import com.gaply.app.domain.model.ScheduleClass

/**
 * Lectura del mapa `schedule` ({ classId: { ...campos...} }) que vive en el
 * documento `users/{uid}`. Compartido por el repositorio de horario y por el
 * mazo de Matches (que deriva los huecos libres de otros usuarios).
 */
fun Any?.aScheduleClasses(): List<ScheduleClass> {
    val mapa = this as? Map<*, *> ?: return emptyList()
    return mapa.mapNotNull { (id, datos) ->
        val clave = id as? String ?: return@mapNotNull null
        (datos as? Map<*, *>)?.aScheduleClass(clave)
    }
}

fun Map<*, *>.aScheduleClass(id: String): ScheduleClass = ScheduleClass(
    id = id,
    this["title"] as? String ?: "",
    this["day"] as? String ?: "Lunes",
    this["startDate"] as? String ?: "",
    this["endDate"] as? String ?: "",
    this["startHour"] as? String ?: "08:00",
    this["endHour"] as? String ?: "10:00",
    this["note"] as? String ?: "",
    this["color"] as? String ?: ScheduleClass.COLOR_POR_DEFECTO,
)
