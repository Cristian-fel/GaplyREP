package com.gaply.app.domain.repository

import android.net.Uri
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User

interface UserRepository {
    suspend fun saveProfile(user: User): RepoResult<User>

    suspend fun uploadProfilePhoto(localUri: Uri): RepoResult<String>

    fun getProfile(): User?
}
