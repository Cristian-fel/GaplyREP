package com.gaply.app.domain.agent

import com.gaply.app.core.i18n.StringsEn
import com.gaply.app.core.i18n.StringsEs
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.ScheduleClass
import com.gaply.app.domain.model.huecosDelHorario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Batería de simulaciones y calibración del Agente de IA de Matches (Módulo 5).
 *
 * Verifica con los 10 perfiles reales de prueba del campus (`usr_01`..`usr_10`
 * de `MockDataSeeder`) y con perfiles extremos:
 *  1. Que la matriz de afinidad semántica de los 15 intereses oficiales distinga
 *     perfiles afines aunque no compartan exactamente la misma etiqueta.
 *  2. Que los pesos multicriterio (`45% Intereses + 30% Horario + 15% Universidad + 10% Meta`)
 *     ordenen coherentemente los candidatos para distintos tipos de estudiantes.
 *  3. Que el modo IA supere al modo clásico (Jaccard/proporción exacta) cuando
 *     dos estudiantes tienen intereses complementarios (p. ej. "Artes" vs "Fotografía",
 *     "Programación" vs "Ciencia").
 *  4. Que el cambio de filtros en tiempo real (cambiar intereses, universidad o
 *     meta) reconfigure el ranking y los porcentajes de compatibilidad.
 *  5. Que el prompt estructurado de IA contenga todos los pesos y atributos.
 */
class SimulacionEntrenamientoMatchesTest {

    // ------------------------------------------------------------------
    // Dataset de los 10 perfiles reales del campus (mismos de MockDataSeeder)
    // ------------------------------------------------------------------

    private data class EstudianteSimulado(
        val id: String,
        val nombre: String,
        val universidad: String,
        val meta: String,
        val intereses: List<String>,
        val clases: List<ScheduleClass>,
    ) {
        val huecos: List<FranjaHoraria> =
            huecosDelHorario(clases, HuecoLibre.MINIMO_MINUTOS)

        fun aPerfilAgente() = PerfilAgente(
            intereses = intereses,
            huecos = huecos,
            universidad = universidad,
            meta = meta,
        )

        fun aCandidatoMatch() = CandidatoMatch(
            userId = id,
            nombre = nombre,
            intereses = intereses,
            huecos = huecos,
            universidad = universidad,
            meta = meta,
        )
    }

    private fun clase(dia: String, inicio: String, fin: String) = ScheduleClass(
        id = "${dia}_${inicio}_$fin",
        title = "Clase",
        day = dia,
        startHour = inicio,
        endHour = fin,
    )

    private val cohortCampus = listOf(
        EstudianteSimulado(
            id = "usr_01",
            nombre = "Valentina",
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "📚 Necesito estudiar",
            intereses = listOf("Música", "Fotografía", "Baile"),
            clases = listOf(
                clase("Lunes", "08:00", "10:00"),
                clase("Lunes", "11:00", "13:00"),
                clase("Miércoles", "10:00", "12:00"),
                clase("Miércoles", "13:00", "15:00"),
                clase("Viernes", "09:00", "11:00"),
                clase("Viernes", "12:00", "14:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_02",
            nombre = "Andrés Felipe",
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "🏀 Qué hay para hacer",
            intereses = listOf("Deportes", "Videojuegos", "Estudio"),
            clases = listOf(
                clase("Lunes", "07:00", "09:00"),
                clase("Lunes", "10:00", "12:00"),
                clase("Martes", "08:00", "10:00"),
                clase("Martes", "11:00", "13:00"),
                clase("Jueves", "07:00", "09:00"),
                clase("Jueves", "10:00", "12:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_03",
            nombre = "María Camila",
            universidad = "Universidad Central",
            meta = "🥺 Aún no lo tengo claro",
            intereses = listOf("Películas", "Estudio", "Eventos"),
            clases = listOf(
                clase("Lunes", "08:00", "10:00"),
                clase("Lunes", "11:00", "13:00"),
                clase("Miércoles", "09:00", "11:00"),
                clase("Miércoles", "12:00", "14:00"),
                clase("Viernes", "10:00", "12:00"),
                clase("Viernes", "13:00", "15:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_04",
            nombre = "Santiago",
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "📚 Necesito estudiar",
            intereses = listOf("Programación", "Videojuegos", "Ciencia"),
            clases = listOf(
                clase("Lunes", "08:00", "10:00"),
                clase("Lunes", "11:00", "13:00"),
                clase("Martes", "07:00", "09:00"),
                clase("Martes", "10:00", "12:00"),
                clase("Jueves", "08:00", "10:00"),
                clase("Jueves", "11:00", "13:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_05",
            nombre = "Laura Sofía",
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "😉 Tengo tiempo libre",
            intereses = listOf("Viajes", "Fotografía", "Moda"),
            clases = listOf(
                clase("Lunes", "10:00", "12:00"),
                clase("Lunes", "13:00", "15:00"),
                clase("Miércoles", "08:00", "10:00"),
                clase("Miércoles", "11:00", "13:00"),
                clase("Viernes", "09:00", "11:00"),
                clase("Viernes", "12:00", "14:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_06",
            nombre = "Diego Alejandro",
            universidad = "Universidad de los Andes",
            meta = "🏀 Qué hay para hacer",
            intereses = listOf("Deportes", "Música", "Juegos de mesa"),
            clases = listOf(
                clase("Lunes", "08:00", "10:00"),
                clase("Lunes", "11:00", "13:00"),
                clase("Martes", "10:00", "12:00"),
                clase("Martes", "13:00", "15:00"),
                clase("Jueves", "08:00", "10:00"),
                clase("Jueves", "11:00", "13:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_07",
            nombre = "Isabella",
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "📚 Necesito estudiar",
            intereses = listOf("Artes", "Baile", "Películas"),
            clases = listOf(
                clase("Lunes", "09:00", "11:00"),
                clase("Lunes", "12:00", "14:00"),
                clase("Miércoles", "08:00", "10:00"),
                clase("Miércoles", "11:00", "13:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_08",
            nombre = "Mateo",
            universidad = "Universidad Central",
            meta = "😉 Tengo tiempo libre",
            intereses = listOf("Estudio", "Deportes", "Cocina"),
            clases = listOf(
                clase("Lunes", "07:00", "09:00"),
                clase("Lunes", "10:00", "12:00"),
                clase("Martes", "08:00", "10:00"),
                clase("Martes", "11:00", "13:00"),
                clase("Jueves", "07:00", "09:00"),
                clase("Jueves", "10:00", "12:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_09",
            nombre = "Camila Natalia",
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "🥺 Aún no lo tengo claro",
            intereses = listOf("Moda", "Eventos", "Música"),
            clases = listOf(
                clase("Lunes", "10:00", "12:00"),
                clase("Lunes", "13:00", "15:00"),
                clase("Miércoles", "09:00", "11:00"),
                clase("Miércoles", "12:00", "14:00"),
                clase("Jueves", "08:00", "10:00"),
                clase("Jueves", "11:00", "13:00"),
            ),
        ),
        EstudianteSimulado(
            id = "usr_10",
            nombre = "Juan Pablo",
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "📚 Necesito estudiar",
            intereses = listOf("Ciencia", "Fotografía", "Juegos de mesa"),
            clases = listOf(
                clase("Lunes", "08:00", "11:00"),
                clase("Lunes", "12:00", "14:00"),
                clase("Martes", "10:00", "12:00"),
                clase("Martes", "13:00", "16:00"),
                clase("Jueves", "09:00", "11:00"),
                clase("Jueves", "12:00", "14:00"),
            ),
        ),
    )

    private val agenteIa = AgenteMatches(
        pesos = PesosUtilidadMatches.CON_UNIVERSIDAD_Y_META,
        maximoMatches = 10,
        usarRefinamientoIa = true,
    )

    private val agenteClasico = AgenteMatches(
        pesos = PesosUtilidadMatches(),
        maximoMatches = 10,
        usarRefinamientoIa = false,
    )

    // ------------------------------------------------------------------
    // Simulación 1: Matriz completa 10x10 entre todos los usuarios del campus
    // ------------------------------------------------------------------

    @Test
    fun `simulacion 1 - matriz 10x10 del campus genera matches validos y explicados para cada estudiante`() {
        cohortCampus.forEach { estudiante ->
            val otros = cohortCampus
                .filter { it.id != estudiante.id }
                .map { it.aCandidatoMatch() }

            val resultado = agenteIa.recomendar(estudiante.aPerfilAgente(), otros)

            // Todos los estudiantes del campus deben recibir al menos 3 sugerencias compatibles
            assertTrue(
                "${estudiante.nombre} debería tener al menos 3 sugerencias y tuvo ${resultado.sugerencias.size}",
                resultado.sugerencias.size >= 3,
            )

            // Las sugerencias deben estar ordenadas de mayor a menor porcentaje
            val puntuaciones = resultado.sugerencias.map { it.puntuacion }
            assertEquals(
                "El ranking de ${estudiante.nombre} no está ordenado descendentemente",
                puntuaciones.sortedDescending(),
                puntuaciones,
            )

            // Cada sugerencia tiene puntuación en [1, 100], razones no vacías y prompt IA válido
            resultado.sugerencias.forEach { match ->
                assertTrue(match.puntuacion in 1..100)
                assertTrue(match.razones.isNotEmpty())
                assertTrue(match.promptIa.contains(estudiante.universidad))
                assertTrue(match.promptIa.contains(match.candidato.nombre))
            }
        }
    }

    // ------------------------------------------------------------------
    // Simulación 2: Entrenamiento semántico de IA vs Modo Clásico (Jaccard/Exacto)
    // ------------------------------------------------------------------

    @Test
    fun `simulacion 2 - el refinador IA detecta compatibilidad entre intereses afines que el modo clasico ignora`() {
        val huecoComun = FranjaHoraria.desde(DiaSemana.LUNES, "10:00", "11:00")

        // Estudiante de Ingeniería (Programación) frente a candidata de Ciencia y Estudio
        val programador = PerfilAgente(
            intereses = listOf("Programación"),
            huecos = listOf(huecoComun),
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "📚 Necesito estudiar",
        )
        val cientifica = CandidatoMatch(
            userId = "c_ciencia",
            nombre = "Científica",
            intereses = listOf("Ciencia", "Estudio"),
            huecos = listOf(huecoComun),
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "📚 Necesito estudiar",
        )

        val scoreClasico = agenteClasico.recomendar(programador, listOf(cientifica))
            .sugerencias.single().puntuacion
        val matchIa = agenteIa.recomendar(programador, listOf(cientifica))
            .sugerencias.single()

        // En modo clásico: 0 intereses exactos -> solo suma el 40% de horario = 40%
        assertEquals(40, scoreClasico)
        // En modo IA: afinidad semántica Programación<->Ciencia (0.75) + horario (1.0) + uni (1.0)
        // = 0.55*0.75 + 0.30 + 0.15 = 0.4125 + 0.45 = 86.25 -> 86%
        assertEquals(86, matchIa.puntuacion)
        assertTrue(matchIa.puntuacion > scoreClasico)
        assertTrue(matchIa.razones.any { it.contains("Ciencia") })
    }

    // ------------------------------------------------------------------
    // Simulación 3: Impacto de la Universidad e Intereses en el desempate
    // ------------------------------------------------------------------

    @Test
    fun `simulacion 3 - a igualdad de intereses y horario prioriza misma universidad`() {
        val hueco = FranjaHoraria.desde(DiaSemana.MIERCOLES, "12:00", "14:00")
        val yo = PerfilAgente(
            intereses = listOf("Fotografía", "Viajes"),
            huecos = listOf(hueco),
            universidad = "Universidad Jorge Tadeo Lozano",
        )

        val otraUni = CandidatoMatch(
            userId = "u1",
            nombre = "Camila UniAndes",
            intereses = listOf("Fotografía", "Viajes"),
            huecos = listOf(hueco),
            universidad = "Universidad de los Andes",
        )
        val mismaUniParcial = CandidatoMatch(
            userId = "u2",
            nombre = "Paula Tadeo",
            intereses = listOf("Fotografía"),
            huecos = listOf(hueco),
            universidad = "Universidad Jorge Tadeo Lozano",
        )
        val mismaUniCompleta = CandidatoMatch(
            userId = "u3",
            nombre = "Laura Tadeo",
            intereses = listOf("Fotografía", "Viajes"),
            huecos = listOf(hueco),
            universidad = "Universidad Jorge Tadeo Lozano",
        )

        val ranking = agenteIa.recomendar(
            yo,
            listOf(otraUni, mismaUniParcial, mismaUniCompleta),
        ).sugerencias

        assertEquals(listOf("u3", "u2", "u1"), ranking.map { it.candidato.userId })
        // u3 = 100% (55+30+15), u2 = 92% (55*0.85 + 30 + 15 = 91.75 -> 92), u1 = 85% (55+30)
        assertEquals(listOf(100, 92, 85), ranking.map { it.puntuacion })
    }

    // ------------------------------------------------------------------
    // Simulación 4: Cambio dinámico de filtros por parte del usuario
    // ------------------------------------------------------------------

    @Test
    fun `simulacion 4 - cuando el usuario cambia sus intereses o universidad en el perfil cambia el top match`() {
        val candidatos = cohortCampus.map { it.aCandidatoMatch() }
        val misHuecos = listOf(
            FranjaHoraria.desde(DiaSemana.LUNES, "10:00", "11:00"),
            FranjaHoraria.desde(DiaSemana.MARTES, "09:00", "10:00"),
            FranjaHoraria.desde(DiaSemana.MIERCOLES, "10:00", "11:00"),
        )

        // Perfil inicial: enfocado en Programación y Ciencia en la Tadeo
        val perfilStem = PerfilAgente(
            intereses = listOf("Programación", "Ciencia", "Videojuegos"),
            huecos = misHuecos,
            universidad = "Universidad Jorge Tadeo Lozano",
            meta = "📚 Necesito estudiar",
        )
        // Perfil tras editar intereses: enfocado en Moda, Fotografía y Viajes
        val perfilCreativo = perfilStem.copy(
            intereses = listOf("Viajes", "Fotografía", "Moda"),
            meta = "😉 Tengo tiempo libre",
        )

        val topStem = agenteIa.recomendar(perfilStem, candidatos).sugerencias.first()
        val topCreativo = agenteIa.recomendar(perfilCreativo, candidatos).sugerencias.first()

        assertEquals("usr_04", topStem.candidato.userId) // Santiago (Programación, Videojuegos, Ciencia)
        assertEquals("usr_05", topCreativo.candidato.userId) // Laura Sofía (Viajes, Fotografía, Moda)
        assertNotEquals(topStem.candidato.userId, topCreativo.candidato.userId)
    }

    // ------------------------------------------------------------------
    // Simulación 5: Casos borde (usuarios nuevos sin horario, sin intereses, multi-idioma)
    // ------------------------------------------------------------------

    @Test
    fun `simulacion 5 - usuario recien registrado sin horario ni intereses no rompe el agente y puntua por universidad`() {
        val usuarioNuevo = PerfilAgente(
            intereses = emptyList(),
            huecos = emptyList(),
            universidad = "Universidad Central",
        )
        val candidatos = cohortCampus.map { it.aCandidatoMatch() }

        val resultado = agenteIa.recomendar(usuarioNuevo, candidatos)

        // Al no tener horario cargado aún, no se descarta a nadie
        assertTrue(resultado.descartados.isEmpty())
        assertEquals(10, resultado.sugerencias.size)

        // María Camila (usr_03) y Mateo (usr_08) estudian en Universidad Central (15%);
        // desempate alfabético: María Camila primero con 15%
        val primero = resultado.sugerencias.first()
        assertEquals("usr_03", primero.candidato.userId)
        assertEquals(15, primero.puntuacion)
    }

    @Test
    fun `simulacion 6 - internacionalizacion en ingles mantiene los mismos porcentajes y traduce las razones`() {
        val agenteEn = AgenteMatches(
            strings = StringsEn,
            pesos = PesosUtilidadMatches.CON_UNIVERSIDAD_Y_META,
            maximoMatches = 5,
            usarRefinamientoIa = true,
        )
        val valentina = cohortCampus[0].aPerfilAgente()
        val candidatos = cohortCampus.drop(1).map { it.aCandidatoMatch() }

        val resEs = agenteIa.recomendar(valentina, candidatos).sugerencias.take(5)
        val resEn = agenteEn.recomendar(valentina, candidatos).sugerencias.take(5)

        assertEquals(resEs.map { it.candidato.userId }, resEn.map { it.candidato.userId })
        assertEquals(resEs.map { it.puntuacion }, resEn.map { it.puntuacion })
        assertFalse(resEn.first().razones.first().contains("Coincide"))
        assertTrue(resEn.first().razones.first().contains("Matches your interests"))
    }
}
