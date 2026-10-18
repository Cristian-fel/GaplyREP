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

enum class DeleteAccountError { CONNECTION, PERMISSION, USER_NOT_FOUND, GENERIC }

/**
 * Módulo 2 · Eliminar cuenta (permanente).
 * Ejecuta la eliminación real de la cuenta y de sus datos en Firestore.
 */
class DeleteAccountViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<DeleteAccountError?>(null)
        private set

    fun deleteAccount(onDeleted: () -> Unit) {
        if (loading) return
        loading = true
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount()) {
                is RepoResult.Success -> onDeleted()
                is RepoResult.Error -> errorMessage = result.type.toDialog()
            }
            loading = false
        }
    }

    fun consumeError() {
        errorMessage = null
    }

    private fun RepoError.toDialog(): DeleteAccountError = when (this) {
        RepoError.CONNECTION -> DeleteAccountError.CONNECTION
        RepoError.PERMISSION -> DeleteAccountError.PERMISSION
        RepoError.USER_NOT_FOUND -> DeleteAccountError.USER_NOT_FOUND
        else -> DeleteAccountError.GENERIC
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeleteAccountViewModel(
                        authRepository = container.authRepository,
                    ) as T
                }
            }
    }
}
