package com.gaply.app.data.repository

import com.gaply.app.data.mapper.toFirestoreMap
import com.gaply.app.data.mapper.toUser
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import android.util.Log

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser

    private var profileListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseUser ->
            profileListener?.remove()
            profileListener = null
            val uid = firebaseUser?.uid
            if (uid == null) {
                _currentUser.value = null
            } else {
                _currentUser.value = User(userId = uid)
                profileListener = firestore.collection(USERS).document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        val data = snapshot?.data
                        _currentUser.value =
                            if (data == null) User(userId = uid) else data.toUser(uid)
                    }
            }
        }
    }

    override suspend fun signUp(
        username: String,
        email: String,
        password: String,
    ): RepoResult<User> {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        if (trimmedUsername.isEmpty() || trimmedEmail.isEmpty() || password.isEmpty()) {
            return RepoResult.Error(RepoError.EMPTY_FIELDS)
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return RepoResult.Error(RepoError.WEAK_PASSWORD)
        }

        return try {
            val availability = checkSignUpAvailable(trimmedUsername, trimmedEmail)
            if (availability is RepoResult.Error) return availability

            val credential = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val uid = credential.user?.uid ?: return RepoResult.Error(RepoError.UNKNOWN)

            val user = User(
                userId = uid,
                username = trimmedUsername,
                email = trimmedEmail,
            )
            firestore.collection(USERS).document(uid).set(user.toFirestoreMap()).await()
            RepoResult.Success(user)
        } catch (e: Exception) {
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun checkSignUpAvailable(
        username: String,
        email: String,
    ): RepoResult<Unit> {
        return try {
            val byUsername = firestore.collection(USERS)
                .whereEqualTo("username", username.trim())
                .limit(1)
                .get()
                .await()
            if (!byUsername.isEmpty) return RepoResult.Error(RepoError.USERNAME_TAKEN)

            val byEmail = firestore.collection(USERS)
                .whereEqualTo("email", email.trim())
                .limit(1)
                .get()
                .await()
            if (!byEmail.isEmpty) return RepoResult.Error(RepoError.EMAIL_TAKEN)

            RepoResult.Success(Unit)
        } catch (e: Exception) {
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun signIn(
        identifier: String,
        password: String,
    ): RepoResult<User> {
        val trimmed = identifier.trim()
        if (trimmed.isEmpty() || password.isEmpty()) return RepoResult.Error(RepoError.EMPTY_FIELDS)

        return try {
            val email = if (trimmed.contains("@")) {
                trimmed
            } else {
                val result = firestore.collection(USERS)
                    .whereEqualTo("username", trimmed)
                    .limit(1)
                    .get()
                    .await()
                val found = result.documents.firstOrNull()
                    ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)
                found.getString("email")
                    ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)
            }

            auth.signInWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.UNKNOWN)
            val profile = firestore.collection(USERS).document(uid).get().await()
            RepoResult.Success(
                profile.data?.toUser(uid) ?: User(userId = uid, email = email),
            )
        } catch (e: Exception) {
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun updatePassword(
        identifier: String?,
        newPassword: String,
    ): RepoResult<Unit> {
        if (newPassword.isEmpty()) return RepoResult.Error(RepoError.EMPTY_FIELDS)
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return RepoResult.Error(RepoError.WEAK_PASSWORD)
        }

        return try {
            val email = resolveEmail(identifier)
                ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)
            auth.sendPasswordResetEmail(email).await()
            RepoResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("Gaply", "Fallo al enviar el correo de recuperación", e)
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun sendResetEmail(email: String): RepoResult<Unit> = try {
        auth.sendPasswordResetEmail(email.trim()).await()
        RepoResult.Success(Unit)
    } catch (e: Exception) {
        Log.e("Gaply", "Fallo al enviar el correo de recuperación", e)
        RepoResult.Error(mapError(e))
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    private suspend fun resolveEmail(identifier: String?): String? {
        val clean = identifier?.trim().orEmpty()
        if (clean.isEmpty()) return auth.currentUser?.email
        if (clean.contains("@")) return clean
        val result = firestore.collection(USERS)
            .whereEqualTo("username", clean)
            .limit(1)
            .get()
            .await()
        return result.documents.firstOrNull()?.getString("email")
    }

    private fun mapError(e: Exception): RepoError = when (e) {
        is FirebaseAuthInvalidUserException -> RepoError.USER_NOT_FOUND
        is FirebaseAuthInvalidCredentialsException -> RepoError.WRONG_CREDENTIALS
        is FirebaseAuthUserCollisionException -> RepoError.EMAIL_TAKEN
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                -> RepoError.CONNECTION

            else -> RepoError.UNKNOWN
        }

        is FirebaseException -> RepoError.CONNECTION
        is IOException, is UnknownHostException, is TimeoutException -> RepoError.CONNECTION
        else -> RepoError.UNKNOWN
    }

    companion object {
        private const val USERS = "users"
        const val MIN_PASSWORD_LENGTH = 6
    }
}