package com.gaply.app.ui.screens.activities

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.ScheduleClass
import com.gaply.app.ui.theme.BrandGreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Módulo de Actividades · Formatos compartidos del módulo.
 * No toca utilidades existentes; solo agrega lo propio del módulo.
 */
internal object ActivityFormat {

    /** Valores canónicos (guardados tal cual) del selector de duración. */
    const val DUR_30 = "30 min"
    const val DUR_1H = "1 hora"
    const val DUR_2H = "2 horas"

    private val longDate: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

    private val shortDateSlash: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

    /** "lunes, 22 de septiembre de 2026" (en el idioma de la app). */
    fun fechaLarga(millis: Long, locale: Locale): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val text = longDate.withLocale(locale).format(date)
        return text.replaceFirstChar { it.uppercase(locale) }
    }

    /** "22/09/2026" (tarjetas del formulario, en el idioma de la app). */
    fun fechaCorta(millis: Long, locale: Locale): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        return shortDateSlash.withLocale(locale).format(date)
    }

    /** "14:00" -> "2:00 p.m." reutilizando el formateador del proyecto. */
    fun hora12(hora24: String): String = ScheduleClass.formatearHora(hora24)

    /** Suma minutos a una hora "HH:mm" y devuelve "HH:mm". */
    fun masMinutos(hora24: String, minutos: Int): String {
        val total = (ScheduleClass.minutosDeHora(hora24) ?: 0) + minutos
        val normalizado = ((total % 1440) + 1440) % 1440
        return "%02d:%02d".format(normalizado / 60, normalizado % 60)
    }

    /** "12:00" + 60min -> "12:00 p.m. - 1:00 p.m.". */
    fun rangoHora(inicio24: String, duracionMin: Int): String {
        val fin24 = masMinutos(inicio24, duracionMin)
        return "${hora12(inicio24)} - ${hora12(fin24)}"
    }

    /** Minutos que representa cada duración del selector segmentado. */
    fun duracionMinutos(duracion: String): Int = when (duracion) {
        DUR_30 -> 30
        DUR_2H -> 120
        else -> 60
    }

    /** Millis de hoy a las 12:00 (valor inicial de fecha + hora). */
    fun hoyAMediodia(): Long {
        val today = LocalDate.now()
        return today.atTime(12, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /** Color sólido por defecto cuando no hay foto (verde de la paleta). */
    fun colorSolido(hex: String?): Color {
        val safe = hex?.takeIf { it.isNotBlank() } ?: ActivityItem.DEFAULT_COLOR_HEX
        return try {
            Color(AndroidColor.parseColor(safe))
        } catch (e: IllegalArgumentException) {
            BrandGreen
        }
    }
}
