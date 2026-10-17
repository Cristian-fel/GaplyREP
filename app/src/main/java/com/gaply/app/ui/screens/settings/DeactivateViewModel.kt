package com.gaply.app.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * Módulo 2 · Desactivar cuenta (temporal).
 * Marca la cuenta como desactivada (conservando los datos) y
 * cierra la sesión; el usuario puede reactivarla con sus mismos
 * credenciales.
 */
class DeactivateViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun deactivate(onDeactivated: () -> Unit) {
        if (loading) return
        loading = true
        viewModelScope.launch {
            when (val result = authRepository.deactivateAccount()) {
                is RepoResult.Success -> onDeactivated()
                is RepoResult.Error -> errorMessage = result.type.toMessage()
            }
            loading = false
        }
    }

    fun consumeError() {
        errorMessage = null
    }

    private fun RepoError.toMessage(): String = when (this) {
        RepoError.PERMISSION ->
            "Por seguridad, cierra sesión y vuelve a entrar para desactivar tu cuenta"
        RepoError.CONNECTION -> "Sin conexión. Revisa tu internet e intenta de nuevo."
        RepoError.USER_NOT_FOUND -> "No hay una sesión activa"
        else -> "No se pudo desactivar la cuenta. Inténtalo de nuevo."
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeactivateViewModel(
                        authRepository = container.authRepository,
                    ) as T
                }
            }
    }
}
