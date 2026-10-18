package com.gaply.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.core.i18n.AppLanguage
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Módulo 2 · Configuración.
 * Cierra la sesión y expone el idioma seleccionado (persistente).
 */
class SettingsViewModel(
    private val authRepository: AuthRepository,
    val language: StateFlow<AppLanguage>,
) : ViewModel() {

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        authRepository = container.authRepository,
                        language = container.appLanguage,
                    ) as T
                }
            }
    }
}
