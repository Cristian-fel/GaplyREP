package com.gaply.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.ScheduleClass
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.ScheduleRepository
import com.gaply.app.ui.screens.schedule.ScheduleViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val user: User? = null,
    val selectedTab: Int = 0,
    val dialog: HomeDialog? = null,
    /** El mayor hueco de hoy, o null. La pantalla lo formatea con el idioma activo. */
    val huecoDeHoy: HuecoLibre? = null,
    /** true si el usuario ya tiene al menos una clase en el horario. */
    val tieneHorario: Boolean = false,
)

enum class HomeDialog { CREATE_ACTIVITY, EVENT_SITE }

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(0)
    private val dialog = MutableStateFlow<HomeDialog?>(null)
    private val huecoDeHoy = MutableStateFlow<HuecoLibre?>(null)
    private val tieneHorario = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        selectedTab,
        dialog,
        huecoDeHoy,
        tieneHorario,
    ) { user, tab, currentDialog, hueco, horario ->
        HomeUiState(
            user = user,
            selectedTab = tab,
            dialog = currentDialog,
            huecoDeHoy = hueco,
            tieneHorario = horario,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    /**
     * Trae el horario y deja en el estado el hueco más grande de hoy: es el
     * mismo dato que recibe el agente de utilidad para proponer actividades.
     */
    fun cargarHorario() {
        viewModelScope.launch {
            when (val resultado = scheduleRepository.getClasses()) {
                is RepoResult.Success -> {
                    // Las clases se guardan con el día en español (clave canónica).
                    val hoy = ScheduleViewModel.tituloDia(LocalDate.now())
                    val clasesDeHoy = resultado.data.filter { it.day == hoy }
                    huecoDeHoy.value = HuecoLibre
                        .deClases(clasesDeHoy, ScheduleViewModel.HUECO_MINIMO_MINUTOS)
                        .maxByOrNull { it.duracionMinutos }
                    tieneHorario.value = resultado.data.isNotEmpty()
                }

                // Sin conexión no se cambia lo que ya se mostraba: la próxima
                // entrada a la pantalla vuelve a intentarlo.
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
                    ) as T
                }
            }
    }
}
