package com.gaply.app.domain.model

import com.gaply.app.domain.agent.DiaSemana
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Pruebas de los modelos del Módulo 5: edad del perfil (sale de `birthDate`,
 * que el registro guarda como "dd/MM/yyyy"), ids determinísticos de chat y
 * huecos libres derivados del horario.
 */
class MatchModelsTest {

    private val hoy = LocalDate.of(2026, 10, 24)

    // ------------------------------------------------------------------
    // Edad del perfil
    // ------------------------------------------------------------------

    @Test
    fun `la edad sale de la fecha del registro`() {
        assertEquals(20, MatchProfile.edadDesde("15/03/2006", hoy))
        assertEquals(25, MatchProfile.edadDesde("01/01/2001", hoy))
    }

    @Test
    fun `en el cumpleanos ya cuenta el ano`() {
        assertEquals(20, MatchProfile.edadDesde("24/10/2006", hoy))
    }

    @Test
    fun `un dia antes del cumpleanos resta un ano`() {
        assertEquals(19, MatchProfile.edadDesde("25/10/2006", hoy))
    }

    @Test
    fun `fecha vacia o con otro formato no muestra edad`() {
        assertNull(MatchProfile.edadDesde("", hoy))
        assertNull(MatchProfile.edadDesde("   ", hoy))
        assertNull(MatchProfile.edadDesde("2006-03-15", hoy))
        assertNull(MatchProfile.edadDesde("ayer", hoy))
    }

    @Test
    fun `una fecha futura no genera edad`() {
        assertNull(MatchProfile.edadDesde("25/10/2030", hoy))
    }

    // ------------------------------------------------------------------
    // Ids determinísticos de chat
    // ------------------------------------------------------------------

    @Test
    fun `el id del chat directo no depende del orden de los uids`() {
        assertEquals(ChatIds.direct("uidB", "uidA"), ChatIds.direct("uidA", "uidB"))
        assertTrue(ChatIds.direct("uidA", "uidB").startsWith("direct_"))
        assertNotEquals(ChatIds.direct("uidA", "uidB"), ChatIds.direct("uidA", "uidC"))
    }

    @Test
    fun `el id del chat de grupo es el de la actividad`() {
        assertEquals("act_123", ChatIds.group("123"))
    }

    // ------------------------------------------------------------------
    // Huecos libres del horario (misma lógica que "Mis huecos")
    // ------------------------------------------------------------------

    @Test
    fun `calcula los huecos por dia e ignora los dias que no son de la semana`() {
        val clases = listOf(
            ScheduleClass(
                id = "1",
                title = "Cálculo",
                day = "Martes",
                startHour = "08:00",
                endHour = "10:00",
            ),
            ScheduleClass(
                id = "2",
                title = "Física",
                day = "Martes",
                startHour = "11:00",
                endHour = "12:00",
            ),
            ScheduleClass(
                id = "3",
                title = "Taller",
                day = "Día inválido",
                startHour = "08:00",
                endHour = "12:00",
            ),
        )

        val huecos = huecosDelHorario(clases, minimoMinutos = 60)

        assertEquals(1, huecos.size)
        assertEquals(DiaSemana.MARTES, huecos[0].dia)
        assertEquals(10 * 60, huecos[0].inicioMinutos)
        assertEquals(11 * 60, huecos[0].finMinutos)
    }

    @Test
    fun `los huecos cortos no cuentan`() {
        val clases = listOf(
            ScheduleClass(id = "1", day = "Lunes", startHour = "08:00", endHour = "10:00"),
            ScheduleClass(id = "2", day = "Lunes", startHour = "10:30", endHour = "12:00"),
        )

        assertTrue(huecosDelHorario(clases, minimoMinutos = 60).isEmpty())
        assertEquals(1, huecosDelHorario(clases, minimoMinutos = 30).size)
    }

    @Test
    fun `sin horario no hay huecos`() {
        assertTrue(huecosDelHorario(emptyList(), minimoMinutos = 60).isEmpty())
    }
}
