package com.gaply.app.navigation

import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeAuthRepository(initialUser: User? = null) : AuthRepository {
    private val _currentUser = MutableStateFlow(initialUser)
    override val currentUser: StateFlow<User?> = _currentUser

    override fun isUserLoggedIn(): Boolean = _currentUser.value != null

    override suspend fun signUp(username: String, email: String, password: String): RepoResult<User> {
        val user = User(userId = "test_user", username = username, email = email)
        _currentUser.value = user
        return RepoResult.Success(user)
    }

    override suspend fun checkSignUpAvailable(username: String, email: String): RepoResult<Unit> =
        RepoResult.Success(Unit)

    override suspend fun signIn(identifier: String, password: String): RepoResult<User> {
        val user = User(userId = "test_user", username = identifier, email = identifier)
        _currentUser.value = user
        return RepoResult.Success(user)
    }

    override suspend fun updatePassword(identifier: String?, newPassword: String): RepoResult<Unit> =
        RepoResult.Success(Unit)

    override suspend fun changePassword(newPassword: String): RepoResult<Unit> =
        RepoResult.Success(Unit)

    override suspend fun reauthenticate(currentPassword: String): RepoResult<Unit> =
        RepoResult.Success(Unit)

    override suspend fun deactivateAccount(): RepoResult<Unit> {
        _currentUser.value = null
        return RepoResult.Success(Unit)
    }

    override suspend fun deleteAccount(): RepoResult<Unit> {
        _currentUser.value = null
        return RepoResult.Success(Unit)
    }

    override suspend fun sendResetEmail(email: String): RepoResult<Unit> =
        RepoResult.Success(Unit)

    override suspend fun signOut() {
        _currentUser.value = null
    }
}

class StartDestinationTest {

    @Test
    fun `si hay sesion activa inicia en HomeScreen`() {
        val repo = FakeAuthRepository(initialUser = User(userId = "usr_01", username = "gaply_user"))
        val destination = resolveStartDestination(repo)
        assertEquals(Routes.HOME, destination)
    }

    @Test
    fun `si no hay sesion activa inicia en WelcomeScreen`() {
        val repo = FakeAuthRepository(initialUser = null)
        val destination = resolveStartDestination(repo)
        assertEquals(Routes.WELCOME, destination)
    }

    @Test
    fun `al cerrar sesion la proxima vez inicia en WelcomeScreen`() = kotlinx.coroutines.runBlocking {
        val repo = FakeAuthRepository(initialUser = User(userId = "usr_01", username = "gaply_user"))
        assertEquals(Routes.HOME, resolveStartDestination(repo))

        repo.signOut()

        assertEquals(Routes.WELCOME, resolveStartDestination(repo))
    }

    @Test
    fun `al iniciar sesion cambia el estado a autenticado`() = kotlinx.coroutines.runBlocking {
        val repo = FakeAuthRepository(initialUser = null)
        assertEquals(Routes.WELCOME, resolveStartDestination(repo))

        repo.signIn("estudiante@universidad.edu", "pass123")

        assertEquals(Routes.HOME, resolveStartDestination(repo))
    }
}
