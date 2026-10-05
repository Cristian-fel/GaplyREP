package com.gaply.app.data.repository

import com.gaply.app.data.local.LocalUserStore
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest
import java.util.UUID

class InMemoryAuthRepository(
    private val store: LocalUserStore,
    private val latencyMs: Long = 250L,
    private val simulateConnectionError: Boolean = SIMULATE_CONNECTION_ERROR,
) : AuthRepository {

    override val currentUser: StateFlow<User?> = store.currentUser

    override suspend fun signUp(username: String, email: String, password: String): RepoResult<User> {
        delay(latencyMs)
        if (simulateConnectionError) return RepoResult.Error(RepoError.CONNECTION)
        val trimmedUser = username.trim()
        val trimmedEmail = email.trim()
        if (trimmedUser.isEmpty() || trimmedEmail.isEmpty() || password.isEmpty()) {
            return RepoResult.Error(RepoError.EMPTY_FIELDS)
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return RepoResult.Error(RepoError.WEAK_PASSWORD)
        }
        val users = store.loadUsers().toMutableList()
        if (users.any { it.username.equals(trimmedUser, ignoreCase = true) }) {
            return RepoResult.Error(RepoError.USERNAME_TAKEN)
        }
        if (users.any { it.email.equals(trimmedEmail, ignoreCase = true) }) {
            return RepoResult.Error(RepoError.EMAIL_TAKEN)
        }
        val user = User(
            userId = UUID.randomUUID().toString(),
            username = trimmedUser,
            email = trimmedEmail,
            passwordHash = hash(password),
        )
        users.add(user)
        store.saveUsers(users)
        store.setSession(user.userId)
        return RepoResult.Success(user)
    }

    override suspend fun signIn(identifier: String, password: String): RepoResult<User> {
        delay(latencyMs)
        if (simulateConnectionError) return RepoResult.Error(RepoError.CONNECTION)
        val trimmed = identifier.trim()
        if (trimmed.isEmpty() || password.isEmpty()) return RepoResult.Error(RepoError.EMPTY_FIELDS)

        val users = store.loadUsers()
        if (users.isEmpty()) return RepoResult.Error(RepoError.USER_NOT_FOUND)

        val user = users.find {
            it.username.equals(trimmed, ignoreCase = true) ||
                    it.email.equals(trimmed, ignoreCase = true)
        } ?: return RepoResult.Error(RepoError.WRONG_CREDENTIALS)

        if (user.passwordHash != hash(password)) {
            return RepoResult.Error(RepoError.WRONG_CREDENTIALS)
        }
        store.setSession(user.userId)
        return RepoResult.Success(user)
    }

    override suspend fun updatePassword(identifier: String?, newPassword: String): RepoResult<Unit> {
        delay(latencyMs)
        if (simulateConnectionError) return RepoResult.Error(RepoError.CONNECTION)
        if (newPassword.isEmpty()) return RepoResult.Error(RepoError.EMPTY_FIELDS)
        if (newPassword.length < MIN_PASSWORD_LENGTH) return RepoResult.Error(RepoError.WEAK_PASSWORD)

        val users = store.loadUsers().toMutableList()
        if (users.isEmpty()) return RepoResult.Error(RepoError.USER_NOT_FOUND)

        val target = identifier?.trim()?.takeIf { it.isNotEmpty() }?.let { id ->
            users.find {
                it.username.equals(id, ignoreCase = true) || it.email.equals(id, ignoreCase = true)
            }
        } ?: store.currentUser.value ?: users.firstOrNull()
        ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)

        val index = users.indexOfFirst { it.userId == target.userId }
        if (index < 0) return RepoResult.Error(RepoError.USER_NOT_FOUND)
        users[index] = target.copy(passwordHash = hash(newPassword))
        store.saveUsers(users)
        return RepoResult.Success(Unit)
    }
    override suspend fun sendResetEmail(email: String): RepoResult<Unit> =
        RepoResult.Success(Unit) // modo local: no manda correos de verdad

    override suspend fun signOut() {
        store.setSession(null)
    }

    override suspend fun checkSignUpAvailable(username: String, email: String): RepoResult<Unit> {
        if (simulateConnectionError) return RepoResult.Error(RepoError.CONNECTION)
        val users = store.loadUsers()
        if (users.any { it.username.equals(username.trim(), ignoreCase = true) }) {
            return RepoResult.Error(RepoError.USERNAME_TAKEN)
        }
        if (users.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return RepoResult.Error(RepoError.EMAIL_TAKEN)
        }
        return RepoResult.Success(Unit)
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(value.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { "%02x".format(it) }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6

        /**
         * Poner en true para demostrar el modal de "¡Error de conexión!" en Login/Registro.
         */
        const val SIMULATE_CONNECTION_ERROR = false
    }
}