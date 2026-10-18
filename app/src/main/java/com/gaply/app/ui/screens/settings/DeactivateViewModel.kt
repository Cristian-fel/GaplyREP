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

enum class DeactivateError { CONNECTION, PERMISSION, USER_NOT_FOUND, GENERIC }

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

    var errorMessage by mutableStateOf<DeactivateError?>(null)
        private set

    fun deactivate(onDeactivated: () -> Unit) {
        if (loading) return
        loading = true
        viewModelScope.launch {
            when (val result = authRepository.deactivateAccount()) {
                is RepoResult.Success -> onDeactivated()
                is RepoResult.Error -> errorMessage = result.type.toDialog()
            }
            loading = false
        }
    }

    fun consumeError() {
        errorMessage = null
    }

    private fun RepoError.toDialog(): DeactivateError = when (this) {
        RepoError.CONNECTION -> DeactivateError.CONNECTION
        RepoError.PERMISSION -> DeactivateError.PERMISSION
        RepoError.USER_NOT_FOUND -> DeactivateError.USER_NOT_FOUND
        else -> DeactivateError.GENERIC
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
