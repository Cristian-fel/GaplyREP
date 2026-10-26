package com.gaply.app.ui.screens.home

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TheaterComedy
import com.gaply.app.R
import com.gaply.app.core.i18n.AppStrings

/**
 * Contenido fijo del Descubrir (lugares del campus y agenda cultural).
 * Vive aparte de [DiscoverTab] para que la UI no cargue con datos:
 * más adelante pueden venir de Firestore sin tocar composables.
 */

internal enum class PlaceKey { BIBLIOTECA, CANCHA, SALON, ZONA_VERDE, AUDITORIO }

internal data class PlaceItem(
    val key: PlaceKey,
    val imageRes: Int,
    val icon: ImageVector,
)

/** Categoría canónica (de los 9 intereses) que representa cada lugar del campus. */
internal fun categoriaDe(key: PlaceKey): String = when (key) {
    PlaceKey.BIBLIOTECA -> "Estudio"
    PlaceKey.CANCHA -> "Deportes"
    PlaceKey.SALON -> "Baile"
    PlaceKey.ZONA_VERDE -> "Eventos"
    PlaceKey.AUDITORIO -> "Música"
}

internal fun placeTitle(key: PlaceKey, s: AppStrings): String = when (key) {
    PlaceKey.BIBLIOTECA -> s.placeLibrary
    PlaceKey.CANCHA -> s.placeCourt
    PlaceKey.SALON -> s.placeMultiRoom
    PlaceKey.ZONA_VERDE -> s.placeGreenZone
    PlaceKey.AUDITORIO -> s.placeAuditorium
}

internal data class CulturalEventItem(
    val title: String,
    val imageRes: Int,
    val url: String,
)

internal val suggestedPlaces = listOf(
    PlaceItem(PlaceKey.BIBLIOTECA, R.drawable.img_biblioteca, Icons.Default.AccountBalance),
    PlaceItem(PlaceKey.CANCHA, R.drawable.img_cancha, Icons.Default.SportsSoccer),
    PlaceItem(PlaceKey.SALON, R.drawable.img_gimnasio, Icons.Default.FitnessCenter),
    PlaceItem(PlaceKey.ZONA_VERDE, R.drawable.img_zona_verde, Icons.Default.Park),
    PlaceItem(PlaceKey.AUDITORIO, R.drawable.img_auditorio, Icons.Default.TheaterComedy),
)

internal val culturalEvents = listOf(
    CulturalEventItem(
        title = "Expo Tadeo",
        imageRes = R.drawable.img_expotadeo,
        url = "https://www.utadeo.edu.co/es/eventos/expotadeo-fest",
    ),
    CulturalEventItem(
        title = "Cátedra",
        imageRes = R.drawable.img_catedra,
        url = "https://www.utadeo.edu.co/es/eventos/presupuesto-deuda-y-crecimiento-los-desafios-que-enfrenta-colombia-para-sostener-su-estado",
    ),
    CulturalEventItem(
        title = "Cabito Fest",
        imageRes = R.drawable.img_cabitofest,
        url = "https://www.utadeo.edu.co/es/noticia/especiales/sistema-de-bibliotecas/104046/participa-con-tu-emprendimiento-en-cabito-fest-2026",
    ),
    CulturalEventItem(
        title = "Torneo de Ajedrez",
        imageRes = R.drawable.img_ajedrez,
        url = "https://www.utadeo.edu.co/es/noticia/novedades/sistema-de-bibliotecas/104046/inscripciones-abiertas-torneo-de-ajedrez",
    ),
)

internal const val URL_AGENDA_GENERAL =
    "https://www.utadeo.edu.co/es/eventos?viewsreference%5Bcompressed%5D=eJxdkNEKgzAMRf8lzz44x5jzZ0pGYw20VWp0iPjva-lm2R5Kc3PP5UJ20CgI3Q4YzOLIC3R-sbaCgVBTgK6uwLLjcz_2_UynmtAkKAthsRQTRwXk8WlJq4gKezP_NpTxDH3-0vsdjlQSIqviY9mUbFPi4xJNwGmAf4B1tC_3tmmL1TNZrTy6FM1iZXoVINDKM48-p29t87hGM5Ms5JQmmw5VH2_zqWo_&page=1"
