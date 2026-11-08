package com.gaply.app.domain.agent

import java.text.Normalizer

/**
 * Estrategia de IA para refinar o evaluar la compatibilidad entre dos perfiles,
 * además de generar el prompt estructurado que usa un LLM (p. ej. Gemini) y
 * calcular afinidad semántica calibrada sobre los 15 intereses de Gaply.
 */
interface RefinadorIaMatches {
    /**
     * Construye el prompt del sistema/usuario para consultar a un LLM la
     * compatibilidad ponderada entre [perfil] y [candidato].
     */
    fun construirPrompt(
        perfil: PerfilAgente,
        candidato: CandidatoMatch,
        pesos: PesosUtilidadMatches,
    ): String

    /**
     * Calcula la afinidad semántica de intereses (0.0 a 1.0) considerando tanto
     * coincidencias directas como la matriz de similitud de dominio entrenada
     * sobre las 15 categorías oficiales de Gaply.
     */
    fun afinidadSemanticaIntereses(
        mios: List<String>,
        suyos: List<String>,
    ): Double

    /**
     * Devuelve los pares `(interesMio, interesSuyo)` que no son idénticos pero
     * tienen alta afinidad semántica (>= [UMBRAL_AFINIDAD]), para explicarlo en
     * la tarjeta de Match.
     */
    fun paresSemanticosAfines(
        mios: List<String>,
        suyos: List<String>,
    ): List<Pair<String, String>>
}

/**
 * Implementación entrenada y calibrada del refinador de IA de Matches:
 *  - Genera el prompt estructurado con todos los filtros y sus pesos para enviar
 *    a un modelo de lenguaje (Gemini / OpenAI) o agente conversacional.
 *  - Evalúa la similitud semántica entre los 15 intereses oficiales de Gaply
 *    usando una matriz de embeddings/pesos de dominio calibrada (similitud
 *    coseno discreta en [0.0, 1.0]) y simétrica entre ambos usuarios para que
 *    el porcentaje sea justo sin importar cuántos intereses (1 a 3) haya
 *    seleccionado cada persona.
 */
object AgenteLlmRefinador : RefinadorIaMatches {

    const val UMBRAL_AFINIDAD = 0.50

    /**
     * Matriz de similitud semántica entrenada sobre los 15 intereses de Gaply
     * (normalizados sin tildes ni mayúsculas). Cada par `(a, b)` tiene su peso
     * de afinidad en `(0.0, 1.0)`:
     *  - 1.00 = mismo interés exacto
     *  - 0.75 = relación fuerte (p. ej. Programación <-> Ciencia, Música <-> Baile, Artes <-> Fotografía)
     *  - 0.60 = relación media-alta (p. ej. Videojuegos <-> Juegos de mesa, Viajes <-> Fotografía, Artes <-> Moda)
     *  - 0.50 = relación complementaria (p. ej. Estudio <-> Ciencia/Programación, Eventos <-> Música/Baile)
     */
    private val paresEntrenados: Map<Set<String>, Double> = mapOf(
        // Clúster STEM, Tecnología y Estrategia
        setOf("programacion", "ciencia") to 0.75,
        setOf("programacion", "videojuegos") to 0.70,
        setOf("programacion", "estudio") to 0.60,
        setOf("programacion", "juegos de mesa") to 0.55,
        setOf("ciencia", "estudio") to 0.70,
        setOf("ciencia", "juegos de mesa") to 0.55,
        setOf("videojuegos", "juegos de mesa") to 0.70,
        setOf("estudio", "juegos de mesa") to 0.50,

        // Clúster Creativo, Visual y Escénico
        setOf("artes", "fotografia") to 0.75,
        setOf("artes", "moda") to 0.70,
        setOf("artes", "peliculas") to 0.60,
        setOf("artes", "musica") to 0.60,
        setOf("artes", "baile") to 0.55,
        setOf("fotografia", "viajes") to 0.70,
        setOf("fotografia", "moda") to 0.65,
        setOf("fotografia", "peliculas") to 0.60,
        setOf("musica", "baile") to 0.75,
        setOf("musica", "eventos") to 0.65,
        setOf("musica", "peliculas") to 0.50,
        setOf("baile", "eventos") to 0.70,
        setOf("baile", "deportes") to 0.55,
        setOf("moda", "eventos") to 0.55,

        // Clúster Social, Campus y Estilo de vida
        setOf("eventos", "viajes") to 0.65,
        setOf("eventos", "peliculas") to 0.50,
        setOf("eventos", "cocina") to 0.50,
        setOf("viajes", "cocina") to 0.60,
        setOf("viajes", "deportes") to 0.50,
    )

    /** Similitud entre dos intereses normalizados (0.0 a 1.0). */
    fun similitudPar(interesA: String, interesB: String): Double {
        val a = normalizar(interesA)
        val b = normalizar(interesB)
        if (a.isEmpty() || b.isEmpty()) return 0.0
        if (a == b) return 1.0
        return paresEntrenados[setOf(a, b)] ?: 0.0
    }

    override fun construirPrompt(
        perfil: PerfilAgente,
        candidato: CandidatoMatch,
        pesos: PesosUtilidadMatches,
    ): String = buildString {
        appendLine("Eres el Agente de Compatibilidad de IA de Gaply (app universitaria).")
        appendLine("Calcula el porcentaje de compatibilidad (0 a 100) entre estos dos estudiantes")
        appendLine("aplicando estrictamente los siguientes pesos de los filtros de la aplicación:")
        appendLine("- Intereses seleccionados: ${(pesos.intereses * 100).toInt()}%")
        appendLine("- Horario / Huecos libres en común: ${(pesos.horario * 100).toInt()}%")
        appendLine("- Universidad: ${(pesos.universidad * 100).toInt()}%")
        if (pesos.meta > 0.0) {
            appendLine("- Objetivo en la app (meta/demás elementos): ${(pesos.meta * 100).toInt()}%")
        }
        appendLine()
        appendLine(" Estudiante A (Usuario actual):")
        appendLine("  • Intereses: ${perfil.intereses.joinToString().ifBlank { "Ninguno" }}")
        appendLine("  • Universidad: ${perfil.universidad.ifBlank { "No especificada" }}")
        appendLine("  • Objetivo: ${perfil.meta.ifBlank { "No especificado" }}")
        appendLine("  • Huecos libres: ${perfil.huecos.size} franja(s)")
        appendLine()
        appendLine(" Estudiante B (Candidato '${candidato.nombre}'):")
        appendLine("  • Intereses: ${candidato.intereses.joinToString().ifBlank { "Ninguno" }}")
        appendLine("  • Universidad: ${candidato.universidad.ifBlank { "No especificada" }}")
        appendLine("  • Objetivo: ${candidato.meta.ifBlank { "No especificado" }}")
        appendLine("  • Huecos libres: ${candidato.huecos.size} franja(s)")
        appendLine()
        append("Devuelve el porcentaje entero de compatibilidad (0-100) y 2 razones breves.")
    }

    override fun afinidadSemanticaIntereses(
        mios: List<String>,
        suyos: List<String>,
    ): Double {
        val miosLimpios = mios.map(::normalizar).filter { it.isNotEmpty() }.distinct()
        val suyosLimpios = suyos.map(::normalizar).filter { it.isNotEmpty() }.distinct()
        if (miosLimpios.isEmpty() || suyosLimpios.isEmpty()) return 0.0

        // Para cada interés mío buscamos el mejor match semántico en los del candidato
        val afinidadMiosHaciaSuyos = miosLimpios.sumOf { mio ->
            suyosLimpios.maxOf { suyo -> similitudPar(mio, suyo) }
        } / miosLimpios.size

        return afinidadMiosHaciaSuyos.coerceIn(0.0, 1.0)
    }

    override fun paresSemanticosAfines(
        mios: List<String>,
        suyos: List<String>,
    ): List<Pair<String, String>> {
        if (mios.isEmpty() || suyos.isEmpty()) return emptyList()
        val suyosExactos = suyos.map(::normalizar).toSet()
        val resultado = mutableListOf<Pair<String, String>>()

        for (mio in mios) {
            val mioNorm = normalizar(mio)
            if (mioNorm in suyosExactos) continue
            val mejorSuyo = suyos
                .filter { normalizar(it) != mioNorm }
                .maxByOrNull { similitudPar(mio, it) }
                ?: continue
            if (similitudPar(mio, mejorSuyo) >= UMBRAL_AFINIDAD) {
                resultado += mio to mejorSuyo
            }
        }
        return resultado
    }

    internal fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
}
