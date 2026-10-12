package com.gaply.app.domain.agent

/**
 * Lo que el agente percibe del usuario: sus intereses (la app permite hasta 3)
 * y sus huecos libres calculados a partir del horario.
 */
data class PerfilAgente(
    val intereses: List<String> = emptyList(),
    val huecos: List<FranjaHoraria> = emptyList(),
)
