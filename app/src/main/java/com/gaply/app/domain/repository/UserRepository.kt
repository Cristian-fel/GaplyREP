package com.gaply.app.domain.repository

import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User

interface UserRepository {
    suspend fun saveProfile(user: User): RepoResult<User>

    fun getProfile(): User?
}
