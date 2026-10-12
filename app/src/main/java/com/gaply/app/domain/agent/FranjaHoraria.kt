package com.gaply.app.domain.agent

/** Días de la semana que entiende el agente. */
enum class DiaSemana(val titulo: String) {
    LUNES("Lunes"),
    MARTES("Martes"),
    MIERCOLES("Miércoles"),
    JUEVES("Jueves"),
    VIERNES("Viernes"),
    SABADO("Sábado"),
    DOMINGO("Domingo"),
}

/**
 * Bloque de tiempo (día + inicio + fin) expresado en minutos desde las 00:00.
 *
 * Es la unidad mínima que el agente percibe del horario: tanto los huecos
 * libres del usuario como la franja de cada actividad candidata se representan
 * con este mismo tipo, lo que permite cruzarlos sin ambigüedad.
 */
data class FranjaHoraria(
    val dia: DiaSemana,
    val inicioMinutos: Int,
    val finMinutos: Int,
) {
    init {
        require(inicioMinutos >= 0) { "La hora de inicio no puede ser negativa." }
        require(finMinutos > inicioMinutos) { "La hora final debe ser posterior a la inicial." }
        require(finMinutos <= MINUTOS_POR_DIA) { "La hora final no puede superar las 24:00." }
    }

    val duracionMinutos: Int get() = finMinutos - inicioMinutos

    /** Minutos que comparten esta franja y [otra] (0 si son de días distintos). */
    fun minutosDeSolapeCon(otra: FranjaHoraria): Int {
        if (dia != otra.dia) return 0
        val inicio = maxOf(inicioMinutos, otra.inicioMinutos)
        val fin = minOf(finMinutos, otra.finMinutos)
        return maxOf(0, fin - inicio)
    }

    /** Indica si ambas franjas comparten tiempo. */
    fun solapaCon(otra: FranjaHoraria): Boolean = minutosDeSolapeCon(otra) > 0

    /** Indica si esta franja cabe completa dentro de [contenedora]. */
    fun estaContenidaEn(contenedora: FranjaHoraria): Boolean =
        dia == contenedora.dia &&
            inicioMinutos >= contenedora.inicioMinutos &&
            finMinutos <= contenedora.finMinutos

    /** Texto legible, por ejemplo: "Martes 10:00-12:00". */
    override fun toString(): String = "${dia.titulo} ${aHora(inicioMinutos)}-${aHora(finMinutos)}"

    companion object {
        private const val MINUTOS_POR_DIA = 24 * 60

        /** Crea una franja a partir de horas legibles, por ejemplo: desde(MARTES, "10:00", "12:00"). */
        fun desde(dia: DiaSemana, inicio: String, fin: String): FranjaHoraria =
            FranjaHoraria(dia, aMinutos(inicio), aMinutos(fin))

        /** Convierte "7:30" en minutos (450). Lanza [IllegalArgumentException] si la hora no es válida. */
        fun aMinutos(hora: String): Int {
            val partes = hora.trim().split(":")
            val horas = partes.getOrNull(0)?.trim()?.toIntOrNull()
                ?: throw IllegalArgumentException("Hora inválida: $hora")
            val minutos = partes.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
            require(horas in 0..23 && minutos in 0..59) { "Hora inválida: $hora" }
            return horas * 60 + minutos
        }

        /** Convierte 630 en "10:30". */
        fun aHora(minutos: Int): String = "%02d:%02d".format(minutos / 60, minutos % 60)
    }
}
