package com.gaply.app.ui.screens.profile.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.UserRepository
import com.gaply.app.ui.screens.register.RegisterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val step: Int = 1,
    val loadingProfile: Boolean = true,
    val firstName: String = "",
    val lastName: String = "",
    val university: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val gender: String = "",
    val interests: List<String> = emptyList(),
    val bio: String = "",
    val currentPhotoUrl: String = "",
    val localPhotoUri: Uri? = null,
    val firstNameError: String? = null,
    val birthDateError: String? = null,
    val universityError: String? = null,
    val isLoading: Boolean = false,
    val dialog: EditProfileDialog? = null,
    val showPreview: Boolean = false,
    val saved: Boolean = false,
)

enum class EditProfileDialog { SAVED, ERROR_SAVE, ERROR_CONNECTION }

/**
 * Módulo 2: editar el perfil del usuario ya registrado.
 * Carga los datos actuales desde Firestore, los deja en campos editables
 * y vuelve a guardar con `saveProfile` (misma fuente de verdad que el registro).
 */
class EditProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    /** Copia de los datos tal como venían, para no perder lo que no se edita. */
    private var originalUser: User = User()

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.currentUser.first { it != null } ?: return@launch
            originalUser = user

            // birthDate se guarda como "15/03/2006": lo separamos otra vez.
            val parts = user.birthDate.split("/")
            val monthNumber = parts.getOrNull(1)?.toIntOrNull() ?: 0

            _uiState.update {
                it.copy(
                    loadingProfile = false,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    university = user.university,
                    birthDay = parts.getOrNull(0).orEmpty(),
                    birthMonth = if (monthNumber in 1..12) {
                        RegisterData.months[monthNumber - 1]
                    } else {
                        ""
                    },
                    birthYear = parts.getOrNull(2).orEmpty(),
                    gender = user.gender,
                    interests = user.interests,
                    bio = user.bio,
                    currentPhotoUrl = user.profilePictureUrl,
                )
            }
        }
    }

    // ---------------------------------------------------------------- pasos

    fun onPreviousStep() {
        _uiState.update { if (it.step > 1) it.copy(step = it.step - 1) else it }
    }

    fun onNextStep() {
        val current = _uiState.value
        if (current.isLoading) return
        if (!validateStep(current.step)) return
        _uiState.update { it.copy(step = (it.step + 1).coerceAtMost(TOTAL_STEPS)) }
    }

    private fun validateStep(step: Int): Boolean {
        val state = _uiState.value
        return when (step) {
            1 -> {
                val error = if (state.firstName.isBlank()) "Este campo es obligatorio" else null
                _uiState.update { it.copy(firstNameError = error) }
                error == null
            }

            2 -> {
                val birthError = when {
                    state.birthDay.isBlank() ||
                        state.birthMonth.isBlank() ||
                        state.birthYear.isBlank() -> "Selecciona tu fecha de nacimiento"

                    else -> null
                }
                val universityError =
                    if (state.university.isBlank()) "Selecciona tu universidad" else null
                _uiState.update {
                    it.copy(birthDateError = birthError, universityError = universityError)
                }
                birthError == null && universityError == null
            }

            else -> true
        }
    }

    // ------------------------------------------------------------- campos

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, firstNameError = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value) }
    }

    fun onUniversityChange(value: String) {
        _uiState.update { it.copy(university = value, universityError = null) }
    }

    fun onBirthDayChange(value: String) {
        _uiState.update { it.copy(birthDay = value, birthDateError = null) }
    }

    fun onBirthMonthChange(value: String) {
        _uiState.update { it.copy(birthMonth = value, birthDateError = null) }
    }

    fun onBirthYearChange(value: String) {
        _uiState.update { it.copy(birthYear = value, birthDateError = null) }
    }

    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = if (it.gender == value) "" else value) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(bio = value.take(MAX_BIO_LENGTH)) }
    }

    fun onInterestToggle(interest: String) {
        _uiState.update { current ->
            val selected = current.interests
            val updated = when {
                interest in selected -> selected - interest
                selected.size >= MAX_INTERESTS -> selected
                else -> selected + interest
            }
            current.copy(interests = updated)
        }
    }

    // --------------------------------------------------------------- foto

    fun onPhotoSelected(uri: Uri) {
        _uiState.update { it.copy(localPhotoUri = uri) }
    }

    // ----------------------------------------------------------- guardar

    fun onTogglePreview() {
        _uiState.update { it.copy(showPreview = !it.showPreview) }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // La foto es opcional: si cambió, se sube primero y se usa su URL.
            var photoUrl = state.currentPhotoUrl
            val localUri = state.localPhotoUri
            if (localUri != null) {
                when (val uploadResult = userRepository.uploadProfilePhoto(localUri)) {
                    is RepoResult.Success -> photoUrl = uploadResult.data
                    is RepoResult.Error -> Unit // el perfil se guarda igualmente
                }
            }

            val monthNumber = (RegisterData.months.indexOf(state.birthMonth) + 1)
                .toString()
                .padStart(2, '0')
            val birthDate = if (state.birthDay.isNotBlank() && state.birthYear.isNotBlank()) {
                "${state.birthDay}/$monthNumber/${state.birthYear}"
            } else {
                ""
            }

            val updatedUser = originalUser.copy(
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                university = state.university,
                birthDate = birthDate,
                gender = state.gender,
                interests = state.interests,
                bio = state.bio.trim(),
                profilePictureUrl = photoUrl,
            )

            when (val result = userRepository.saveProfile(updatedUser)) {
                is RepoResult.Success -> _uiState.update {
                    it.copy(isLoading = false, saved = true, dialog = EditProfileDialog.SAVED)
                }

                is RepoResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        dialog = if (result.type == RepoError.CONNECTION) {
                            EditProfileDialog.ERROR_CONNECTION
                        } else {
                            EditProfileDialog.ERROR_SAVE
                        },
                    )
                }
            }
        }
    }

    fun consumeDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }

    companion object {
        const val TOTAL_STEPS = 3
        const val MAX_INTERESTS = 3
        const val MAX_BIO_LENGTH = 200

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditProfileViewModel(
                        authRepository = container.authRepository,
                        userRepository = container.userRepository,
                    ) as T
                }
            }
    }
}
