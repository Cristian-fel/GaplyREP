package com.gaply.app.domain.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del agente de Matches con datos controlados.
 *
 * Comprueba el mismo ciclo que `AgenteRecomendacionTest` (percepción ->
 * decisión -> acción) pero con la función de utilidad propia de los perfiles:
 * 0.60 intereses + 0.40 solape de huecos libres.
 */
class AgenteMatchesTest {

    private val agente = AgenteMatches()

    private val huecoMartes = FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "12:00")

    private val perfilBase = PerfilAgente(
        intereses = listOf("Deportes"),
        huecos = listOf(huecoMartes),
    )

    private fun candidato(
        id: String,
        nombre: String = id,
        intereses: List<String> = emptyList(),
        huecos: List<FranjaHoraria> = listOf(huecoMartes),
    ) = CandidatoMatch(
        userId = id,
        nombre = nombre,
        intereses = intereses,
        huecos = huecos,
    )

    // ------------------------------------------------------------------
    // 1. Percepción: descartar lo que no comparte hueco libre
    // ------------------------------------------------------------------

    @Test
    fun descartaAlPerfilSinNingunHuecoLibreSimultaneo() {
        val compatible = candidato("a1", "Ana", listOf("Deportes"))
        val sinSolape = candidato(
            "a2",
            "Beto",
            intereses = listOf("Deportes"),
            huecos = listOf(FranjaHoraria.desde(DiaSemana.SABADO, "10:00", "12:00")),
        )

        val resultado = agente.recomendar(perfilBase, listOf(compatible, sinSolape))

        assertEquals(listOf("a1"), resultado.sugerencias.map { it.candidato.userId })
        assertEquals(listOf("a2"), resultado.descartados.map { it.candidato.userId })
        assertTrue(resultado.descartados.single().motivo.contains("común"))
    }

    @Test
    fun descartaAlPerfilSinHorarioSoloCuandoLosDosTienenHorarioSinSolape() {
        val compatible = candidato("a1", "Ana", listOf("Deportes"))
        val sinSolape = candidato(
            "a2",
            "Beto",
            intereses = listOf("Deportes"),
            huecos = listOf(FranjaHoraria.desde(DiaSemana.SABADO, "10:00", "12:00")),
        )

        val resultado = agente.recomendar(perfilBase, listOf(compatible, sinSolape))

        assertEquals(listOf("a1"), resultado.sugerencias.map { it.candidato.userId })
        assertEquals(listOf("a2"), resultado.descartados.map { it.candidato.userId })
        assertTrue(resultado.descartados.single().motivo.contains("común"))
    }

    @Test
    fun muestraAlPerfilSinHorarioYPuntuaSoloPorIntereses() {
        // Sin horario del otro lado no hay evidencia de incompatibilidad:
        // entra al mazo y el eje de tiempo aporta 0 (solo mandan los intereses).
        val sinHorario = candidato("a1", "Caro", listOf("Deportes"), huecos = emptyList())

        val resultado = agente.recomendar(perfilBase, listOf(sinHorario))

        assertTrue(resultado.descartados.isEmpty())
        val sugerencia = resultado.sugerencias.single()
        assertEquals(60, sugerencia.puntuacion)
        assertTrue(sugerencia.huecosEnComun.isEmpty())
    }

    @Test
    fun noDescartaCuandoElUsuarioNoTieneHorario() {
        val sinHorarioYo = PerfilAgente(intereses = listOf("Deportes"), huecos = emptyList())
        val candidato = candidato("a1", "Ana", listOf("Deportes"))

        val resultado = agente.recomendar(sinHorarioYo, listOf(candidato))

        assertTrue(resultado.descartados.isEmpty())
        assertEquals(60, resultado.sugerencias.single().puntuacion)
    }

    @Test
    fun sinCandidatasDevuelveResultadoVacio() {
        val resultado = agente.recomendar(perfilBase, emptyList())

        assertTrue(resultado.sugerencias.isEmpty())
        assertTrue(resultado.descartados.isEmpty())
    }

    // ------------------------------------------------------------------
    // 2. Decisión: la función de utilidad (0.60 + 0.40)
    // ------------------------------------------------------------------

    @Test
    fun puntuaInteresesYHorarioConLosPesosDelModulo() {
        val perfecto = candidato("a1", "Perfecto", listOf("Deportes"))
        val soloHorario = candidato("a2", "Solo horario", listOf("Música"))
        val horarioParcial = candidato(
            "a3",
            "Parcial",
            intereses = listOf("Deportes"),
            huecos = listOf(FranjaHoraria.desde(DiaSemana.MARTES, "11:00", "13:00")),
        )

        val sugerencias = agente
            .recomendar(perfilBase, listOf(soloHorario, horarioParcial, perfecto))
            .sugerencias

        assertEquals(listOf("a1", "a3", "a2"), sugerencias.map { it.candidato.userId })
        // 0.60*1.0 + 0.40*1.0 = 100 · 0.60*1.0 + 0.40*0.5 = 80 · 0.60*0 + 0.40*1.0 = 40
        assertEquals(listOf(100, 80, 40), sugerencias.map { it.puntuacion })
    }

    @Test
    fun laPuntuacionSiempreEstaEntre0y100() {
        val candidatos = listOf(
            candidato("a1", "Uno", listOf("Deportes", "Música", "Artes")),
            candidato("a2", "Dos", intereses = emptyList()),
            candidato(
                "a3",
                "Tres",
                huecos = listOf(FranjaHoraria.desde(DiaSemana.MARTES, "11:59", "12:00")),
            ),
        )

        agente.recomendar(perfilBase, candidatos).sugerencias.forEach {
            assertTrue("Puntuación fuera de rango: ${it.puntuacion}", it.puntuacion in 0..100)
        }
    }

    @Test
    fun comparaLosInteresesSinTildesNiMayusculas() {
        val perfil = PerfilAgente(listOf("Deportes"), listOf(huecoMartes))
        val futbolista = candidato("a1", "Futbol", listOf("DEPORTES"))

        val sugerencia = agente.recomendar(perfil, listOf(futbolista)).sugerencias.single()

        assertEquals(100, sugerencia.puntuacion)
        assertTrue(sugerencia.razones.any { it.contains("DEPORTES") })
    }

    @Test
    fun explicaLasRazonesDeCadaSugerencia() {
        val candidato = candidato("a1", "Ana", listOf("Deportes"))

        val sugerencia = agente.recomendar(perfilBase, listOf(candidato)).sugerencias.single()

        assertTrue(sugerencia.razones.any { it.contains("intereses", ignoreCase = true) })
        assertTrue(sugerencia.razones.any { it.contains("común", ignoreCase = true) })
        assertTrue(sugerencia.huecosEnComun.isNotEmpty())
        assertEquals(120, sugerencia.minutosEnComun)
    }

    // ------------------------------------------------------------------
    // 3. Acción: orden, límite y desempates
    // ------------------------------------------------------------------

    @Test
    fun respetaElMaximoDeMatches() {
        val candidatos = (1..7).map { candidato("a$it", "Perfil $it", listOf("Deportes")) }

        assertEquals(5, agente.recomendar(perfilBase, candidatos).sugerencias.size)

        val agenteDeTres = AgenteMatches(maximoMatches = 3)
        assertEquals(3, agenteDeTres.recomendar(perfilBase, candidatos).sugerencias.size)
    }

    @Test
    fun losEmpatesSeResuelvenAlfabeticamente() {
        val zoe = candidato("a2", "Zoe", listOf("Deportes"))
        val ana = candidato("a1", "Ana", listOf("Deportes"))

        val sugerencias = agente.recomendar(perfilBase, listOf(zoe, ana)).sugerencias

        assertEquals(listOf("Ana", "Zoe"), sugerencias.map { it.candidato.nombre })
        assertEquals(sugerencias[0].puntuacion, sugerencias[1].puntuacion)
    }

    @Test
    fun fusionaLosHuecosEnComunDeUnMismoTramo() {
        val perfil = PerfilAgente(
            intereses = listOf("Deportes"),
            huecos = listOf(
                FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "11:00"),
                FranjaHoraria.desde(DiaSemana.MARTES, "11:00", "12:00"),
            ),
        )
        val candidato = candidato(
            "a1",
            "Ana",
            intereses = listOf("Deportes"),
            huecos = listOf(FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "12:00")),
        )

        val sugerencia = agente.recomendar(perfil, listOf(candidato)).sugerencias.single()

        assertEquals(1, sugerencia.huecosEnComun.size)
        assertEquals(120, sugerencia.minutosEnComun)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaPesosQueNoSumanUno() {
        PesosUtilidadMatches(intereses = 0.7, horario = 0.7)
    }
}
