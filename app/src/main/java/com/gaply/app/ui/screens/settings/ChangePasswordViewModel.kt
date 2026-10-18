package com.gaply.app.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.core.i18n.FieldError
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.launch

enum class ChangePasswordError { CONNECTION, PERMISSION, USER_NOT_FOUND, GENERIC }

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

    /** Contraseña actual: solo se pide cuando Firebase exige reautenticación. */
    var currentPassword by mutableStateOf("")
        private set

    /** true = la sesión ya no es "reciente" y hay que reautenticarse. */
    var needsReauth by mutableStateOf(false)
        private set

    var newPasswordError by mutableStateOf<FieldError?>(null)
        private set

    var confirmPasswordError by mutableStateOf<FieldError?>(null)
        private set

    var currentPasswordError by mutableStateOf<FieldError?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    /** true = se muestra el modal "¡Contraseña actualizada!". */
    var success by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<ChangePasswordError?>(null)
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

    fun onCurrentPasswordChange(value: String) {
        currentPassword = value
        currentPasswordError = null
    }

    fun submit() {
        if (loading) return

        newPasswordError = null
        confirmPasswordError = null
        errorMessage = null

        val blankNew = newPassword.isBlank()
        val blankConfirm = confirmPassword.isBlank()
        if (blankNew || blankConfirm) {
            newPasswordError = if (blankNew) FieldError.REQUIRED else null
            confirmPasswordError = if (blankConfirm) FieldError.REQUIRED else null
            return
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            newPasswordError = FieldError.MIN_6
            return
        }
        if (newPassword != confirmPassword) {
            confirmPasswordError = FieldError.PASSWORDS_MISMATCH
            return
        }

        loading = true
        viewModelScope.launch {
            if (needsReauth) {
                // Reintenta el cambio tras reautenticar con la contraseña actual.
                when (val reauth = authRepository.reauthenticate(currentPassword)) {
                    is RepoResult.Error -> {
                        currentPasswordError = when (reauth.type) {
                            RepoError.EMPTY_FIELDS -> FieldError.REQUIRED
                            RepoError.WRONG_CREDENTIALS -> FieldError.WRONG_PASSWORD
                            else -> null
                        }
                        if (currentPasswordError == null) {
                            errorMessage = reauth.type.toDialog()
                        }
                    }

                    is RepoResult.Success -> {
                        currentPassword = ""
                        needsReauth = false
                        aplicarCambio()
                    }
                }
            } else {
                aplicarCambio()
            }
            loading = false
        }
    }

    private suspend fun aplicarCambio() {
        when (val result = authRepository.changePassword(newPassword)) {
            is RepoResult.Success -> {
                success = true
                newPassword = ""
                confirmPassword = ""
            }

            is RepoResult.Error -> when (result.type) {
                RepoError.EMPTY_FIELDS -> {
                    newPasswordError = FieldError.REQUIRED
                    confirmPasswordError = FieldError.REQUIRED
                }

                RepoError.WEAK_PASSWORD -> newPasswordError = FieldError.MIN_6
                RepoError.PERMISSION -> needsReauth = true
                else -> errorMessage = result.type.toDialog()
            }
        }
    }

    fun consumeSuccess() {
        success = false
    }

    fun consumeError() {
        errorMessage = null
    }

    private fun RepoError.toDialog(): ChangePasswordError = when (this) {
        RepoError.CONNECTION -> ChangePasswordError.CONNECTION
        RepoError.PERMISSION -> ChangePasswordError.PERMISSION
        RepoError.USER_NOT_FOUND -> ChangePasswordError.USER_NOT_FOUND
        else -> ChangePasswordError.GENERIC
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
