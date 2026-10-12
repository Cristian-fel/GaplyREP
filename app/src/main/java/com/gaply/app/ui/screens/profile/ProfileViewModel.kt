package com.gaply.app.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Módulo 2 · Lógica de la pestaña Perfil.
 * Ahora mismo se encarga de cambiar la foto desde el badge de la cámara
 * (galería o cámara) usando el mismo Storage que el registro.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _uploading.value = true
            val user = authRepository.currentUser.value
            if (user == null) {
                _error.value = "No pudimos identificar tu cuenta. Cierra sesión y vuelve a entrar."
                _uploading.value = false
                return@launch
            }

            when (val uploadResult = userRepository.uploadProfilePhoto(uri)) {
                is RepoResult.Success -> {
                    userRepository.saveProfile(
                        user.copy(profilePictureUrl = uploadResult.data),
                    )
                }

                is RepoResult.Error -> {
                    _error.value = "No pudimos cambiar tu foto. Inténtalo más tarde."
                }
            }
            _uploading.value = false
        }
    }

    fun consumeError() {
        _error.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(
                        authRepository = container.authRepository,
                        userRepository = container.userRepository,
                    ) as T
                }
            }
    }
}
