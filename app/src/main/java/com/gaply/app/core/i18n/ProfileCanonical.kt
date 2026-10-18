package com.gaply.app.core.i18n

/**
 * Mapea universidad / género / objetivo entre la etiqueta localizada
 * (lo que ve el usuario) y la clave canónica en español (lo que se
 * persiste en Firestore). Las listas de todos los idiomas tienen el
 * mismo orden, así que basta la posición.
 */

/** Etiqueta localizada -> clave canónica ES (misma posición en la lista). */
fun canonicalFromLocalized(
    localized: String,
    localizedList: List<String>,
    canonicalList: List<String>,
): String {
    if (localized.isBlank()) return localized
    val idx = localizedList.indexOf(localized)
    return if (idx in canonicalList.indices) canonicalList[idx] else localized
}

/** Clave canónica ES -> etiqueta localizada (misma posición en la lista). */
fun localizedFromCanonical(
    canonical: String,
    localizedList: List<String>,
    canonicalList: List<String>,
): String {
    if (canonical.isBlank()) return canonical
    val idx = canonicalList.indexOf(canonical)
    return if (idx in localizedList.indices) localizedList[idx] else canonical
}
