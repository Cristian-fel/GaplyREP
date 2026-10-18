package com.gaply.app.core.i18n

import java.util.Locale

/**
 * Los 8 idiomas disponibles en Configuración · Idioma.
 *
 * [label] es el texto que se muestra en la lista (en su propio idioma,
 * tal como en el Figma) y es además la clave antigua que se guardaba en
 * SharedPreferences, por lo que [fromStored] acepta tanto el id estable
 * ("es", "en", ...) como cualquier label legado para no romper instalaciones
 * existentes.
 */
enum class AppLanguage(
    val id: String,
    val label: String,
    val locale: Locale,
) {
    ES("es", "Español (ES)", Locale("es")),
    EN("en", "English (Us)", Locale("en", "US")),
    FR("fr", "Français", Locale("fr")),
    DE("de", "Deutsch (DE)", Locale("de")),
    IT("it", "Italiano (IT)", Locale("it")),
    PT("pt", "Português (BR)", Locale("pt", "BR")),
    JA("ja", "日本語(JP)", Locale("ja", "JP")),
    ZH("zh", "中國人(中國)", Locale("zh", "CN")),
    ;

    companion object {
        val DEFAULT = ES

        /** Acepta el id estable ("de") o el label legado ("Deutsch (DE)"). */
        fun fromStored(value: String?): AppLanguage =
            entries.firstOrNull { it.id == value || it.label == value } ?: DEFAULT
    }
}
