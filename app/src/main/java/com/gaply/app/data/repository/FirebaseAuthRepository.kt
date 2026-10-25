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
import com.google.firebase.firestore.FieldValue
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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
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
                val byEmail = firestore.collection(USERS)
                    .whereEqualTo("email", trimmed)
                    .limit(1)
                    .get()
                    .await()
                if (byEmail.isEmpty) {
                    return RepoResult.Error(RepoError.EMAIL_NOT_FOUND)
                }
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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Gaply", "Fallo al enviar el correo de recuperación", e)
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun sendResetEmail(email: String): RepoResult<Unit> = try {
        auth.sendPasswordResetEmail(email.trim()).await()
        RepoResult.Success(Unit)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("Gaply", "Fallo al enviar el correo de recuperación", e)
        RepoResult.Error(mapError(e))
    }

    override suspend fun changePassword(newPassword: String): RepoResult<Unit> {
        if (newPassword.isEmpty()) return RepoResult.Error(RepoError.EMPTY_FIELDS)
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return RepoResult.Error(RepoError.WEAK_PASSWORD)
        }
        val user = auth.currentUser ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)

        return try {
            // updatePassword exige sesión reciente; si Firebase lo pide,
            // mapError lo traduce a PERMISSION y la UI pide reautenticarse.
            user.updatePassword(newPassword).await()
            RepoResult.Success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Gaply", "Fallo al cambiar la contraseña", e)
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun reauthenticate(currentPassword: String): RepoResult<Unit> {
        val user = auth.currentUser ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)
        val email = user.email ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)
        if (currentPassword.isEmpty()) return RepoResult.Error(RepoError.EMPTY_FIELDS)

        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            RepoResult.Success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Gaply", "Fallo al reautenticar", e)
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun deactivateAccount(): RepoResult<Unit> {
        val uid = auth.currentUser?.uid ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)

        return try {
            // Se conservan todos los datos; solo se marca la cuenta como
            // desactivada para que deje de ser visible/usable hasta reactivarla.
            firestore.collection(USERS).document(uid)
                .set(mapOf(KEY_DEACTIVATED to true), com.google.firebase.firestore.SetOptions.merge())
                .await()
            auth.signOut()
            RepoResult.Success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Gaply", "Fallo al desactivar la cuenta", e)
            RepoResult.Error(mapError(e))
        }
    }

    override suspend fun deleteAccount(): RepoResult<Unit> {
        val user = auth.currentUser ?: return RepoResult.Error(RepoError.USER_NOT_FOUND)

        return try {
            // Primero el documento de Firestore, después la cuenta de Auth
            // (si fallara Auth, no quedaría un usuario huérfano en la app).
            borrarPerfil(user.uid)
            user.delete().await()
            RepoResult.Success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Gaply", "Fallo al eliminar la cuenta", e)
            RepoResult.Error(mapError(e))
        }
    }

    /**
     * Borra `users/{uid}`; si `firestore.rules` lo prohíbe (`allow delete:
     * if false`) se anonimiza el perfil con un `update` (permitido al dueño)
     * para que con lectura pública no quede nada personal, y la cuenta de Auth
     * se elimina igual. Si algún día las reglas permiten borrar, se borra.
     */
    private suspend fun borrarPerfil(uid: String) {
        try {
            firestore.collection(USERS).document(uid).delete().await()
        } catch (e: FirebaseFirestoreException) {
            if (e.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) throw e
            Log.w("Gaply", "users/{uid} no permite delete: se anonimiza el perfil")
            firestore.collection(USERS).document(uid).update(
                mapOf(
                    "userId" to "",
                    "username" to "",
                    "email" to "",
                    "firstName" to "",
                    "lastName" to "",
                    "birthDate" to "",
                    "university" to "",
                    "gender" to "",
                    "goal" to "",
                    "interests" to emptyList<String>(),
                    "bio" to "",
                    "profilePictureUrl" to "",
                    "receiveMarketing" to false,
                    "schedule" to FieldValue.delete(),
                    "rating" to FieldValue.delete(),
                    KEY_DEACTIVATED to FieldValue.delete(),
                ),
            ).await()
        }
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
        is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ->
            // Cambiar/eliminar contraseña exige reautenticación reciente
            RepoError.PERMISSION
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
        private const val KEY_DEACTIVATED = "deactivated"
        const val MIN_PASSWORD_LENGTH = 6
    }
}