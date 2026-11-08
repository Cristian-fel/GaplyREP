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
 * Puntos principales de la Universidad Jorge Tadeo Lozano (sede Bogotá,
 * Cra 4 #22-61, Las Nieves — 4.6067, -74.0676). Las actividades guardan
 * el nombre del punto elegido junto a su latitud/longitud, de modo que el
 * Radar AR pueda apuntar directo a la zona sin texto libre.
 *
 * Coordenadas reales tomadas de Google Maps (pin sobre cada lugar);
 * el mismo centro usa `LocationMapper` del Radar AR.
 */
object CampusLocations {

    /** Centro del campus: respaldo cuando el lugar no está en el catálogo. */
    val CENTER = CampusPoint(
        name = "Centro del campus",
        latitude = 4.6067,
        longitude = -74.0676,
    )

    /** Lugares principales del campus que se pueden elegir en el formulario. */
    val ALL: List<CampusPoint> = listOf(
        CampusPoint("Módulo 7A", 4.606984, -74.068782),
        CampusPoint("Biblioteca", 4.606351, -74.067468),
        CampusPoint("Cancha M7A", 4.607168, -74.068427),
        CampusPoint("Arcos", 4.606828, -74.067653),
        CampusPoint("Zona Verde", 4.607223, -74.068725),
        CampusPoint("Cafetería", 4.606795, -74.068450),
        CampusPoint("Restaurante", 4.607424, -74.067762),
        CampusPoint("Gimnasio", 4.607991, -74.067496),
        CampusPoint("Zona Deportes", 4.607998, -74.067579),
        CampusPoint("Brújula", 4.606787, -74.067914),
        CampusPoint("Auditorio", 4.606351, -74.067468),
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
