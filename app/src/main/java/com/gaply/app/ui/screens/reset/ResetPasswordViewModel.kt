package com.gaply.app.ui.screens.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val dialog: ResetDialog? = null,
    val successHandled: Boolean = false,
)

enum class ResetDialog { SUCCESS, ERROR }

class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
    identifier: String,
) : ViewModel() {

    // Si venía un correo desde el login, lo dejamos escrito en el campo
    private val _uiState = MutableStateFlow(
        ResetPasswordUiState(
            email = if (identifier.contains("@")) identifier.trim() else "",
        ),
    )
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onSendEmail() {
        val current = _uiState.value
        if (current.isLoading) return

        val email = current.email.trim()
        val error = when {
            email.isEmpty() -> "Este campo es obligatorio"
            !email.contains("@") -> "Ingresa un correo válido"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(emailError = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (authRepository.sendResetEmail(email)) {
                is RepoResult.Success -> _uiState.update {
                    it.copy(isLoading = false, dialog = ResetDialog.SUCCESS)
                }

                is RepoResult.Error -> _uiState.update {
                    it.copy(isLoading = false, dialog = ResetDialog.ERROR)
                }
            }
        }
    }

    fun consumeDialog() {
        val wasSuccess = _uiState.value.dialog == ResetDialog.SUCCESS
        _uiState.update { it.copy(dialog = null, successHandled = wasSuccess || it.successHandled) }
    }

    companion object {
        fun factory(container: AppContainer, identifier: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ResetPasswordViewModel(container.authRepository, identifier) as T
                }
            }
    }
}