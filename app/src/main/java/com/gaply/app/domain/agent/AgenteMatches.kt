package com.gaply.app.domain.agent

import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.StringsEs
import java.text.Normalizer
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pesos de la función de utilidad de Matches. Deben sumar 1.0.
 *
 * Permite ponderar tanto los ejes clásicos (intereses + horario) como los
 * filtros adicionales del perfil (universidad y objetivo/meta en Gaply).
 *
 * @param intereses   cuánto pesa la compatibilidad de intereses (por defecto 0.60).
 * @param horario     cuánto pesa el solape de huecos libres (por defecto 0.40).
 * @param universidad cuánto pesa coincidir en la misma universidad (0.0 por defecto;
 *                    en [CON_UNIVERSIDAD_Y_META] es 0.15).
 * @param meta        cuánto pesa coincidir en el objetivo dentro de la app (0.0 por
 *                    defecto; en [CON_UNIVERSIDAD_Y_META] es 0.10).
 */
data class PesosUtilidadMatches(
    val intereses: Double = 0.60,
    val horario: Double = 0.40,
    val universidad: Double = 0.0,
    val meta: Double = 0.0,
) {
    init {
        require(
            intereses >= 0.0 && horario >= 0.0 && universidad >= 0.0 && meta >= 0.0,
        ) { "Los pesos no pueden ser negativos." }
        val suma = intereses + horario + universidad + meta
        require(abs(suma - 1.0) < 0.0001) { "La suma de los pesos debe ser 1.0." }
    }

    companion object {
        /**
         * Configuración multicriterio para la pantalla de Matches:
         *  - 55 % intereses (coincidencia directa + afinidad semántica de IA)
         *  - 30 % horario / huecos libres en común (solape de tiempo libre entre clases)
         *  - 15 % universidad (coincidencia de institución)
         */
        val CON_UNIVERSIDAD_Y_META = PesosUtilidadMatches(
            intereses = 0.55,
            horario = 0.30,
            universidad = 0.15,
            meta = 0.0,
        )
    }
}

/**
 * Lo que el agente percibe de cada perfil candidato (puro Kotlin: sin
 * Android, sin Firestore, sin la data class [com.gaply.app.domain.model.MatchProfile]).
 *
 * @param nombre solo se usa para desempatar alfabéticamente.
 */
data class CandidatoMatch(
    val userId: String,
    val nombre: String,
    val intereses: List<String> = emptyList(),
    val huecos: List<FranjaHoraria> = emptyList(),
    val universidad: String = "",
    val meta: String = "",
)

/** Candidato que superó la percepción, con su puntuación y sus razones. */
data class PuntuacionMatch(
    val candidato: CandidatoMatch,
    val puntuacion: Int,
    val razones: List<String>,
    val huecosEnComun: List<FranjaHoraria>,
    /** Prompt estructurado generado para auditar o consultar al LLM. */
    val promptIa: String = "",
) {
    /** Minutos totales de hueco compartidos (para el chip "X en común"). */
    val minutosEnComun: Int get() = huecosEnComun.sumOf { it.duracionMinutos }
}

/** Candidato descartado y el motivo, para poder explicar el "por qué no". */
data class DescarteMatch(
    val candidato: CandidatoMatch,
    val motivo: String,
)

/** Resultado de una corrida del agente de Matches: sugeridos y descartados. */
data class ResultadoMatches(
    val sugerencias: List<PuntuacionMatch> = emptyList(),
    val descartados: List<DescarteMatch> = emptyList(),
)

/**
 * Agente de Matches de Gaply (materia de Inteligencia Artificial).
 *
 * Sigue el mismo ciclo percepción -> decisión -> acción que
 * [AgenteRecomendacion], con su propia función de utilidad ponderada para
 * perfiles:
 *
 *  utilidad = w_intereses * compatibilidadDeIntereses
 *           + w_horario * solapeDeHuecosLibres
 *           + w_universidad * coincidenciaUniversidad
 *           + w_meta * coincidenciaMeta
 *
 * Cuando [usarRefinamientoIa] es `true`, la compatibilidad de intereses usa
 * [refinadorIa] (matriz de similitud semántica entrenada sobre los 15 intereses
 * + generación del prompt LLM) en lugar de limitarse al cruce exacto de etiquetas.
 *
 *  1. **Percepción**: solo descarta un perfil cuando AMBOS tienen horario y,
 *     aun así, no comparten ni un minuto (motivo legible vía i18n). Si falta
 *     el horario de alguno de los dos no hay evidencia de incompatibilidad:
 *     el perfil se evalúa igual y el eje de tiempo puntúa 0.
 *  2. **Decisión**: puntúa 0-100 y explica por qué con razones legibles.
 *  3. **Acción**: devuelve los [maximoMatches] mejores ordenados.
 *
 * Es puro Kotlin y se prueba con datos controlados (ver `AgenteMatchesTest`).
 */
class AgenteMatches(
    private val strings: AppStrings = StringsEs,
    val pesos: PesosUtilidadMatches = PesosUtilidadMatches(),
    val maximoMatches: Int = 5,
    val usarRefinamientoIa: Boolean = false,
    private val refinadorIa: RefinadorIaMatches = AgenteLlmRefinador,
) {
    init {
        require(maximoMatches > 0) { "El máximo de matches debe ser positivo." }
    }

    /** Ejecuta el ciclo completo del agente. */
    fun recomendar(perfil: PerfilAgente, candidatos: List<CandidatoMatch>): ResultadoMatches {
        val (compatibles, descartados) = percibir(perfil, candidatos)
        val evaluados = decidir(perfil, compatibles)
        return actuar(evaluados, descartados)
    }

    /** Construye el prompt de IA para evaluar un par (perfil, candidato). */
    fun construirPromptIa(perfil: PerfilAgente, candidato: CandidatoMatch): String =
        refinadorIa.construirPrompt(perfil, candidato, pesos)

    /**
     * 1. PERCEPCIÓN:
     *  - En el modo clásico (`usarRefinamientoIa = false`), descarta al que
     *    teniendo horario no solapa ni un minuto con el del usuario.
     *  - En el modo IA multicriterio (`usarRefinamientoIa = true`), también
     *    conserva al candidato si coincide en universidad, meta o afinidad de
     *    intereses (en ese caso el eje de horario puntúa 0, pero el usuario
     *    puede conocer compañeros compatibles de su universidad/intereses).
     */
    internal fun percibir(
        perfil: PerfilAgente,
        candidatos: List<CandidatoMatch>,
    ): PairListas {
        val compatibles = mutableListOf<CandidatoMatch>()
        val descartados = mutableListOf<DescarteMatch>()

        candidatos.forEach { candidato ->
            val faltaHorario = perfil.huecos.isEmpty() || candidato.huecos.isEmpty()
            val enComun = huecosEnComun(perfil.huecos, candidato.huecos)
            val tieneAfinidadIa = usarRefinamientoIa && (
                compatibilidadDeIntereses(perfil, candidato) > 0.0 ||
                    compatibilidadUniversidad(perfil, candidato) > 0.0 ||
                    (pesos.meta > 0.0 && compatibilidadMeta(perfil, candidato) > 0.0)
                )
            if (faltaHorario || enComun.isNotEmpty() || tieneAfinidadIa) {
                compatibles += candidato
            } else {
                descartados += DescarteMatch(candidato, strings.reasonNoCommonFreeSlot)
            }
        }
        return PairListas(compatibles = compatibles, descartados = descartados)
    }

    /** 2. DECISIÓN: cada candidato recibe su utilidad (0-100) y sus razones. */
    internal fun decidir(
        perfil: PerfilAgente,
        compatibles: List<CandidatoMatch>,
    ): List<PuntuacionMatch> = compatibles.map { candidato ->
        val enComun = huecosEnComun(perfil.huecos, candidato.huecos)

        val utilidadIntereses = compatibilidadDeIntereses(perfil, candidato)
        val utilidadHorario = solapeDeHuecos(enComun, perfil.huecos, candidato.huecos)
        val utilidadUniversidad = compatibilidadUniversidad(perfil, candidato)
        val utilidadMeta = compatibilidadMeta(perfil, candidato)

        val utilidad = pesos.intereses * utilidadIntereses +
            pesos.horario * utilidadHorario +
            pesos.universidad * utilidadUniversidad +
            pesos.meta * utilidadMeta

        PuntuacionMatch(
            candidato = candidato,
            puntuacion = (utilidad * 100).roundToInt().coerceIn(0, 100),
            razones = razonesDe(perfil, candidato, enComun),
            huecosEnComun = enComun,
            promptIa = if (usarRefinamientoIa) construirPromptIa(perfil, candidato) else "",
        )
    }

    /** 3. ACCIÓN: ordena de mayor a menor utilidad y limita el resultado. */
    internal fun actuar(
        evaluados: List<PuntuacionMatch>,
        descartados: List<DescarteMatch>,
    ): ResultadoMatches {
        val ordenadas = evaluados
            .sortedWith(
                compareByDescending<PuntuacionMatch> { it.puntuacion }
                    .thenBy { it.candidato.nombre.lowercase() },
            )
            .take(maximoMatches)

        return ResultadoMatches(sugerencias = ordenadas, descartados = descartados)
    }

    // ------------------------------------------------------------------
    // Funciones de utilidad
    // ------------------------------------------------------------------

    /**
     * Proporción de MIS intereses que también tiene el candidato (0.0 - 1.0).
     * Si [usarRefinamientoIa] está activo, emplea la matriz de afinidad semántica
     * entrenada del refinador IA para valorar también intereses complementarios.
     */
    private fun compatibilidadDeIntereses(perfil: PerfilAgente, candidato: CandidatoMatch): Double {
        if (perfil.intereses.isEmpty()) return 0.0
        if (usarRefinamientoIa) {
            return refinadorIa.afinidadSemanticaIntereses(perfil.intereses, candidato.intereses)
        }
        val compartidos = interesesCompartidos(perfil, candidato).size
        return (compartidos.toDouble() / perfil.intereses.size).coerceIn(0.0, 1.0)
    }

    /** 1.0 si ambos tienen universidad registrada y coinciden (sin tildes/mayúsculas). */
    private fun compatibilidadUniversidad(perfil: PerfilAgente, candidato: CandidatoMatch): Double {
        if (perfil.universidad.isBlank() || candidato.universidad.isBlank()) return 0.0
        return if (normalizar(perfil.universidad) == normalizar(candidato.universidad)) 1.0 else 0.0
    }

    /**
     * 1.0 si ambos tienen objetivo ("goal") registrado y coinciden exactamente,
     * o 0.5 si en modo IA sus objetivos son complementarios (ej. "Tengo tiempo libre"
     * y "Qué hay para hacer").
     */
    private fun compatibilidadMeta(perfil: PerfilAgente, candidato: CandidatoMatch): Double {
        val mia = normalizarMeta(perfil.meta)
        val suya = normalizarMeta(candidato.meta)
        if (mia.isEmpty() || suya.isEmpty()) return 0.0
        if (mia == suya) return 1.0
        if (usarRefinamientoIa) {
            val metasSociales = setOf("que hay para hacer", "tengo tiempo libre", "aun no lo tengo claro")
            if (mia in metasSociales && suya in metasSociales) return 0.5
        }
        return 0.0
    }

    /**
     * Qué tanto se solapan los huecos libres de ambos (0.0 - 1.0).
     *
     * Es los minutos compartidos divididos por el menor tiempo libre total,
     * así 1.0 significa "el que tiene menos tiempo libre lo tiene todo en
     * común".
     */
    private fun solapeDeHuecos(
        enComun: List<FranjaHoraria>,
        mios: List<FranjaHoraria>,
        suyos: List<FranjaHoraria>,
    ): Double {
        val menorTotal = minOf(
            mios.sumOf { it.duracionMinutos },
            suyos.sumOf { it.duracionMinutos },
        )
        if (menorTotal <= 0) return 0.0
        return (enComun.sumOf { it.duracionMinutos }.toDouble() / menorTotal).coerceIn(0.0, 1.0)
    }

    /** Intereses del candidato que también están en mi perfil (sin tildes ni mayúsculas). */
    private fun interesesCompartidos(
        perfil: PerfilAgente,
        candidato: CandidatoMatch,
    ): List<String> {
        val mios = perfil.intereses.map { normalizar(it) }.toSet()
        return candidato.intereses.filter { normalizar(it) in mios }
    }

    /** Minutos que dos listas de franjas comparten (franja por franja, mismo día). */
    private fun huecosEnComun(
        mios: List<FranjaHoraria>,
        suyos: List<FranjaHoraria>,
    ): List<FranjaHoraria> {
        val compartidas = mutableListOf<FranjaHoraria>()
        mios.forEach { mio ->
            suyos.forEach { suyo ->
                val solape = mio.minutosDeSolapeCon(suyo)
                if (solape > 0) {
                    val dia = mio.dia
                    val inicio = maxOf(mio.inicioMinutos, suyo.inicioMinutos)
                    val fin = minOf(mio.finMinutos, suyo.finMinutos)
                    val franja = runCatching { FranjaHoraria(dia, inicio, fin) }.getOrNull()
                    if (franja != null) compartidas += franja
                }
            }
        }
        // Fusiona solapes del mismo tramo para no contar dos veces el mismo tiempo.
        return fusionar(compartidas.sortedBy { it.inicioMinutos })
    }

    /** Une franjas solapadas o pegadas del mismo día en una sola. */
    private fun fusionar(franjas: List<FranjaHoraria>): List<FranjaHoraria> {
        val resultado = mutableListOf<FranjaHoraria>()
        franjas.groupBy { it.dia }.forEach { (_, delDia) ->
            val ordenadas = delDia.sortedBy { it.inicioMinutos }
            var actual: FranjaHoraria? = null
            ordenadas.forEach { franja ->
                val acumulada = actual
                actual = when {
                    acumulada == null -> franja
                    franja.inicioMinutos <= acumulada.finMinutos -> runCatching {
                        FranjaHoraria(
                            acumulada.dia,
                            acumulada.inicioMinutos,
                            maxOf(acumulada.finMinutos, franja.finMinutos),
                        )
                    }.getOrNull() ?: acumulada

                    else -> {
                        resultado += acumulada
                        franja
                    }
                }
            }
            actual?.let { resultado += it }
        }
        return resultado.sortedBy { it.inicioMinutos }
    }

    /** Explicación legible de cada sugerencia (lo que se muestra en pantalla). */
    private fun razonesDe(
        perfil: PerfilAgente,
        candidato: CandidatoMatch,
        enComun: List<FranjaHoraria>,
    ): List<String> {
        val razones = mutableListOf<String>()
        val compartidos = interesesCompartidos(perfil, candidato)
        val paresAfines = if (usarRefinamientoIa) {
            refinadorIa.paresSemanticosAfines(perfil.intereses, candidato.intereses)
        } else {
            emptyList()
        }

        when {
            compartidos.isNotEmpty() ->
                razones += strings.reasonInterestsMatch(
                    compartidos.joinToString(", ") { strings.interestLabel(it) },
                )

            paresAfines.isNotEmpty() -> {
                val etiquetasAfines = paresAfines
                    .map { (_, suyo) -> strings.interestLabel(suyo) }
                    .distinct()
                    .joinToString(", ")
                razones += strings.reasonInterestsMatch(etiquetasAfines)
            }

            perfil.intereses.isNotEmpty() ->
                razones += strings.reasonNoInterestsMatch

            else ->
                razones += strings.reasonNoInterestsRegistered
        }

        if (compatibilidadUniversidad(perfil, candidato) > 0.0) {
            razones += candidato.universidad
        }

        if (pesos.meta > 0.0 && compatibilidadMeta(perfil, candidato) >= 1.0 && candidato.meta.isNotBlank()) {
            razones += candidato.meta
        }

        val mejor = enComun.maxByOrNull { it.duracionMinutos }
        if (mejor != null) razones += strings.reasonSharedFreeSlot(mostrarFranja(mejor))

        return razones
    }

    private fun mostrarFranja(franja: FranjaHoraria): String =
        "${strings.dayName(franja.dia.titulo)} " +
            "${FranjaHoraria.aHora(franja.inicioMinutos)}-${FranjaHoraria.aHora(franja.finMinutos)}"

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

    /** Quita emojis iniciales (📚, 🏀, 😉, 🥺) antes de comparar metas. */
    private fun normalizarMeta(meta: String): String =
        normalizar(meta).replace(Regex("^[^a-z0-9]+"), "").trim()
}

/** Salida de la etapa de percepción del agente de Matches. */
internal data class PairListas(
    val compatibles: List<CandidatoMatch>,
    val descartados: List<DescarteMatch>,
)
