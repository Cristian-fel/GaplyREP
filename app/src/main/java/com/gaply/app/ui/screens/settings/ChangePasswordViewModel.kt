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
 * Módulo 2 · Cambiar contraseña (autenticado).
 * Valida que las dos contraseñas coincidan y que cumplan la longitud
 * mínima, y delega el cambio en [AuthRepository.changePassword].
 */
class ChangePasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var newPassword by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var newPasswordError by mutableStateOf<String?>(null)
        private set

    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    /** true = se muestra el modal "¡Contraseña actualizada!". */
    var success by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val canSubmit: Boolean
        get() = newPassword.isNotBlank() && confirmPassword.isNotBlank() && !loading

    fun onNewPasswordChange(value: String) {
        newPassword = value
        newPasswordError = null
    }

    fun onConfirmPasswordChange(value: String) {
        confirmPassword = value
        confirmPasswordError = null
    }

    fun submit() {
        if (loading) return

        newPasswordError = null
        confirmPasswordError = null
        errorMessage = null

        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            newPasswordError = "Completa los dos campos"
            return
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            newPasswordError = "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
            return
        }
        if (newPassword != confirmPassword) {
            confirmPasswordError = "Las contraseñas no coinciden"
            return
        }

        loading = true
        viewModelScope.launch {
            when (val result = authRepository.changePassword(newPassword)) {
                is RepoResult.Success -> {
                    success = true
                    newPassword = ""
                    confirmPassword = ""
                }
                is RepoResult.Error -> errorMessage = result.type.toMessage()
            }
            loading = false
        }
    }

    fun consumeSuccess() {
        success = false
    }

    fun consumeError() {
        errorMessage = null
    }

    private fun RepoError.toMessage(): String = when (this) {
        RepoError.EMPTY_FIELDS -> "Completa los dos campos"
        RepoError.WEAK_PASSWORD -> "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
        RepoError.PERMISSION ->
            "Por seguridad, cierra sesión y vuelve a entrar para cambiar tu contraseña"
        RepoError.CONNECTION -> "Sin conexión. Revisa tu internet e intenta de nuevo."
        RepoError.USER_NOT_FOUND -> "No hay una sesión activa"
        else -> "No se pudo cambiar la contraseña. Inténtalo de nuevo."
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChangePasswordViewModel(
                        authRepository = container.authRepository,
                    ) as T
                }
            }
    }
}
