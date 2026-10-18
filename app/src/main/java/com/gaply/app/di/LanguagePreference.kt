package com.gaply.app.di

import com.gaply.app.core.i18n.AppLanguage
import kotlinx.coroutines.flow.StateFlow

/**
 * Preferencia de idioma que usa [com.gaply.app.ui.screens.settings.LanguageViewModel].
 * [AppContainer] la implementa; en tests unitarios se usa un doble falso.
 */
interface LanguagePreference {
    val appLanguage: StateFlow<AppLanguage>
    fun setAppLanguage(language: AppLanguage)
}
