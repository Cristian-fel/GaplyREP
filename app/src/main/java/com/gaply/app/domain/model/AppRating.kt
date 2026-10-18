package com.gaply.app.domain.model

/**
 * Calificación de la app (Módulo 2 · Calificar la aplicación).
 *
 * @param id identificador único de la calificación (sirve para "la última").
 * @param stars estrellas dadas, de 1 a 5.
 * @param comment motivos escritos por el usuario (opcional).
 * @param createdAt marca de tiempo en millis, para ordenar las calificaciones.
 */
data class AppRating(
    val id: String = "",
    val stars: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L,
)
