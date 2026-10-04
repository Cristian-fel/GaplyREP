package com.gaply.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val identifierError: String? = null,
    val passwordError: String? = null,
    val credentialsError: String? = null,
    val isLoading: Boolean = false,
    val dialog: LoginDialog? = null,
    val isLoggedIn: Boolean = false,
    val receiveMarketing: Boolean = false,
)

enum class LoginDialog { CONNECTION, USER_NOT_FOUND }

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) {
        _uiState.update {
            it.copy(identifier = value, identifierError = null, credentialsError = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, passwordError = null, credentialsError = null)
        }
    }

    fun onMarketingToggle(checked: Boolean) {
        _uiState.update { it.copy(receiveMarketing = checked) }
    }

    fun onLogin() {
        val current = _uiState.value
        if (current.isLoading) return

        val identifierError = if (current.identifier.isBlank()) "Este campo es obligatorio" else null
        val passwordError = if (current.password.isBlank()) "Este campo es obligatorio" else null
        if (identifierError != null || passwordError != null) {
            _uiState.update {
                it.copy(identifierError = identifierError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, credentialsError = null) }
            when (val result = authRepository.signIn(current.identifier, current.password)) {
                is RepoResult.Success -> {
                    val user = result.data
                    if (user.receiveMarketing != current.receiveMarketing) {
                        userRepository.saveProfile(user.copy(receiveMarketing = current.receiveMarketing))
                    }
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }

                is RepoResult.Error -> {
                    _uiState.update { it.copy(isLoading = false).applyError(result.type) }
                }
            }
        }
    }

    fun consumeDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    private fun LoginUiState.applyError(error: RepoError): LoginUiState = when (error) {
        RepoError.EMPTY_FIELDS -> copy(
            identifierError = if (identifier.isBlank()) "Este campo es obligatorio" else null,
            passwordError = if (password.isBlank()) "Este campo es obligatorio" else null,
        )

        RepoError.WRONG_CREDENTIALS -> copy(credentialsError = "Usuario o contraseña son incorrectos")
        RepoError.USER_NOT_FOUND -> copy(dialog = LoginDialog.USER_NOT_FOUND)
        RepoError.CONNECTION -> copy(dialog = LoginDialog.CONNECTION)
        else -> copy(credentialsError = "Usuario o contraseña son incorrectos")
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(
                        authRepository = container.authRepository,
                        userRepository = container.userRepository,
                    ) as T
                }
            }
    }
}
