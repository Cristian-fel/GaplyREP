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
import java.util.Locale

/**
 * Módulo de Actividades · Formatos compartidos del módulo.
 * No toca utilidades existentes; solo agrega lo propio del módulo.
 */
internal object ActivityFormat {

    private val locale = Locale("es", "CO")

    private val longDate: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", locale)

    private val shortDateSlash: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)

    /** "lunes, 22 de septiembre de 2026" -> "Lunes, 22 de septiembre de 2026". */
    fun fechaLarga(millis: Long): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val text = longDate.format(date)
        return text.replaceFirstChar { it.uppercase(locale) }
    }

    /** "22/09/2026" (tarjetas del formulario). */
    fun fechaCorta(millis: Long): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        return shortDateSlash.format(date)
    }

    /** "14:00" -> "2:00 p.m." reutilizando el formateador del proyecto. */
    fun hora12(hora24: String): String = ScheduleClass.formatearHora(hora24)

    /** Suma minutos a una hora "HH:mm" y devuelve "HH:mm". */
    fun masMinutos(hora24: String, minutos: Int): String {
        val total = ScheduleClass.minutosDeHora(hora24) + minutos
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
        "30 min" -> 30
        "2 horas" -> 120
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
