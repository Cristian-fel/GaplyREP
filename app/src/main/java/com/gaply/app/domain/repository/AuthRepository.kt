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

    /** Cambia la contraseña del usuario autenticado (pantalla "Cambiar contraseña"). */
    suspend fun changePassword(newPassword: String): RepoResult<Unit>

    /** Marca la cuenta como desactivada (reversible) y cierra la sesión. */
    suspend fun deactivateAccount(): RepoResult<Unit>

    /** Elimina el usuario de Auth y su documento de Firestore (permanente). */
    suspend fun deleteAccount(): RepoResult<Unit>

    suspend fun sendResetEmail(email: String): RepoResult<Unit>

    suspend fun signOut()
}