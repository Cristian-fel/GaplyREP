package com.gaply.app.ui.screens.register

import android.net.Uri
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

data class RegisterUiState(
    val step: Int = 1,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val firstNameError: String? = null,
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val birthDateError: String? = null,
    val university: String = "",
    val universityError: String? = null,
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

    val months = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
    )

    val days = (1..31).map { it.toString() }

    val years = (2010 downTo 1985).map { it.toString() }

    val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "Otra",
    )

    val genders = listOf("Femenino", "Masculino", "No binario", "Prefiero no decirlo")

    val goals = listOf(
        "\uD83D\uDCDA Necesito estudiar",
        "\uD83C\uDFC0 Qu\u00e9 hay para hacer",
        "\uD83D\uDE09 Tengo tiempo libre",
        "\uD83E\uDD7A A\u00fan no lo tengo claro",
    )

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

    fun onBirthMonthChange(value: String) =
        _uiState.update { it.copy(birthMonth = value, birthDateError = null) }

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
            state.username.isBlank() -> "Este campo es obligatorio"
            state.username.trim().length < 3 -> "M\u00ednimo 3 caracteres"
            else -> null
        }
        val emailError = when {
            state.email.isBlank() -> "Este campo es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() ->
                "Ingresa un correo v\u00e1lido"

            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> "Este campo es obligatorio"
            state.password.length < 6 -> "M\u00ednimo 6 caracteres"
            else -> null
        }
        val confirmError = when {
            state.confirmPassword.isBlank() -> "Este campo es obligatorio"
            state.confirmPassword != state.password -> "Las contrase\u00f1as no coinciden"
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
        RepoError.USERNAME_TAKEN -> copy(usernameError = "Ese nombre de usuario ya est\u00e1 en uso")
        RepoError.EMAIL_TAKEN -> copy(emailError = "Ese correo ya est\u00e1 registrado")
        RepoError.CONNECTION -> copy(dialog = RegisterDialog.ERROR_CONNECTION)
        else -> copy(dialog = RegisterDialog.ERROR_SAVE)
    }

    private fun validateStep2() {
        val state = _uiState.value
        val firstNameError = if (state.firstName.isBlank()) "Este campo es obligatorio" else null
        val birthError = when {
            state.birthDay.isBlank() || state.birthMonth.isBlank() || state.birthYear.isBlank() ->
                "Selecciona tu fecha de nacimiento"

            else -> null
        }
        val universityError =
            if (state.university.isBlank()) "Selecciona tu universidad" else null

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

            val monthNumber = (RegisterData.months.indexOf(state.birthMonth) + 1)
                .toString().padStart(2, '0')
            val birthDate = if (state.birthDay.isNotBlank() && state.birthYear.isNotBlank()) {
                "${state.birthDay}/$monthNumber/${state.birthYear}"
            } else {
                ""
            }

            val completeUser = user.copy(
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                birthDate = birthDate,
                university = state.university,
                gender = state.gender,
                goal = state.goal,
                interests = state.selectedInterests,
                bio = state.bio.trim(),
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
                copy(step = 1, usernameError = "Ese nombre de usuario ya est\u00e1 en uso")

            RepoError.EMAIL_TAKEN -> copy(step = 1, emailError = "Ese correo ya est\u00e1 registrado")
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
