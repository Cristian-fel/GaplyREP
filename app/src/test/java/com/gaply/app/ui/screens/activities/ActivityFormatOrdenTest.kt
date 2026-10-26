package com.gaply.app.ui.screens.activities

import com.gaply.app.domain.model.ActivityItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Orden "lo que pasa primero va primero" = fecha + hora de inicio.
 *
 * Lo comparten dos listados del módulo: **Actividades actuales** (las que uní)
 * y el listado por **categoría** que se abre al tocar un lugar del Descubrir
 * (Biblioteca → Estudio, Cancha → Deportes, …).
 */
class ActivityFormatOrdenTest {

    private fun actividad(
        id: String,
        date: String,
        startTime: String = "12:00",
    ): ActivityItem = ActivityItem(
        id = id,
        title = "Actividad $id",
        category = "Eventos",
        duration = "1 hora",
        date = date,
        timeRange = "",
        location = "Biblioteca Central",
        startTime = startTime,
    )

    @Test
    fun `ordena de la fecha mas proxima a la mas lejana`() {
        val lejana = actividad("lejana", "2026-11-14")
        val cercana = actividad("cercana", "2026-10-26")

        val orden = listOf(lejana, cercana).sortedBy { ActivityFormat.ordenDeProximidad(it) }

        assertEquals(listOf("cercana", "lejana"), orden.map { it.id })
    }

    @Test
    fun `con la misma fecha desempata por la hora de inicio`() {
        val tarde = actividad("tarde", "2026-10-26", startTime = "18:00")
        val temprano = actividad("temprano", "2026-10-26", startTime = "07:00")

        val orden = listOf(tarde, temprano).sortedBy { ActivityFormat.ordenDeProximidad(it) }

        assertEquals(listOf("temprano", "tarde"), orden.map { it.id })
    }

    @Test
    fun `una fecha ilegible queda al final del listado`() {
        val sinFecha = actividad("sin-fecha", "mañana")
        val valida = actividad("valida", "2026-11-14")

        assertTrue(
            ActivityFormat.ordenDeProximidad(sinFecha) >
                ActivityFormat.ordenDeProximidad(valida),
        )
    }

    @Test
    fun `entiende ISO y millis con la misma clave de orden`() {
        // El formulario guarda millis en texto; los datos de prueba usan ISO.
        // Se usa hora 00:00 para que la clave sea solo la fecha.
        val esperado = LocalDate.parse("2026-10-25")
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val porIso = ActivityFormat.ordenDeProximidad(
            actividad("iso", "2026-10-25", startTime = "00:00"),
        )
        val porMillis = ActivityFormat.ordenDeProximidad(
            actividad("millis", "$esperado", startTime = "00:00"),
        )

        assertEquals(esperado, porIso)
        assertEquals(porIso, porMillis)
    }

    @Test
    fun `una hora invalida no revienta el orden`() {
        val rara = actividad("rara", "2026-10-26", startTime = "25:99")
        val normal = actividad("normal", "2026-10-26", startTime = "12:00")

        // Hora ilegible → cuenta como 00:00: sigue ordenando, sin excepción.
        assertTrue(ActivityFormat.ordenDeProximidad(rara) < ActivityFormat.ordenDeProximidad(normal))
    }
}
