package com.gaply.app.domain.agent

/**
 * Lo que el agente percibe del usuario: sus intereses (la app permite hasta 3),
 * sus huecos libres calculados a partir del horario, su universidad y su
 * objetivo ("goal") en Gaply.
 */
data class PerfilAgente(
    val intereses: List<String> = emptyList(),
    val huecos: List<FranjaHoraria> = emptyList(),
    val universidad: String = "",
    val meta: String = "",
)
