package com.gaply.app.domain.model

import com.gaply.app.domain.agent.DiaSemana
import com.gaply.app.domain.agent.FranjaHoraria

/**
 * Clase o actividad del horario del usuario (Módulo 2B · Mi horario).
 *
 * Las horas se guardan en formato 24 h ("10:00") y se muestran como en el
 * Figma ("10:00 a.m."). Las fechas se guardan como "dd/MM/yyyy".
 */
data class ScheduleClass(
    val id: String = "",
    val title: String = "",
    val day: String = "Lunes",
    val startDate: String = "",
    val endDate: String = "",
    val startHour: String = "08:00",
    val endHour: String = "10:00",
    val note: String = "",
    val color: String = COLOR_POR_DEFECTO,
) {
    val startMinutes: Int get() = minutosDeHora(startHour)

    val endMinutes: Int get() = minutosDeHora(endHour)

    /** Rango legible del Figma: "10:00 a.m. - 12:00 p.m." */
    val rangoHorario: String
        get() = "${formatearHora(startHour)} - ${formatearHora(endHour)}"

    /** Convierte la clase en una franja que entiende el agente (null si los datos no son válidos). */
    fun aFranjaHoraria(): FranjaHoraria? {
        val dia = DiaSemana.values().firstOrNull { it.titulo == day } ?: return null
        if (endMinutes <= startMinutes) return null
        return FranjaHoraria(dia, startMinutes, endMinutes)
    }

    companion object {

        /** Paleta del Figma ("Seleccione un color"), en el mismo orden. */
        val COLORES = listOf(
            "#F2A9E8", "#C469AC", "#A88BD3",
            "#F26B60", "#B85C70", "#EA9770",
            "#DCB040", "#F0A96B", "#92A3CD",
            "#5CB9DD", "#3E7DA8", "#51A391",
            "#82B192", "#528F61", "#A06184",
        )

        const val COLOR_POR_DEFECTO = "#51A391"

        /** "10:00" -> 600. */
        fun minutosDeHora(hora: String): Int {
            val partes = hora.trim().split(":")
            val horas = partes.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
            val minutos = partes.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
            return horas * 60 + minutos
        }

        /** "10:00" -> "10:00 a.m." · "12:00" -> "12:00 p.m." · "00:15" -> "12:15 a.m." */
        fun formatearHora(hora: String): String {
            val totalMinutos = minutosDeHora(hora)
            val hora24 = totalMinutos / 60
            val minutos = totalMinutos % 60
            val sufijo = if (hora24 < 12) "a.m." else "p.m."
            val hora12 = when {
                hora24 == 0 || hora24 == 12 -> 12
                hora24 > 12 -> hora24 - 12
                else -> hora24
            }
            return "%d:%02d %s".format(hora12, minutos, sufijo)
        }
    }
}

/**
 * Franja de tiempo libre entre dos clases: el "hueco" que ve el usuario y
 * donde el agente de utilidad propone actividades.
 */
data class HuecoLibre(
    val inicioMinutos: Int,
    val finMinutos: Int,
) {
    val duracionMinutos: Int get() = finMinutos - inicioMinutos

    val inicioHora: String get() = "%02d:%02d".format(inicioMinutos / 60, inicioMinutos % 60)

    val finHora: String get() = "%02d:%02d".format(finMinutos / 60, finMinutos % 60)

    /** "2 horas libres" · "1 hora libre" · "90 minutos libres" */
    val textoDuracion: String
        get() = when {
            duracionMinutos >= 60 && duracionMinutos % 60 == 0 -> {
                val horas = duracionMinutos / 60
                if (horas == 1) "1 hora libre" else "$horas horas libres"
            }

            duracionMinutos >= 60 -> "${duracionMinutos / 60} h ${duracionMinutos % 60} min libres"
            else -> "$duracionMinutos minutos libres"
        }

    fun aFranjaHoraria(dia: DiaSemana): FranjaHoraria = FranjaHoraria(dia, inicioMinutos, finMinutos)

    companion object {

        /**
         * Huecos de al menos [minimoMinutos] entre clases consecutivas del día,
         * en orden. Los usa la pantalla del horario y el banner del Home
         * (el mismo dato con el que trabaja el agente de utilidad).
         */
        fun deClases(clases: List<ScheduleClass>, minimoMinutos: Int): List<HuecoLibre> {
            return clases
                .filter { it.endMinutes > it.startMinutes }
                .sortedBy { it.startMinutes }
                .zipWithNext()
                .mapNotNull { (anterior, siguiente) ->
                    val inicio = anterior.endMinutes
                    val fin = siguiente.startMinutes
                    if (fin - inicio >= minimoMinutos) {
                        HuecoLibre(inicioMinutos = inicio, finMinutos = fin)
                    } else {
                        null
                    }
                }
        }
    }
}
