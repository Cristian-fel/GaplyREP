package com.gaply.app.domain.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del bloque de tiempo que usa el agente para leer el horario. */
class FranjaHorariaTest {

    @Test
    fun convierteHorasLegiblesAMinutos() {
        val franja = FranjaHoraria.desde(DiaSemana.MARTES, "7:30", "8:00")

        assertEquals(7 * 60 + 30, franja.inicioMinutos)
        assertEquals(8 * 60, franja.finMinutos)
        assertEquals(30, franja.duracionMinutos)
    }

    @Test
    fun calculaElSolapeEntreDosFranjas() {
        val hueco = FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "12:00")
        val actividad = FranjaHoraria.desde(DiaSemana.MARTES, "11:00", "13:00")

        assertEquals(60, hueco.minutosDeSolapeCon(actividad))
        assertTrue(hueco.solapaCon(actividad))
        assertFalse(actividad.estaContenidaEn(hueco))
    }

    @Test
    fun lasFranjasDeDiasDistintosNoSeSolapan() {
        val martes = FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "12:00")
        val sabado = FranjaHoraria.desde(DiaSemana.SABADO, "10:00", "12:00")

        assertEquals(0, martes.minutosDeSolapeCon(sabado))
        assertFalse(martes.solapaCon(sabado))
    }

    @Test
    fun unaFranjaCabeDentroDeOtraMayor() {
        val hueco = FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "12:00")
        val clase = FranjaHoraria.desde(DiaSemana.MARTES, "10:30", "11:00")

        assertTrue(clase.estaContenidaEn(hueco))
        assertFalse(hueco.estaContenidaEn(clase))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaUnaFranjaConLaFinalAnteriorALaInicial() {
        FranjaHoraria.desde(DiaSemana.MARTES, "12:00", "10:00")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaUnaHoraInexistente() {
        FranjaHoraria.desde(DiaSemana.MARTES, "25:00", "26:00")
    }

    @Test
    fun seMuestraEnFormatoLegible() {
        val franja = FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "12:00")

        assertEquals("Martes 10:00-12:00", franja.toString())
    }
}
