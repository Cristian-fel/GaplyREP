package com.gaply.app.domain.model

sealed class RepoResult<out T> {
    data class Success<T>(val data: T) : RepoResult<T>()
    data class Error(val type: RepoError) : RepoResult<Nothing>()
}

enum class RepoError {
    EMPTY_FIELDS,
    WRONG_CREDENTIALS,
    USER_NOT_FOUND,
    CONNECTION,
    USERNAME_TAKEN,
    EMAIL_TAKEN,
    WEAK_PASSWORD,
    UNKNOWN,
}
