package com.gaply.app.ui.screens.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.FieldError
import com.gaply.app.core.i18n.StringsEs
import com.gaply.app.core.i18n.canonicalFromLocalized
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

data class RegisterUiState(
    val step: Int = 1,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: FieldError? = null,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val confirmPasswordError: FieldError? = null,
    val firstName: String = "",
    val lastName: String = "",
    val firstNameError: FieldError? = null,
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthMonthNumber: Int = 0,
    val birthYear: String = "",
    val birthDateError: FieldError? = null,
    val university: String = "",
    val universityError: FieldError? = null,
    val gender: String = "",
    val goal: String = "",
    val selectedInterests: List<String> = emptyList(),
    val extraInterests: List<String> = emptyList(),
    val bio: String = "",
    val accountCreated: Boolean = false,
    val profileSaved: Boolean = false,
    val isLoading: Boolean = false,
    val dialog: RegisterDialog? = null,
    val finishAndGoHome: Boolean = false,
    val profileImageUri: Uri? = null,
)

enum class RegisterDialog { SAVED_PROFILE, GALLERY_INFO, CAMERA_INFO, ERROR_SAVE, ERROR_CONNECTION }

object RegisterData {
    const val MAX_INTERESTS = 3

    val days = (1..31).map { it.toString() }

    val years = (2010 downTo 1985).map { it.toString() }

    val interests = listOf(
        "Deportes", "M\u00fasica", "Videojuegos", "Artes",
        "Juegos de mesa", "Pel\u00edculas", "Estudio", "Eventos", "Baile",
    )

    val extraOptions = listOf(
        "Fotograf\u00eda", "Viajes", "Cocina", "Programaci\u00f3n", "Moda", "Ciencia",
    )
}

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    /** Idioma activo: lo asigna la pantalla (SideEffect) para canonicalizar al guardar. */
    var strings: AppStrings = StringsEs

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) =
        _uiState.update { it.copy(username = value, usernameError = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }

    fun onFirstNameChange(value: String) =
        _uiState.update { it.copy(firstName = value, firstNameError = null) }

    fun onLastNameChange(value: String) = _uiState.update { it.copy(lastName = value) }

    fun onBirthDayChange(value: String) =
        _uiState.update { it.copy(birthDay = value, birthDateError = null) }

    fun onBirthMonthChange(value: String, monthNumber: Int) =
        _uiState.update {
            it.copy(birthMonth = value, birthMonthNumber = monthNumber, birthDateError = null)
        }

    fun onBirthYearChange(value: String) =
        _uiState.update { it.copy(birthYear = value, birthDateError = null) }

    fun onUniversityChange(value: String) =
        _uiState.update { it.copy(university = value, universityError = null) }

    fun onGenderChange(value: String) = _uiState.update { it.copy(gender = value) }

    fun onGoalChange(value: String) = _uiState.update { it.copy(goal = value) }

    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value) }

    fun onProfileImageSelected(uri: Uri?) {
        _uiState.update { it.copy(profileImageUri = uri) }
    }

    fun onToggleInterest(interest: String) {
        _uiState.update { state ->
            val selected = state.selectedInterests
            val next = when {
                selected.contains(interest) -> selected - interest
                selected.size >= RegisterData.MAX_INTERESTS -> selected
                else -> selected + interest
            }
            state.copy(selectedInterests = next)
        }
    }

    fun onAddExtraInterest(interest: String) {
        _uiState.update { state ->
            if (!state.extraInterests.contains(interest)) {
                state.copy(extraInterests = state.extraInterests + interest)
            } else {
                state
            }
        }
        onToggleInterest(interest)
    }

    fun onDismissExtras() {
        _uiState.update { state ->
            val selectedExtras = state.selectedInterests.filter {
                it in RegisterData.extraOptions
            }
            state.copy(extraInterests = (state.extraInterests + selectedExtras).distinct())
        }
    }

    fun onNextStep() {
        when (_uiState.value.step) {
            1 -> validateStep1()
            2 -> validateStep2()
            3 -> _uiState.update { it.copy(step = 4) }
            4 -> _uiState.update { it.copy(step = 5) }
            else -> saveProfile(navigateAfter = true)
        }
    }

    fun onSkipStep() {
        when (_uiState.value.step) {
            3 -> _uiState.update { it.copy(step = 4) }
            4 -> _uiState.update { it.copy(step = 5) }
        }
    }

    fun onPreviousStep() {
        val current = _uiState.value.step
        if (current > 1) {
            _uiState.update { it.copy(step = current - 1) }
        }
    }

    fun onSaveProfile() = saveProfile(navigateAfter = false)

    fun onShowGalleryInfo() = _uiState.update { it.copy(dialog = RegisterDialog.GALLERY_INFO) }

    fun onShowCameraInfo() = _uiState.update { it.copy(dialog = RegisterDialog.CAMERA_INFO) }

    fun consumeDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun consumeFinish() {
        _uiState.update { it.copy(finishAndGoHome = false) }
    }

    private fun validateStep1() {
        val state = _uiState.value
        val usernameError = when {
            state.username.isBlank() -> FieldError.REQUIRED
            state.username.trim().length < 3 -> FieldError.MIN_3
            else -> null
        }
        val emailError = when {
            state.email.isBlank() -> FieldError.REQUIRED
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() ->
                FieldError.INVALID_EMAIL

            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> FieldError.REQUIRED
            state.password.length < 6 -> FieldError.MIN_6
            else -> null
        }
        val confirmError = when {
            state.confirmPassword.isBlank() -> FieldError.REQUIRED
            state.confirmPassword != state.password -> FieldError.PASSWORDS_MISMATCH
            else -> null
        }

        if (usernameError != null || emailError != null || passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    usernameError = usernameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (
                val result = authRepository.checkSignUpAvailable(state.username, state.email)
            ) {
                is RepoResult.Success -> _uiState.update {
                    it.copy(isLoading = false, step = 2)
                }

                is RepoResult.Error -> _uiState.update {
                    it.copy(isLoading = false).applySignUpError(result.type)
                }
            }
        }
    }

    private fun RegisterUiState.applySignUpError(error: RepoError): RegisterUiState = when (error) {
        RepoError.USERNAME_TAKEN -> copy(usernameError = FieldError.USERNAME_TAKEN)
        RepoError.EMAIL_TAKEN -> copy(emailError = FieldError.EMAIL_TAKEN)
        RepoError.CONNECTION -> copy(dialog = RegisterDialog.ERROR_CONNECTION)
        else -> copy(dialog = RegisterDialog.ERROR_SAVE)
    }

    private fun validateStep2() {
        val state = _uiState.value
        val firstNameError = if (state.firstName.isBlank()) FieldError.REQUIRED else null
        val birthError = when {
            state.birthDay.isBlank() || state.birthMonth.isBlank() || state.birthYear.isBlank() ->
                FieldError.SELECT_BIRTHDATE

            else -> null
        }
        val universityError =
            if (state.university.isBlank()) FieldError.SELECT_UNIVERSITY else null

        if (firstNameError != null || birthError != null || universityError != null) {
            _uiState.update {
                it.copy(
                    firstNameError = firstNameError,
                    birthDateError = birthError,
                    universityError = universityError,
                )
            }
            return
        }
        _uiState.update { it.copy(step = 3) }
    }

    private fun saveProfile(navigateAfter: Boolean) {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            var accountUser = if (state.accountCreated) {
                authRepository.currentUser.value
            } else {
                null
            }

            if (accountUser == null) {
                when (
                    val signUpResult =
                        authRepository.signUp(state.username, state.email, state.password)
                ) {
                    is RepoResult.Success -> {
                        accountUser = signUpResult.data
                        _uiState.update { it.copy(accountCreated = true) }
                    }

                    is RepoResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false)
                                .applySignUpErrorToStep1(result = signUpResult.type)
                        }
                        return@launch
                    }
                }
            }

            val user = accountUser ?: run {
                _uiState.update { it.copy(isLoading = false, dialog = RegisterDialog.ERROR_SAVE) }
                return@launch
            }

            val monthNumber = state.birthMonthNumber.toString().padStart(2, '0')
            val birthDate = if (state.birthDay.isNotBlank() && state.birthYear.isNotBlank()) {
                "${state.birthDay.padStart(2, '0')}/$monthNumber/${state.birthYear}"
            } else {
                ""
            }

            var photoUrl = user.profilePictureUrl
            val localPhotoUri = state.profileImageUri
            if (localPhotoUri != null) {
                when (val uploadResult = userRepository.uploadProfilePhoto(localPhotoUri)) {
                    is RepoResult.Success -> photoUrl = uploadResult.data
                    is RepoResult.Error -> Unit // la foto es opcional: el perfil se guarda sin ella
                }
            }

            // Guarda la clave canónica (ES), no la etiqueta localizada:
            // al cambiar de idioma los chips siguen coincidiendo.
            val canonicalUniversity = canonicalFromLocalized(
                state.university, strings.universities, StringsEs.universities,
            )
            val canonicalGender = canonicalFromLocalized(
                state.gender, strings.genders, StringsEs.genders,
            )
            val canonicalGoal = canonicalFromLocalized(
                state.goal, strings.goals, StringsEs.goals,
            )

            val completeUser = user.copy(
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                birthDate = birthDate,
                university = canonicalUniversity,
                gender = canonicalGender,
                goal = canonicalGoal,
                interests = state.selectedInterests,
                bio = state.bio.trim(),
                profilePictureUrl = photoUrl,
            )

            when (val saveResult = userRepository.saveProfile(completeUser)) {
                is RepoResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileSaved = true,
                        dialog = if (navigateAfter) null else RegisterDialog.SAVED_PROFILE,
                        finishAndGoHome = if (navigateAfter) true else it.finishAndGoHome,
                    )
                }

                is RepoResult.Error -> _uiState.update {
                    it.copy(isLoading = false, dialog = RegisterDialog.ERROR_SAVE)
                }
            }
        }
    }

    private fun RegisterUiState.applySignUpErrorToStep1(result: RepoError): RegisterUiState =
        when (result) {
            RepoError.USERNAME_TAKEN ->
                copy(step = 1, usernameError = FieldError.USERNAME_TAKEN)

            RepoError.EMAIL_TAKEN -> copy(step = 1, emailError = FieldError.EMAIL_TAKEN)
            RepoError.CONNECTION -> copy(dialog = RegisterDialog.ERROR_CONNECTION)
            else -> copy(dialog = RegisterDialog.ERROR_SAVE)
        }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RegisterViewModel(
                        authRepository = container.authRepository,
                        userRepository = container.userRepository,
                    ) as T
                }
            }
    }
}
