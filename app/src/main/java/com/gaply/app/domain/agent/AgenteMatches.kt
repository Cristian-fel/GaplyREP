package com.gaply.app.domain.agent

import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.StringsEs
import java.text.Normalizer
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pesos de la función de utilidad de Matches. Deben sumar 1.0.
 *
 * @param intereses cuánto pesa la compatibilidad de intereses (0.60).
 * @param horario   cuánto pesa el solape de huecos libres (0.40).
 */
data class PesosUtilidadMatches(
    val intereses: Double = 0.60,
    val horario: Double = 0.40,
) {
    init {
        require(intereses >= 0.0 && horario >= 0.0) { "Los pesos no pueden ser negativos." }
        require(abs(intereses + horario - 1.0) < 0.0001) { "La suma de los pesos debe ser 1.0." }
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
)

/** Candidato que superó la percepción, con su puntuación y sus razones. */
data class PuntuacionMatch(
    val candidato: CandidatoMatch,
    val puntuacion: Int,
    val razones: List<String>,
    val huecosEnComun: List<FranjaHoraria>,
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
 * [AgenteRecomendacion], con su propia función de utilidad para perfiles:
 *
 *  utilidad = 0.60 x compatibilidadDeIntereses + 0.40 x solapeDeHuecosLibres
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

    /**
     * 1. PERCEPCIÓN: descarta solo al que teniendo horario no solapa con el
     * del usuario. Sin horario de alguno de los dos no se puede concluir nada
     * y el candidato sigue en el mazo (luego puntúa solo por intereses).
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
            if (faltaHorario || enComun.isNotEmpty()) {
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
        val utilidad = pesos.intereses * utilidadIntereses + pesos.horario * utilidadHorario

        PuntuacionMatch(
            candidato = candidato,
            puntuacion = (utilidad * 100).roundToInt().coerceIn(0, 100),
            razones = razonesDe(perfil, candidato, enComun),
            huecosEnComun = enComun,
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

    /** Proporción de MIS intereses que también tiene el candidato (0.0 - 1.0). */
    private fun compatibilidadDeIntereses(perfil: PerfilAgente, candidato: CandidatoMatch): Double {
        if (perfil.intereses.isEmpty()) return 0.0
        val compartidos = interesesCompartidos(perfil, candidato).size
        return (compartidos.toDouble() / perfil.intereses.size).coerceIn(0.0, 1.0)
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

        when {
            compartidos.isNotEmpty() ->
                razones += strings.reasonInterestsMatch(
                    compartidos.joinToString(", ") { strings.interestLabel(it) },
                )

            perfil.intereses.isNotEmpty() ->
                razones += strings.reasonNoInterestsMatch

            else ->
                razones += strings.reasonNoInterestsRegistered
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
}

/** Salida de la etapa de percepción del agente de Matches. */
internal data class PairListas(
    val compatibles: List<CandidatoMatch>,
    val descartados: List<DescarteMatch>,
)
