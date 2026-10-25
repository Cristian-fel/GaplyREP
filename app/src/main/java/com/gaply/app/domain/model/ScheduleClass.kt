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
    val startMinutes: Int get() = minutosDeHora(startHour) ?: 0

    val endMinutes: Int get() = minutosDeHora(endHour) ?: 0

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

        /** "10:00" -> 600. Devuelve null si la hora está fuera de 00:00-23:59. */
        fun minutosDeHora(hora: String): Int? {
            val partes = hora.trim().split(":")
            val horas = partes.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
            val minutos = partes.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
            if (horas !in 0..23 || minutos !in 0..59) return null
            return horas * 60 + minutos
        }

        /** "10:00" -> "10:00 a.m." · "12:00" -> "12:00 p.m." · "00:15" -> "12:15 a.m." */
        fun formatearHora(hora: String): String {
            val totalMinutos = minutosDeHora(hora) ?: 0
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
         * Minutos mínimos para que un espacio entre dos clases se considere
         * "hueco libre". Misma regla en Mi horario, en el banner del Home y en
         * los candidatos del mazo de Matches.
         */
        const val MINIMO_MINUTOS = 60

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

/**
 * Hueco tocado en la banda del horario ("ver actividades para este hueco"):
 * el listado de actividades se filtra por ese día y ese rango exacto.
 */
data class FiltroFranja(
    val dia: java.time.LocalDate,
    val hueco: HuecoLibre,
)

/**
 * Todos los huecos libres de la semana, ya como [FranjaHoraria] (la unidad que
 * entienden los agentes).
 *
 * Es la misma lógica de "Mis huecos" aplicada a los 7 días: agrupa por día,
 * calcula los huecos entre clases consecutivas con [HuecoLibre.deClases] y los
 * convierte a franja. Los usa el agente de Matches para medir el solape entre
 * dos usuarios; los días que no son clave canónica en español se ignoran.
 */
fun huecosDelHorario(
    clases: List<ScheduleClass>,
    minimoMinutos: Int,
): List<FranjaHoraria> = clases
    .groupBy { it.day }
    .flatMap { (dia, delDia) ->
        val diaSemana = DiaSemana.values().firstOrNull { it.titulo == dia }
            ?: return@flatMap emptyList()
        HuecoLibre.deClases(delDia, minimoMinutos).map { it.aFranjaHoraria(diaSemana) }
    }

