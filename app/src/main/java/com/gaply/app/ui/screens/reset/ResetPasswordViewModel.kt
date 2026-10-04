package com.gaply.app.ui.screens.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val dialog: ResetDialog? = null,
    val successHandled: Boolean = false,
)

enum class ResetDialog { SUCCESS, ERROR }

class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
    private val identifier: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, newPasswordError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun onSavePassword() {
        val current = _uiState.value
        if (current.isLoading) return

        val newError = when {
            current.newPassword.isBlank() -> "Este campo es obligatorio"
            current.newPassword.length < MIN_PASSWORD -> "Mínimo $MIN_PASSWORD caracteres"
            else -> null
        }
        val confirmError = when {
            current.confirmPassword.isBlank() -> "Este campo es obligatorio"
            current.confirmPassword != current.newPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        if (newError != null || confirmError != null) {
            _uiState.update {
                it.copy(newPasswordError = newError, confirmPasswordError = confirmError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.updatePassword(identifier, current.newPassword)) {
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
        private const val MIN_PASSWORD = 6

        fun factory(container: AppContainer, identifier: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ResetPasswordViewModel(container.authRepository, identifier) as T
                }
            }
    }
}
