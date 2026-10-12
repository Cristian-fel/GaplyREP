package com.gaply.app.domain.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del agente de utilidad con datos controlados.
 *
 * Cada prueba corresponde a un comportamiento que el profesor puede pedir
 * que se demuestre: entrada conocida -> salida esperada.
 */
class AgenteRecomendacionTest {

    private val agente = AgenteRecomendacion()

    private val huecoMartes = FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "12:00")

    private val perfilBase = PerfilAgente(
        intereses = listOf("Deportes", "Café"),
        huecos = listOf(huecoMartes),
    )

    private fun actividad(
        id: String,
        nombre: String,
        intereses: List<String>,
        dia: DiaSemana = DiaSemana.MARTES,
        inicio: String = "10:00",
        fin: String = "12:00",
    ) = Actividad(
        id = id,
        nombre = nombre,
        intereses = intereses,
        franja = FranjaHoraria.desde(dia, inicio, fin),
    )

    // ------------------------------------------------------------------
    // 1. Percepción: descartar lo incompatible
    // ------------------------------------------------------------------

    @Test
    fun descartaLaActividadQueNoEncajaEnNingunHueco() {
        val queEncaja = actividad("a1", "Fútbol", listOf("Deportes"))
        val queNo = actividad("a2", "Cine", listOf("Deportes"), inicio = "18:00", fin = "20:00")

        val resultado = agente.recomendar(perfilBase, listOf(queEncaja, queNo))

        assertEquals(1, resultado.recomendaciones.size)
        assertEquals("a1", resultado.recomendaciones[0].actividad.id)
        assertEquals(1, resultado.descartadas.size)
        assertEquals("a2", resultado.descartadas[0].actividad.id)
        assertTrue(resultado.descartadas[0].motivo.contains("No encaja"))
    }

    @Test
    fun descartaLaActividadDeUnDiaSinHuecosLibres() {
        val sabado = actividad("a1", "Caminata", listOf("Deportes"), dia = DiaSemana.SABADO)

        val resultado = agente.recomendar(perfilBase, listOf(sabado))

        assertTrue(resultado.recomendaciones.isEmpty())
        assertTrue(resultado.descartadas.single().motivo.contains("Sábado"))
    }

    // ------------------------------------------------------------------
    // 2. Decisión: la función de utilidad
    // ------------------------------------------------------------------

    @Test
    fun ordenaDeMayorAMenorUtilidad() {
        val completa = actividad("a1", "Fútbol", listOf("Deportes", "Café"))
        val unInteres = actividad("a2", "Café de estudio", listOf("Café"), fin = "11:00")
        val sinCoincidencia = actividad("a3", "Yoga", listOf("Música"), inicio = "11:00")

        val recomendaciones = agente
            .recomendar(perfilBase, listOf(sinCoincidencia, completa, unInteres))
            .recomendaciones

        assertEquals(listOf("Fútbol", "Café de estudio", "Yoga"), recomendaciones.map { it.actividad.nombre })
        assertEquals(listOf(100, 68, 35), recomendaciones.map { it.puntuacion })
    }

    @Test
    fun prefiereLaActividadQueCabeCompletaEnElHueco() {
        val perfil = PerfilAgente(intereses = listOf("Deportes"), huecos = listOf(huecoMartes))
        val completa = actividad("a1", "Completo", listOf("Deportes"))
        val seSale = actividad("a2", "A media", listOf("Deportes"), inicio = "09:30", fin = "10:30")

        val recomendaciones = agente
            .recomendar(perfil, listOf(seSale, completa))
            .recomendaciones

        assertEquals(listOf("Completo", "A media"), recomendaciones.map { it.actividad.nombre })
        assertEquals(listOf(100, 83), recomendaciones.map { it.puntuacion })
    }

    @Test
    fun alCambiarLosInteresesCambiaElOrden() {
        val futbol = actividad("a1", "Fútbol", listOf("Deportes"), inicio = "10:00", fin = "11:00")
        val concierto = actividad("a2", "Concierto", listOf("Música"), inicio = "11:00", fin = "12:00")
        val candidatas = listOf(futbol, concierto)

        val deportista = PerfilAgente(listOf("Deportes"), listOf(huecoMartes))
        val musico = PerfilAgente(listOf("Música"), listOf(huecoMartes))

        val ordenDeportes = agente.recomendar(deportista, candidatas).recomendaciones.map { it.actividad.nombre }
        val ordenMusica = agente.recomendar(musico, candidatas).recomendaciones.map { it.actividad.nombre }

        assertEquals(listOf("Fútbol", "Concierto"), ordenDeportes)
        assertEquals(listOf("Concierto", "Fútbol"), ordenMusica)
        assertNotEquals(ordenDeportes, ordenMusica)
    }

    @Test
    fun comparaLosInteresesSinTildesNiMayusculas() {
        val perfil = PerfilAgente(listOf("cafe"), listOf(huecoMartes))
        val cafeteria = actividad("a1", "Cafetería", listOf("CAFÉ"))

        val recomendacion = agente.recomendar(perfil, listOf(cafeteria)).recomendaciones.single()

        assertEquals(100, recomendacion.puntuacion)
        assertTrue(recomendacion.razones.any { it.contains("CAFÉ") })
    }

    @Test
    fun laPuntuacionSiempreEstaEntre0y100() {
        val candidatas = listOf(
            actividad("a1", "Coincide", listOf("Deportes", "Café", "Música")),
            actividad("a2", "Se solapa poco", listOf("Artes"), inicio = "11:30", fin = "13:00"),
            actividad("a3", "Sin etiquetas", emptyList()),
        )

        val resultado = agente.recomendar(perfilBase, candidatas)

        assertTrue(resultado.recomendaciones.isNotEmpty())
        resultado.recomendaciones.forEach { recomendacion ->
            assertTrue(
                "Puntuación fuera de rango: ${recomendacion.puntuacion}",
                recomendacion.puntuacion in 0..100,
            )
        }
    }

    // ------------------------------------------------------------------
    // 3. Acción: orden, límite y explicabilidad
    // ------------------------------------------------------------------

    @Test
    fun respetaElMaximoDeRecomendaciones() {
        val candidatas = (1..7).map { numero ->
            actividad("a$numero", "Actividad $numero", listOf("Deportes"), fin = "10:30")
        }

        assertEquals(5, agente.recomendar(perfilBase, candidatas).recomendaciones.size)

        val agenteDeTres = AgenteRecomendacion(maximoRecomendaciones = 3)
        assertEquals(3, agenteDeTres.recomendar(perfilBase, candidatas).recomendaciones.size)
    }

    @Test
    fun losEmpatesSeResuelvenAlfabeticamente() {
        val zumba = actividad("a2", "Zumba", listOf("Deportes"))
        val ajedrez = actividad("a1", "Ajedrez", listOf("Deportes"))

        val recomendaciones = agente.recomendar(perfilBase, listOf(zumba, ajedrez)).recomendaciones

        assertEquals(listOf("Ajedrez", "Zumba"), recomendaciones.map { it.actividad.nombre })
        assertEquals(recomendaciones[0].puntuacion, recomendaciones[1].puntuacion)
    }

    @Test
    fun cadaRecomendacionExplicaPorQueSeSugiere() {
        val futbol = actividad("a1", "Fútbol", listOf("Deportes"))

        val recomendacion = agente.recomendar(perfilBase, listOf(futbol)).recomendaciones.single()

        assertTrue(recomendacion.razones.any { it.contains("intereses", ignoreCase = true) })
        assertTrue(recomendacion.razones.any { it.contains("hueco", ignoreCase = true) })
        assertEquals(huecoMartes, recomendacion.huecoAprovechado)
    }

    @Test
    fun sinCandidatasDevuelveResultadoVacio() {
        val resultado = agente.recomendar(perfilBase, emptyList())

        assertTrue(resultado.recomendaciones.isEmpty())
        assertTrue(resultado.descartadas.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaPesosQueNoSumanUno() {
        PesosUtilidad(intereses = 0.8, horario = 0.8)
    }
}
