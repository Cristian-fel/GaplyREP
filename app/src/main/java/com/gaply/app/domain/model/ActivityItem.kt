package com.gaply.app.domain.model

/**
 * Módulo de Actividades · Modelo de una actividad pública creada por un usuario.
 *
 * La lista de actividades nace vacía en [com.gaply.app.ui.screens.activities.ActivityViewModel]
 * y solo crece cuando alguien publica desde el formulario "Nueva actividad".
 */
data class ActivityItem(
    /** Identificador único (UUID generado al publicar). */
    val id: String,
    /** Título del evento, ej. "Partido de fútbol". */
    val title: String,
    /** Uno de los 9 intereses de la app, ej. "Deportes". */
    val category: String,
    /** Duración estimada: "30 min", "1 hora" o "2 horas". */
    val duration: String,
    /** Fecha en ISO "yyyy-MM-dd" (se formatea en español al mostrarla). */
    val date: String,
    /** Rango legible, ej. "12:00 p.m. - 2:00 p.m.". */
    val timeRange: String,
    /** Lugar escrito por el usuario, ej. "Cancha M7A". */
    val location: String,
    /** Descripción opcional del evento. */
    val description: String = "",
    /** Foto del lugar elegida desde la galería; null si no hubo foto. */
    val imageUri: String? = null,
    /** Color sólido por defecto (verde de la paleta) cuando [imageUri] es null. */
    val defaultColorHex: String = DEFAULT_COLOR_HEX,
    /** Inicial de quien organiza la actividad. */
    val organizerInitial: String = "T",
    /** Nombres de los asistentes (crece al unirse la gente). */
    val attendees: List<String> = emptyList(),
    /** true cuando el usuario actual ya se unió ("Ya está dentro"). */
    val isJoined: Boolean = false,
    /** true cuando esta actividad la publicó el usuario actual ("Tus actividades"). */
    val isCreatedByMe: Boolean = true,
    /** Hora de inicio en 24h "HH:mm" (se guarda aparte para poder reeditarla). */
    val startTime: String = "12:00",
) {
    companion object {
        /** Tono verde de la paleta de la app usado como "imagen" por defecto. */
        const val DEFAULT_COLOR_HEX = "#1A5C48"
    }
}
