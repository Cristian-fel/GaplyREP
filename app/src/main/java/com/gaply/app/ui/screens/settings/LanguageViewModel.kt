package com.gaply.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gaply.app.di.AppContainer
import kotlinx.coroutines.flow.StateFlow

/**
 * Módulo 2 · Idioma.
 * Mantiene el idioma seleccionado de forma reactiva y lo persiste
 * en [AppContainer.setAppLanguage] al pulsar "Guardar idioma".
 */
class LanguageViewModel(
    private val container: AppContainer,
    private val initialLanguage: String,
) : ViewModel() {

    val languages: List<String> = DEFAULT_LANGUAGES

    var selected: String = initialLanguage
        private set

    val savedLanguage: StateFlow<String> = container.appLanguage

    fun select(language: String) {
        selected = language
    }

    fun save() {
        container.setAppLanguage(selected)
    }

    companion object {
        val DEFAULT_LANGUAGES = listOf(
            "Español (ES)",
            "English (Us)",
            "Français",
            "Deutsch (DE)",
            "Italiano (IT)",
            "Português (BR)",
            "日本語(JP)",
            "中國人(中國)",
        )

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
