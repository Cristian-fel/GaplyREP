package com.gaply.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas del catálogo de coordenadas del campus (Radar AR y formulario).
 *
 * Los datos vienen de pines reales de Google Maps; el centro es la sede
 * de la Universidad Jorge Tadeo Lozano en Bogotá (Cra 4 #22-61).
 */
class CampusLocationsTest {

    @Test
    fun `el centro del campus es la sede real de la tadeo`() {
        assertEquals(4.6067, CampusLocations.CENTER.latitude, 0.0001)
        assertEquals(-74.0676, CampusLocations.CENTER.longitude, 0.0001)
    }

    @Test
    fun `encuentra la biblioteca con coincidencia exacta`() {
        val punto = CampusLocations.find("Biblioteca")
        assertNotNull(punto)
        assertEquals(4.606351, punto!!.latitude, 0.0001)
        assertEquals(-74.067468, punto.longitude, 0.0001)
    }

    @Test
    fun `encuentra el modulo 7a sin importar mayusculas ni tildes`() {
        val punto = CampusLocations.find("MÓDULO 7A")
        assertNotNull(punto)
        assertEquals(4.606984, punto!!.latitude, 0.0001)
        assertEquals(-74.068782, punto.longitude, 0.0001)
    }

    @Test
    fun `encuentra el lugar aunque el texto tenga mas palabras`() {
        val punto = CampusLocations.find("Biblioteca central, piso 2")
        assertNotNull(punto)
        assertEquals(4.606351, punto!!.latitude, 0.0001)
    }

    @Test
    fun `lugar desconocido devuelve null para usar el centro`() {
        assertNull(CampusLocations.find("Lugar inventado xyz"))
        assertNull(CampusLocations.find(""))
    }

    @Test
    fun `todos los puntos del catalogo estan en el campus`() {
        // Cordón de seguridad: nada a más de ~1.5 km del centro real.
        CampusLocations.ALL.forEach { punto ->
            val dLat = (punto.latitude - CampusLocations.CENTER.latitude) * 111_000.0
            val dLng = (punto.longitude - CampusLocations.CENTER.longitude) * 111_000.0
            val distancia = kotlin.math.sqrt(dLat * dLat + dLng * dLng)
            org.junit.Assert.assertTrue(
                "Fuera del campus: ${punto.name} a ${distancia.toInt()} m",
                distancia < 1_500.0,
            )
        }
    }
}
