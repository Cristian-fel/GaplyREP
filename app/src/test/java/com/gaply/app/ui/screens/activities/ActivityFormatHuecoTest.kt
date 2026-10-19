package com.gaply.app.ui.screens.activities

import com.gaply.app.domain.agent.DiaSemana
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.HuecoLibre
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Filtro de "Actividades para tu hueco": solo aparece en el listado lo que
 * solapa con un hueco libre de hoy (misma percepción que el agente de
 * utilidad, que descarta lo que no cabe en ningún hueco).
 */
class ActivityFormatHuecoTest {

    private val martes = DiaSemana.MARTES

    /** Hueco libre del martes 10:00-12:00 (600-720 minutos). */
    private val hueco = HuecoLibre(inicioMinutos = 600, finMinutos = 720)

    private fun actividad(inicio: String, duracion: String = "1 hora"): ActivityItem =
        ActivityItem(
            id = "act-1",
            title = "Partido de fútbol",
            category = "Deportes",
            duration = duracion,
            date = "2026-10-20",
            timeRange = "",
            location = "Cancha M7A",
            startTime = inicio,
        )

    private fun encaja(activity: ActivityItem, huecos: List<HuecoLibre> = listOf(hueco)): Boolean =
        ActivityFormat.encajaEnHueco(activity, huecos, martes)

    @Test
    fun `la actividad completa dentro del hueco encaja`() {
        // 10:00-11:00 cabe entera en el hueco 10:00-12:00.
        assertTrue(encaja(actividad("10:00")))
    }

    @Test
    fun `la actividad que solapa el hueco por el borde encaja`() {
        // 11:30-12:30 comparte 30 minutos con el hueco: coincide.
        assertTrue(encaja(actividad("11:30")))
    }

    @Test
    fun `la actividad fuera del hueco no encaja`() {
        // 12:00-13:00 empieza exactamente donde termina el hueco: solape 0.
        assertFalse(encaja(actividad("12:00")))
    }

    @Test
    fun `sin huecos libres nada encaja`() {
        assertFalse(encaja(actividad("10:00"), huecos = emptyList()))
    }

    @Test
    fun `la hora de inicio invalida no encaja`() {
        assertFalse(encaja(actividad("25:99")))
    }

    @Test
    fun `la actividad que cruza la medianoche no encaja`() {
        // 23:30 + 2 horas no cabe en la franja de un solo día.
        assertFalse(encaja(actividad("23:30", duracion = "2 horas")))
    }

    @Test
    fun `la actividad que solapa el hueco tocado por el borde`() {
        // 11:30-12:30 comparte 30 minutos con el hueco tocado 10:00-12:00.
        assertTrue(ActivityFormat.solapaConHueco(actividad("11:30"), hueco, martes))
    }

    @Test
    fun `la actividad fuera del hueco tocado no la propone`() {
        // 12:00-13:00 empieza donde termina el hueco tocado: solape 0.
        assertFalse(ActivityFormat.solapaConHueco(actividad("12:00"), hueco, martes))
    }

    @Test
    fun `la hora invalida no solapa con el hueco tocado`() {
        assertFalse(ActivityFormat.solapaConHueco(actividad("25:99"), hueco, martes))
    }
}
