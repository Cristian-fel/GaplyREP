package com.gaply.app.ui.screens.radar

import android.location.Location
import java.text.Normalizer

/**
 * Radar AR · Mapeo de lugares de la universidad a coordenadas GPS.
 *
 * Las actividades guardan la ubicación como texto libre (ej. "Biblioteca",
 * "Cancha M7A", "Salón 204"). Este objeto traduce ese nombre a una
 * coordenada aproximada de la Universidad Jorge Tadeo Lozano (Campus
 * Chapinero, Bogotá, Cl. 79B #12-18).
 *
 * La búsqueda es tolerante a mayúsculas y tildes ("Biblioteca" y
 * "biblioteca" son la misma entrada). Si el nombre no coincide con
 * ningún lugar conocido, se devuelve el centro del campus como
 * coordenada por defecto.
 */
object LocationMapper {

    /** Centro del campus UJT (aproximado) — respaldo cuando el lugar no se conoce. */
    private const val CAMPUS_CENTER_LAT = 4.6581
    private const val CAMPUS_CENTER_LNG = -74.0565

    /** Nombre del "provider" de las [Location] que devuelve este mapper. */
    private const val DESTINATION_PROVIDER = "gaply_radar_destino"

    /**
     * Lugares conocidos del campus con su desplazamiento respecto al
     * centro, en grados (0.0001° ≈ 11 m). El primer valor es latitud,
     * el segundo longitud.
     */
    private val knownPlaces: List<Pair<String, Pair<Double, Double>>> = listOf(
        "biblioteca" to (0.0004 to 0.0002),
        "cancha" to (0.0006 to -0.0003),
        "auditorio" to (-0.0003 to 0.0004),
        "teatro" to (-0.0003 to 0.0004),
        "cafeteria" to (0.0002 to 0.0005),
        "restaurante" to (0.0002 to 0.0005),
        "salon" to (-0.0005 to -0.0002),
        "aula" to (-0.0005 to -0.0002),
        "bloque" to (-0.0004 to 0.0001),
        "gimnasio" to (-0.0006 to 0.0003),
        "piscina" to (-0.0006 to 0.0005),
        "capilla" to (0.0005 to -0.0005),
        "laboratorio" to (0.0003 to -0.0004),
        "jardin" to (0.0007 to 0.0001),
        "plaza" to (0.0001 to -0.0006),
        "parqueadero" to (-0.0007 to -0.0004),
        "estacionamiento" to (-0.0007 to -0.0004),
    )

    /**
     * Devuelve la [Location] destino para el nombre de lugar indicado.
     * Lugar desconocido → centro del campus.
     */
    fun resolve(placeName: String): Location {
        val key = normalize(placeName)
        val offset = knownPlaces
            .firstOrNull { (known, _) -> key.contains(known) }
            ?.second
        return Location(DESTINATION_PROVIDER).apply {
            latitude = CAMPUS_CENTER_LAT + (offset?.first ?: 0.0)
            longitude = CAMPUS_CENTER_LNG + (offset?.second ?: 0.0)
        }
    }

    /** Minúsculas, sin tildes ni signos extra y con espacios simples. */
    private fun normalize(text: String): String =
        Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
