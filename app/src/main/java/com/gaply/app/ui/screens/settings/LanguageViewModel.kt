package com.gaply.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gaply.app.core.i18n.AppLanguage
import com.gaply.app.di.LanguagePreference
import kotlinx.coroutines.flow.StateFlow

/**
 * Módulo 2 · Idioma.
 * Mantiene el idioma seleccionado de forma reactiva y lo persiste
 * en [LanguagePreference.setAppLanguage] al pulsar "Guardar idioma".
 *
 * En esta versión solo se muestran y permiten Español e Inglés; el
 * resto de idiomas se mantiene implementado en el código y se
 * habilitará en la versión final.
 */
class LanguageViewModel(
    private val preference: LanguagePreference,
    initialLanguage: AppLanguage,
) : ViewModel() {

    /** Idiomas visibles y seleccionables en esta versión. */
    val languages: List<AppLanguage> = listOf(AppLanguage.ES, AppLanguage.EN)

    var selected: AppLanguage =
        if (initialLanguage in languages) initialLanguage else AppLanguage.DEFAULT
        private set

    val savedLanguage: StateFlow<AppLanguage> = preference.appLanguage

    fun select(language: AppLanguage) {
        if (language in languages) {
            selected = language
        }
    }

    fun save() {
        preference.setAppLanguage(selected)
    }

    companion object {

        fun factory(preference: LanguagePreference): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LanguageViewModel(
                        preference = preference,
                        initialLanguage = preference.appLanguage.value,
                    ) as T
                }
            }
    }
}
