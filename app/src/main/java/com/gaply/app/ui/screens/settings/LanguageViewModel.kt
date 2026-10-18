package com.gaply.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gaply.app.core.i18n.AppLanguage
import com.gaply.app.di.AppContainer
import kotlinx.coroutines.flow.StateFlow

/**
 * Módulo 2 · Idioma.
 * Mantiene el idioma seleccionado de forma reactiva y lo persiste
 * en [AppContainer.setAppLanguage] al pulsar "Guardar idioma".
 */
class LanguageViewModel(
    private val container: AppContainer,
    initialLanguage: AppLanguage,
) : ViewModel() {

    val languages: List<AppLanguage> = AppLanguage.entries

    var selected: AppLanguage = initialLanguage
        private set

    val savedLanguage: StateFlow<AppLanguage> = container.appLanguage

    fun select(language: AppLanguage) {
        selected = language
    }

    fun save() {
        container.setAppLanguage(selected)
    }

    companion object {

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LanguageViewModel(
                        container = container,
                        initialLanguage = container.appLanguage.value,
                    ) as T
                }
            }
    }
}
