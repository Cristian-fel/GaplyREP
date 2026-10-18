package com.gaply.app.di

import android.content.Context
import com.gaply.app.GaplyApp
import com.gaply.app.data.local.LocalUserStore
import com.gaply.app.data.repository.FirebaseActivityRepository
import com.gaply.app.data.repository.FirebaseAuthRepository
import com.gaply.app.data.repository.FirebaseScheduleRepository
import com.gaply.app.data.repository.FirebaseUserRepository
import com.gaply.app.data.repository.InMemoryAuthRepository
import com.gaply.app.data.repository.LocalActivityRepository
import com.gaply.app.data.repository.LocalScheduleRepository
import com.gaply.app.data.repository.LocalUserRepository
import com.gaply.app.domain.repository.ActivityRepository
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.ScheduleRepository
import com.gaply.app.domain.repository.UserRepository

class AppContainer(context: Context) {

    private val userStore = LocalUserStore(context)

    /** Preferencia de idioma de Configuración (lectura reactiva + guardado). */
    val appLanguage: kotlinx.coroutines.flow.StateFlow<com.gaply.app.core.i18n.AppLanguage> =
        userStore.language

    fun setAppLanguage(language: com.gaply.app.core.i18n.AppLanguage) =
        userStore.setLanguage(language)

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

    val scheduleRepository: ScheduleRepository = if (USE_FIREBASE) {
        FirebaseScheduleRepository()
    } else {
        LocalScheduleRepository()
    }

    val activityRepository: ActivityRepository = if (USE_FIREBASE) {
        FirebaseActivityRepository()
    } else {
        LocalActivityRepository()
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
