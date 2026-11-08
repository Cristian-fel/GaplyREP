package com.gaply.app.data

/**
 * Punto principal del campus con sus coordenadas aproximadas.
 *
 * @param name Nombre visible del lugar, ej. "Biblioteca".
 * @param latitude Latitud del punto (WGS84, aproximada).
 * @param longitude Longitud del punto (WGS84, aproximada).
 */
data class CampusPoint(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

/**
 * Módulo de Actividades · Catálogo de coordenadas del campus.
 *
 * Puntos principales de la Universidad Jorge Tadeo Lozano (Campus
 * Chapinero, Bogotá, Cl. 79B #12-18). Las actividades guardan el nombre
 * del punto elegido junto a su latitud/longitud, de modo que el futuro
 * Radar AR pueda apuntar directo a la zona sin texto libre.
 *
 * Coordenadas aproximadas agrupadas alrededor del centro del campus
 * (mismo centro que usa `LocationMapper` del Radar AR).
 */
object CampusLocations {

    /** Centro del campus: respaldo cuando el lugar no está en el catálogo. */
    val CENTER = CampusPoint(
        name = "Centro del campus",
        latitude = 4.6581,
        longitude = -74.0565,
    )

    /** Lugares principales del campus que se pueden elegir en el formulario. */
    val ALL: List<CampusPoint> = listOf(
        CampusPoint("Biblioteca", 4.6585, -74.0563),
        CampusPoint("Cancha M7A", 4.6587, -74.0568),
        CampusPoint("Salón Polifuncional", 4.6576, -74.0567),
        CampusPoint("Zona Verde", 4.6588, -74.0564),
        CampusPoint("Auditorio", 4.6578, -74.0561),
        CampusPoint("Plazoleta Central", 4.6582, -74.0571),
        CampusPoint("Módulo 7", 4.6577, -74.0566),
        CampusPoint("Módulo 24", 4.6584, -74.0569),
    )

    /**
     * Punto del catálogo para [text]: primero coincidencia exacta y luego
     * el punto cuyo nombre aparezca contenido en el texto (para datos
     * antiguos o compuestos como "Azotea Módulo 7"). Null si no hay
     * coincidencia (se usa [CENTER] como respaldo).
     */
    fun find(text: String): CampusPoint? {
        val key = normalize(text)
        if (key.isEmpty()) return null
        return ALL.firstOrNull { normalize(it.name) == key }
            ?: ALL.firstOrNull { normalize(it.name) in key }
    }

    /** Minúsculas y sin tildes para comparar "Salón" == "salon". */
    private fun normalize(text: String): String =
        java.text.Normalizer.normalize(text.trim().lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
}
