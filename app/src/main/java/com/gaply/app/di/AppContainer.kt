package com.gaply.app.di

import android.content.Context
import com.gaply.app.GaplyApp
import com.gaply.app.data.local.LocalUserStore
import com.gaply.app.data.repository.FirebaseActivityRepository
import com.gaply.app.data.repository.FirebaseChatsRepository
import com.gaply.app.data.repository.FirebaseMatchesRepository
import com.gaply.app.data.repository.FirebaseAuthRepository
import com.gaply.app.data.repository.FirebaseScheduleRepository
import com.gaply.app.data.repository.FirebaseRatingsRepository
import com.gaply.app.data.repository.FirebaseUserRepository
import com.gaply.app.data.repository.InMemoryAuthRepository
import com.gaply.app.data.repository.InMemoryChatsRepository
import com.gaply.app.data.repository.LocalActivityRepository
import com.gaply.app.data.repository.LocalMatchesRepository
import com.gaply.app.data.repository.LocalRatingsRepository
import com.gaply.app.data.repository.LocalScheduleRepository
import com.gaply.app.data.repository.LocalUserRepository
import com.gaply.app.domain.repository.ActivityRepository
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.ChatsRepository
import com.gaply.app.domain.repository.MatchesRepository
import com.gaply.app.domain.repository.RatingsRepository
import com.gaply.app.domain.repository.ScheduleRepository
import com.gaply.app.domain.repository.UserRepository

class AppContainer(context: Context) : LanguagePreference {

    private val userStore = LocalUserStore(context)

    /** Preferencia de idioma de Configuración (lectura reactiva + guardado). */
    override val appLanguage: kotlinx.coroutines.flow.StateFlow<com.gaply.app.core.i18n.AppLanguage> =
        userStore.language

    override fun setAppLanguage(language: com.gaply.app.core.i18n.AppLanguage) =
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

    /**
     * Módulo 5 · Matches: swipes en la colección global `swipes` (id
     * `swipe_{fromUid}_{targetId}`) y mazo de candidatos (con sus huecos
     * libres derivados del horario).
     */
    val matchesRepository: MatchesRepository = if (USE_FIREBASE) {
        FirebaseMatchesRepository()
    } else {
        // El respaldo local lee el horario compartido del dispositivo.
        LocalMatchesRepository(userStore, scheduleRepository)
    }

    /**
     * Módulo 5 · Chats: `chats/{chatId}` + `messages` en tiempo real, con el
     * respaldo en memoria para probar sin conexión.
     */
    val chatsRepository: ChatsRepository = if (USE_FIREBASE) {
        FirebaseChatsRepository()
    } else {
        InMemoryChatsRepository(userStore)
    }

    /**
     * Filtro de categoría del listado "Actividades para tu hueco": lo escribe
     * el Home al tocar un lugar y lo lee el listado (null = ver todo). Vive
     * aquí (igual que appLanguage) para no depender de argumentos de navegación.
     */
    val filtroActividades: kotlinx.coroutines.flow.MutableStateFlow<String?> =
        kotlinx.coroutines.flow.MutableStateFlow(null)

    /**
     * Hueco tocado en la banda del horario (día + rango): el listado se
     * filtra por ese hueco exacto en lugar de por los huecos de hoy.
     */
    val filtroFranja: kotlinx.coroutines.flow.MutableStateFlow<com.gaply.app.domain.model.FiltroFranja?> =
        kotlinx.coroutines.flow.MutableStateFlow(null)

    val ratingsRepository: RatingsRepository = if (USE_FIREBASE) {
        FirebaseRatingsRepository()
    } else {
        LocalRatingsRepository()
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
