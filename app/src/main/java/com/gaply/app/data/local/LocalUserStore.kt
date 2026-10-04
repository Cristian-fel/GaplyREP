package com.gaply.app.data.local

import android.content.Context
import com.gaply.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

class LocalUserStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadSessionUser())
    val currentUser: StateFlow<User?> = _currentUser

    fun loadUsers(): List<User> {
        val raw = prefs.getString(KEY_USERS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                runCatching { array.getJSONObject(index).toUser() }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    fun saveUsers(users: List<User>) {
        val array = JSONArray()
        users.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_USERS, array.toString()).apply()
        refreshCurrentFromSession()
    }

    fun setSession(userId: String?) {
        prefs.edit().putString(KEY_SESSION, userId).apply()
        _currentUser.value = userId?.let { id -> loadUsers().find { it.userId == id } }
    }

    fun refreshCurrentFromSession() {
        val sessionId = prefs.getString(KEY_SESSION, null)
        _currentUser.value = sessionId?.let { id -> loadUsers().find { it.userId == id } }
    }

    private fun loadSessionUser(): User? {
        val sessionId = prefs.getString(KEY_SESSION, null) ?: return null
        return loadUsers().find { it.userId == sessionId }
    }

    private fun User.toJson(): JSONObject = JSONObject().apply {
        put("userId", userId)
        put("username", username)
        put("email", email)
        put("passwordHash", passwordHash)
        put("firstName", firstName)
        put("lastName", lastName)
        put("birthDate", birthDate)
        put("university", university)
        put("gender", gender)
        put("goal", goal)
        put("interests", JSONArray(interests))
        put("bio", bio)
        put("profilePictureUrl", profilePictureUrl)
        put("receiveMarketing", receiveMarketing)
    }

    private fun JSONObject.toUser(): User = User(
        userId = optString("userId"),
        username = optString("username"),
        email = optString("email"),
        passwordHash = optString("passwordHash"),
        firstName = optString("firstName"),
        lastName = optString("lastName"),
        birthDate = optString("birthDate"),
        university = optString("university"),
        gender = optString("gender"),
        goal = optString("goal"),
        interests = run {
            val array = optJSONArray("interests")
            if (array == null) emptyList()
            else (0 until array.length()).map { array.getString(it) }
        },
        bio = optString("bio"),
        profilePictureUrl = optString("profilePictureUrl"),
        receiveMarketing = optBoolean("receiveMarketing", false),
    )

    companion object {
        private const val PREFS_NAME = "gaply_store"
        private const val KEY_USERS = "users"
        private const val KEY_SESSION = "session_user_id"
    }
}
