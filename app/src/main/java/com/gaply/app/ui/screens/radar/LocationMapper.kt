package com.gaply.app.ui.screens.radar

import android.location.Location
import com.gaply.app.data.CampusLocations

/**
 * Radar AR · Mapeo de lugares de la universidad a coordenadas GPS.
 *
 * Las actividades guardan la ubicación como texto libre (ej. "Biblioteca",
 * "Cancha M7A", "Módulo 7A"). Este objeto traduce ese nombre a una
 * coordenada del catálogo [CampusLocations] (Universidad Jorge Tadeo
 * Lozano, sede Bogotá, Cra 4 #22-61 — 4.6067, -74.0676).
 *
 * La búsqueda es tolerante a mayúsculas y tildes ("Biblioteca" y
 * "biblioteca" son la misma entrada). Si el nombre no coincide con
 * ningún lugar conocido, se devuelve el centro del campus como
 * coordenada por defecto.
 *
 * Ojo: el Radar usa las coordenadas exactas de la actividad
 * (`latitude`/`longitude`) cuando existen; este mapper es solo el
 * respaldo para actividades sin GPS guardado.
 */
object LocationMapper {

    /** Nombre del "provider" de las [Location] que devuelve este mapper. */
    private const val DESTINATION_PROVIDER = "gaply_radar_destino"

    /**
     * Devuelve la [Location] destino para el nombre de lugar indicado.
     * Lugar desconocido → centro del campus.
     */
    fun resolve(placeName: String): Location {
        val punto = CampusLocations.find(placeName) ?: CampusLocations.CENTER
        return Location(DESTINATION_PROVIDER).apply {
            latitude = punto.latitude
            longitude = punto.longitude
        }
    }
}
