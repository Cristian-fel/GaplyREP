package com.gaply.app.domain.agent

/**
 * Candidato que el agente evalúa.
 *
 * Por ahora nace del catálogo de actividades que la app ya muestra en la
 * pestaña Descubrir; en la fase siguiente se cargará desde Firestore sin
 * cambiar esta forma, porque el agente solo recibe una lista de [Actividad].
 */
data class Actividad(
    val id: String,
    val nombre: String,
    val intereses: List<String> = emptyList(),
    val franja: FranjaHoraria,
    val descripcion: String = "",
)
