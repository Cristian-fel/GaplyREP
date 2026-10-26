package com.gaply.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.ChatType
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.ActivityRepository
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.ChatsRepository
import com.gaply.app.domain.repository.ScheduleRepository
import com.gaply.app.ui.screens.activities.ActivityFormat
import com.gaply.app.ui.screens.schedule.ScheduleViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Contador de la campana del Descubrir: mensajes sin leer (por su número) más
 * los chats directos que nunca abrí (un match recién creado). Un chat con
 * mensajes pendientes cuenta por esos mensajes y no dos veces.
 */
internal fun badgeDeChats(chats: List<Chat>): Int = chats.sumOf { chat ->
    when {
        chat.unreadCount > 0 -> chat.unreadCount
        chat.esMatchNuevo -> 1
        else -> 0
    }
}

data class HomeUiState(
    val user: User? = null,
    val selectedTab: Int = 0,
    val dialog: HomeDialog? = null,
    /** El mayor hueco de hoy, o null. La pantalla lo formatea con el idioma activo. */
    val huecoDeHoy: HuecoLibre? = null,
    /** Todos los huecos libres de hoy; con ellos se filtra el listado de actividades. */
    val huecosDeHoy: List<HuecoLibre> = emptyList(),
    /** true si el usuario ya tiene al menos una clase en el horario. */
    val tieneHorario: Boolean = false,
)

enum class HomeDialog { CREATE_ACTIVITY, EVENT_SITE }

/**
 * Estado de la sección "Mis matches" y del badge de la campana del Descubrir.
 *
 * @param directos chats directos (dos personas): son exactamente mis matches.
 * @param badge    no leídos + matches a los que todavía no entré.
 */
data class MatchesBarUiState(
    val directos: List<Chat> = emptyList(),
    val badge: Int = 0,
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val scheduleRepository: ScheduleRepository,
    private val activityRepository: ActivityRepository,
    chatsRepository: ChatsRepository,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(0)
    private val dialog = MutableStateFlow<HomeDialog?>(null)
    private val huecosDeHoy = MutableStateFlow<List<HuecoLibre>>(emptyList())
    private val tieneHorario = MutableStateFlow(false)

    /**
     * Hasta 3 actividades reales de hoy que caben en un hueco libre y no son
     * mías: son las tarjetas "Actividades para tu hueco" del Descubrir.
     */
    private val _sugeridas = MutableStateFlow<List<ActivityItem>>(emptyList())
    val sugeridas: StateFlow<List<ActivityItem>> = _sugeridas.asStateFlow()

    private val _matchesBar = MutableStateFlow(MatchesBarUiState())
    val matchesBar: StateFlow<MatchesBarUiState> = _matchesBar.asStateFlow()

    init {
        // La lista de chats en tiempo real alimenta la sección "Mis matches"
        // y el contador de la campana (mismo dato que la pestaña Chats).
        viewModelScope.launch {
            chatsRepository.observeChats().collect { resultado ->
                val chats = (resultado as? RepoResult.Success)?.data ?: return@collect
                _matchesBar.value = MatchesBarUiState(
                    directos = chats.filter { it.type == ChatType.DIRECT },
                    badge = badgeDeChats(chats),
                )
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        selectedTab,
        dialog,
        huecosDeHoy,
        tieneHorario,
    ) { user, tab, currentDialog, huecos, horario ->
        HomeUiState(
            user = user,
            selectedTab = tab,
            dialog = currentDialog,
            // El banner muestra el mayor; el listado de actividades usa la lista completa.
            huecoDeHoy = huecos.maxByOrNull { it.duracionMinutos },
            huecosDeHoy = huecos,
            tieneHorario = horario,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    /**
     * Trae el horario y deja en el estado los huecos libres de hoy: es el
     * mismo dato que reciben el banner y el agente de utilidad para
     * proponer actividades.
     */
    fun cargarHorario() {
        viewModelScope.launch {
            when (val resultado = scheduleRepository.getClasses()) {
                is RepoResult.Success -> {
                    // Las clases se guardan con el día en español (clave canónica).
                    val hoy = ScheduleViewModel.tituloDia(LocalDate.now())
                    val clasesDeHoy = resultado.data.filter { it.day == hoy }
                    val huecos = HuecoLibre
                        .deClases(clasesDeHoy, ScheduleViewModel.HUECO_MINIMO_MINUTOS)
                    huecosDeHoy.value = huecos
                    tieneHorario.value = resultado.data.isNotEmpty()
                    cargarSugeridas(huecos)
                }

                // Sin conexión no se cambia lo que ya se mostraba: la próxima
                // entrada a la pantalla vuelve a intentarlo.
                is RepoResult.Error -> Unit
            }
        }
    }

    /**
     * Actividades de hoy que encajan en un hueco libre (misma percepción que
     * el agente: [ActivityFormat.encajaEnHueco]), excluyendo las mías —
     * para eso está la tarjeta "Tus actividades". Si falla la lectura se
     * conserva lo que ya se mostraba.
     */
    private fun cargarSugeridas(huecos: List<HuecoLibre>) {
        viewModelScope.launch {
            when (val resultado = activityRepository.getActivities()) {
                is RepoResult.Success -> {
                    val hoy = LocalDate.now()
                    _sugeridas.value = resultado.data
                        .filter { !it.isCreatedByMe }
                        .filter { ActivityFormat.esDeFecha(it.date, hoy) }
                        .filter { ActivityFormat.encajaEnHueco(it, huecos, ActivityFormat.diaDe(hoy)) }
                        .sortedByDescending { it.createdAt }
                        .take(3)
                }

                is RepoResult.Error -> Unit
            }
        }
    }

    fun onSelectTab(index: Int) {
        selectedTab.value = index
    }

    fun onShowCreateActivityInfo() {
        dialog.value = HomeDialog.CREATE_ACTIVITY
    }

    fun onShowEventSiteInfo() {
        dialog.value = HomeDialog.EVENT_SITE
    }

    fun consumeDialog() {
        dialog.value = null
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        authRepository = container.authRepository,
                        scheduleRepository = container.scheduleRepository,
                        activityRepository = container.activityRepository,
                        chatsRepository = container.chatsRepository,
                    ) as T
                }
            }
    }
}
