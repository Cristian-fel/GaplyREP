package com.gaply.app.core.i18n

/** Errores de validación de campo compartidos entre registro, login, etc. */
enum class FieldError {
    REQUIRED,
    MIN_3,
    MIN_6,
    INVALID_EMAIL,
    PASSWORDS_MISMATCH,
    WRONG_PASSWORD,
    USERNAME_TAKEN,
    EMAIL_TAKEN,
    SELECT_BIRTHDATE,
    SELECT_UNIVERSITY,

    /** Marca el campo en rojo sin mostrar texto de apoyo. */
    HIGHLIGHT,
}

fun AppStrings.fieldError(error: FieldError): String = when (error) {
    FieldError.REQUIRED -> fieldRequired
    FieldError.MIN_3 -> minChars3
    FieldError.MIN_6 -> minChars6
    FieldError.INVALID_EMAIL -> validEmailPrompt
    FieldError.PASSWORDS_MISMATCH -> passwordsMismatch
    FieldError.WRONG_PASSWORD -> wrongPasswordMsg
    FieldError.USERNAME_TAKEN -> usernameTaken
    FieldError.EMAIL_TAKEN -> emailTaken
    FieldError.SELECT_BIRTHDATE -> selectBirthdate
    FieldError.SELECT_UNIVERSITY -> selectUniversity
    FieldError.HIGHLIGHT -> " "
}
