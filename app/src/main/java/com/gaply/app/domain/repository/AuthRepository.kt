package com.gaply.app.domain.repository

import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>

    suspend fun signUp(username: String, email: String, password: String): RepoResult<User>

    suspend fun checkSignUpAvailable(username: String, email: String): RepoResult<Unit>

    suspend fun signIn(identifier: String, password: String): RepoResult<User>

    suspend fun updatePassword(identifier: String?, newPassword: String): RepoResult<Unit>

    suspend fun signOut()
}
