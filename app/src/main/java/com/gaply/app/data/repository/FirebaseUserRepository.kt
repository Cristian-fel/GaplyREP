package com.gaply.app.data.repository

import com.gaply.app.data.mapper.toFirestoreMap
import com.gaply.app.data.mapper.toUser
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.concurrent.TimeoutException

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : UserRepository {

    @Volatile
    private var lastSaved: User? = null

    override suspend fun saveProfile(user: User): RepoResult<User> = try {
        firestore.collection(USERS).document(user.userId).set(user.toFirestoreMap()).await()
        lastSaved = user
        RepoResult.Success(user)
    } catch (e: Exception) {
        RepoResult.Error(mapError(e))
    }

    override fun getProfile(): User? = lastSaved

    private fun mapError(e: Exception): RepoError = when (e) {
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            -> RepoError.CONNECTION

            else -> RepoError.UNKNOWN
        }

        is FirebaseException -> RepoError.CONNECTION
        is IOException, is TimeoutException -> RepoError.CONNECTION
        else -> RepoError.UNKNOWN
    }

    companion object {
        private const val USERS = "users"
    }
}
