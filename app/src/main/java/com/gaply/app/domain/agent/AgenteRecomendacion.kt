package com.gaply.app.domain.agent

import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.StringsEs
import java.text.Normalizer
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pesos de la función de utilidad. Deben sumar 1.0.
 *
 * @param intereses cuánto pesa la compatibilidad con los intereses del usuario.
 * @param horario cuánto pesa el encaje de la actividad en un hueco libre.
 */
data class PesosUtilidad(
    val intereses: Double = 0.65,
    val horario: Double = 0.35,
) {
    init {
        require(intereses >= 0.0 && horario >= 0.0) { "Los pesos no pueden ser negativos." }
        require(abs(intereses + horario - 1.0) < 0.0001) { "La suma de los pesos debe ser 1.0." }
    }
}

/** Resultado de la etapa de percepción: lo que sirve y lo que se descarta. */
internal data class Percibidas(
    val compatibles: List<Actividad>,
    val descartadas: List<Descarte>,
)

/**
 * Agente de utilidad de Gaply (materia de Inteligencia Artificial).
 *
 * Ejecuta el ciclo percepción -> decisión -> acción:
 *
 *  1. **Percepción**: cruza los huecos libres del usuario con las candidatas y
 *     descarta las que no caben en ningún hueco (guardando el motivo).
 *  2. **Decisión**: calcula la utilidad de cada candidata con una función de
 *     pesos: compatibilidad de intereses + encaje en el horario.
 *  3. **Acción**: devuelve las recomendaciones ordenadas de mayor a menor
 *     utilidad, con su puntuación (0-100) y las razones que las justifican.
 *
 * Es puro Kotlin: no depende de Android, de la interfaz ni de Firestore, por
 * eso puede probarse con datos controlados (ver `AgenteRecomendacionTest`).
 */
class AgenteRecomendacion(
    private val strings: AppStrings = StringsEs,
    val pesos: PesosUtilidad = PesosUtilidad(),
    val maximoRecomendaciones: Int = 5,
) {
    init {
        require(maximoRecomendaciones > 0) { "El máximo de recomendaciones debe ser positivo." }
    }

    /** Ejecuta el ciclo completo del agente. */
    fun recomendar(perfil: PerfilAgente, candidatas: List<Actividad>): ResultadoAgente {
        val (compatibles, descartadas) = percibir(perfil, candidatas)
        val evaluadas = decidir(perfil, compatibles)
        return actuar(evaluadas, descartadas)
    }

    /** 1. PERCEPCIÓN: solo sobrevive lo que encaja en algún hueco libre. */
    internal fun percibir(perfil: PerfilAgente, candidatas: List<Actividad>): Percibidas {
        val compatibles = mutableListOf<Actividad>()
        val descartadas = mutableListOf<Descarte>()

        candidatas.forEach { actividad ->
            if (actividad.franja.enAlgunHueco(perfil.huecos)) {
                compatibles += actividad
            } else {
                descartadas += Descarte(
                    actividad = actividad,
                    motivo = strings.reasonNoFreeSlot(mostrarFranja(actividad.franja)),
                )
            }
        }
        return Percibidas(compatibles = compatibles, descartadas = descartadas)
    }

    /** 2. DECISIÓN: cada candidata recibe su utilidad (0-100) y sus razones. */
    internal fun decidir(perfil: PerfilAgente, compatibles: List<Actividad>): List<Recomendacion> {
        return compatibles.mapNotNull { actividad ->
            val hueco = huecoAprovechado(actividad, perfil.huecos) ?: return@mapNotNull null

            val utilidadIntereses = compatibilidadDeIntereses(perfil, actividad)
            val utilidadHorario = encajeHorario(actividad, hueco)
            val utilidad = pesos.intereses * utilidadIntereses + pesos.horario * utilidadHorario

            Recomendacion(
                actividad = actividad,
                puntuacion = (utilidad * 100).roundToInt().coerceIn(0, 100),
                razones = razonesDe(perfil, actividad, hueco),
                huecoAprovechado = hueco,
            )
        }
    }

    /** 3. ACCIÓN: ordena de mayor a menor utilidad y limita el resultado. */
    internal fun actuar(
        evaluadas: List<Recomendacion>,
        descartadas: List<Descarte>,
    ): ResultadoAgente {
        val ordenadas = evaluadas
            .sortedWith(
                compareByDescending<Recomendacion> { it.puntuacion }
                    .thenBy { it.actividad.nombre.lowercase() },
            )
            .take(maximoRecomendaciones)

        return ResultadoAgente(recomendaciones = ordenadas, descartadas = descartadas)
    }

    // ------------------------------------------------------------------
    // Funciones de utilidad
    // ------------------------------------------------------------------

    /** Hueco donde mejor encaja la actividad: primero la que la contiene con menos sobrante. */
    private fun huecoAprovechado(
        actividad: Actividad,
        huecos: List<FranjaHoraria>,
    ): FranjaHoraria? {
        val solapados = huecos.filter { it.solapaCon(actividad.franja) }
        if (solapados.isEmpty()) return null

        return solapados.minByOrNull { hueco ->
            if (actividad.franja.estaContenidaEn(hueco)) {
                // Contenida: nos quedamos con el hueco del que menos sobra.
                hueco.duracionMinutos - actividad.franja.duracionMinutos
            } else {
                // No cabe completa: priorizamos el hueco con más minutos de solape.
                COSTO_MAXIMO - hueco.minutosDeSolapeCon(actividad.franja)
            }
        }
    }

    /** Proporción de los intereses del usuario que cubre la actividad (0.0 - 1.0). */
    private fun compatibilidadDeIntereses(perfil: PerfilAgente, actividad: Actividad): Double {
        if (perfil.intereses.isEmpty()) return 0.0
        val compartidos = interesesCompartidos(perfil, actividad).size
        return (compartidos.toDouble() / perfil.intereses.size).coerceIn(0.0, 1.0)
    }

    /** Qué tan bien aprovecha el hueco la actividad (1.0 si cabe completa). */
    private fun encajeHorario(actividad: Actividad, hueco: FranjaHoraria): Double {
        if (actividad.franja.estaContenidaEn(hueco)) return 1.0
        val solape = hueco.minutosDeSolapeCon(actividad.franja)
        return (solape.toDouble() / actividad.franja.duracionMinutos).coerceIn(0.0, 1.0)
    }

    /** Intereses de la actividad que también están en el perfil (comparación sin tildes ni mayúsculas). */
    private fun interesesCompartidos(perfil: PerfilAgente, actividad: Actividad): List<String> {
        val delPerfil = perfil.intereses.map { normalizar(it) }.toSet()
        return actividad.intereses.filter { normalizar(it) in delPerfil }
    }

    /** Explicación legible de cada recomendación (lo que se muestra en pantalla). */
    private fun razonesDe(
        perfil: PerfilAgente,
        actividad: Actividad,
        hueco: FranjaHoraria,
    ): List<String> {
        val razones = mutableListOf<String>()
        val compartidos = interesesCompartidos(perfil, actividad)

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

        if (actividad.franja.estaContenidaEn(hueco)) {
            razones += strings.reasonFitsCompletely(mostrarFranja(hueco))
        } else {
            val solape = hueco.minutosDeSolapeCon(actividad.franja)
            razones += strings.reasonPartiallyUses(solape, mostrarFranja(hueco))
        }
        return razones
    }

    private fun mostrarFranja(franja: FranjaHoraria): String =
        "${strings.dayName(franja.dia.titulo)} " +
            "${FranjaHoraria.aHora(franja.inicioMinutos)}-${FranjaHoraria.aHora(franja.finMinutos)}"

    private fun FranjaHoraria.enAlgunHueco(huecos: List<FranjaHoraria>): Boolean =
        huecos.any { solapaCon(it) }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

    private companion object {
        /** Costo mayor que cualquier sobrante posible (máx. 1440 min/día). */
        const val COSTO_MAXIMO = 10_000
    }
}
