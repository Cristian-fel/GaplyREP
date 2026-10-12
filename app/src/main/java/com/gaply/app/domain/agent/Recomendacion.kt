package com.gaply.app.domain.agent

/**
 * Salida del agente para una actividad que sí encaja en el horario.
 *
 * @param puntuacion utilidad en escala 0-100 (mayor = mejor opción).
 * @param razones explicación legible de por qué se sugiere, para mostrarla al usuario.
 * @param huecoAprovechado hueco libre del usuario en el que entra la actividad.
 */
data class Recomendacion(
    val actividad: Actividad,
    val puntuacion: Int,
    val razones: List<String>,
    val huecoAprovechado: FranjaHoraria,
)

/** Candidato descartado y el motivo, para poder explicar el "por qué no". */
data class Descarte(
    val actividad: Actividad,
    val motivo: String,
)

/** Resultado completo de una corrida del agente: lo sugerido y lo descartado. */
data class ResultadoAgente(
    val recomendaciones: List<Recomendacion>,
    val descartadas: List<Descarte>,
)
