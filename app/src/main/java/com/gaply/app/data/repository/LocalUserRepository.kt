package com.gaply.app.data.repository

import com.gaply.app.data.local.LocalUserStore
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.UserRepository
import kotlinx.coroutines.delay

class LocalUserRepository(
    private val store: LocalUserStore,
    private val latencyMs: Long = 200L,
) : UserRepository {

    override suspend fun saveProfile(user: User): RepoResult<User> {
        delay(latencyMs)
        val users = store.loadUsers().toMutableList()
        val index = users.indexOfFirst { it.userId == user.userId }
        if (index >= 0) {
            users[index] = user
        } else {
            users.add(user)
        }
        store.saveUsers(users)
        return RepoResult.Success(user)
    }

    override fun getProfile(): User? = store.currentUser.value
}
