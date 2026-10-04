package com.gaply.app.di

import android.content.Context
import com.gaply.app.GaplyApp
import com.gaply.app.data.local.LocalUserStore
import com.gaply.app.data.repository.FirebaseAuthRepository
import com.gaply.app.data.repository.FirebaseUserRepository
import com.gaply.app.data.repository.InMemoryAuthRepository
import com.gaply.app.data.repository.LocalUserRepository
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.UserRepository

class AppContainer(context: Context) {

    private val userStore = LocalUserStore(context)

    val authRepository: AuthRepository = if (USE_FIREBASE) {
        FirebaseAuthRepository()
    } else {
        InMemoryAuthRepository(userStore)
    }

    val userRepository: UserRepository = if (USE_FIREBASE) {
        FirebaseUserRepository()
    } else {
        LocalUserRepository(userStore)
    }

    companion object {

        /**
         * true = Firebase Auth + Firestore (requiere google-services.json).
         * false = respaldo local en memoria (para probar sin conexion).
         */
        const val USE_FIREBASE = true
    }
}

fun Context.appContainer(): AppContainer = (applicationContext as GaplyApp).container
