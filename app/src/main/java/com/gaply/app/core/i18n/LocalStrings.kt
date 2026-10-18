package com.gaply.app.core.i18n

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/** Textos activos de la app; se provee desde MainActivity según el idioma guardado. */
val LocalStrings = staticCompositionLocalOf<AppStrings> { StringsEs }

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ES -> StringsEs
    AppLanguage.EN -> StringsEn
    AppLanguage.FR -> StringsFr
    AppLanguage.DE -> StringsDe
    AppLanguage.IT -> StringsIt
    AppLanguage.PT -> StringsPt
    AppLanguage.JA -> StringsJa
    AppLanguage.ZH -> StringsZh
}

/**
 * Aplica [locale] a la configuración de este Context (pickers del sistema, etc.).
 *
 * Devuelve un [ContextWrapper] cuya cadena base llega hasta este Context (la Activity):
 * androidx resuelve `LocalActivityResultRegistryOwner`/`LocalOnBackPressedDispatcherOwner`
 * caminando `ContextWrapper.getBaseContext()` desde `LocalContext`, y un
 * `createConfigurationContext` puro rompe ese camineo (crash al componer pickers de
 * foto/cámara con "No ActivityResultRegistryOwner was provided").
 */
fun Context.withLocale(locale: Locale): Context {
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    val localized = createConfigurationContext(config)
    return object : ContextWrapper(this) {
        override fun getResources(): Resources = localized.resources

        override fun getAssets(): AssetManager = localized.assets
    }
}
