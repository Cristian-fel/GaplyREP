package com.gaply.app.ui.screens.activities

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.di.AppContainer
import com.gaply.app.di.appContainer
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ActivitiesUiState(
    /** Lista pública de actividades publicadas. Arranca COMPLETAMENTE vacía. */
    val activities: List<ActivityItem> = emptyList(),
    /** Usuario actual: foto de perfil e inicial del organizador. */
    val user: User? = null,
)

/**
 * Módulo de Actividades · Estado compartido del módulo.
 *
 * Una sola instancia (scoped a la Activity) mantiene la lista de
 * actividades para que el formulario, el listado y el detalle vean los
 * mismos datos sin importar el orden de navegación.
 *
 * La lista inicia COMPLETAMENTE vacía: no hay actividades quemadas en el
 * código; solo aparecen las que los usuarios publiquen en tiempo de ejecución.
 */
class ActivityViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _activities = MutableStateFlow<List<ActivityItem>>(emptyList())

    val uiState: StateFlow<ActivitiesUiState> = combine(
        _activities,
        authRepository.currentUser,
    ) { activities, user ->
        ActivitiesUiState(activities = activities, user = user)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivitiesUiState(),
    )

    /** Guarda una actividad nueva arriba de la lista (la más reciente primero). */
    fun publish(activity: ActivityItem) {
        _activities.update { listOf(finalize(activity)) + it }
    }

    /** Actualiza una actividad existente (modo edición de "Tus actividades"). */
    fun updateActivity(activity: ActivityItem) {
        val updated = finalize(activity)
        _activities.update { list ->
            list.map { current -> if (current.id == updated.id) updated else current }
        }
    }

    /** Elimina (cancela) una actividad publicada por el usuario. */
    fun deleteActivity(activityId: String) {
        _activities.update { list -> list.filterNot { it.id == activityId } }
    }

    /** Normaliza los campos derivados antes de guardar (publicar o editar). */
    private fun finalize(activity: ActivityItem): ActivityItem = activity.copy(
        imageUri = activity.imageUri?.takeIf { it.isNotBlank() },
        defaultColorHex = activity.defaultColorHex.ifBlank { ActivityItem.DEFAULT_COLOR_HEX },
        attendees = activity.attendees.distinct(),
    )

    fun findById(id: String): ActivityItem? = _activities.value.find { it.id == id }

    /** Marca "Unirme": isJoined = true y +1 asistente con el usuario actual. */
    fun join(activityId: String) {
        val user = authRepository.currentUser.value
        val attendeeName = user?.firstName?.ifBlank { null }
            ?: user?.username?.ifBlank { null }
            ?: "Tú"
        _activities.update { list ->
            list.map { activity ->
                if (activity.id != activityId) {
                    activity
                } else {
                    val alreadyIn = activity.attendees.any {
                        it.equals(attendeeName, ignoreCase = true)
                    }
                    activity.copy(
                        isJoined = true,
                        attendees = if (alreadyIn) {
                            activity.attendees
                        } else {
                            activity.attendees + attendeeName
                        },
                    )
                }
            }
        }
    }

    /** Inicial del organizador: la del usuario logueado o "T". */
    fun organizerInitial(): String {
        val user = authRepository.currentUser.value
        val name = user?.firstName?.ifBlank { null } ?: user?.username?.ifBlank { null }
        return name?.firstOrNull()?.uppercaseChar()?.toString() ?: "T"
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ActivityViewModel(
                        authRepository = container.authRepository,
                    ) as T
                }
            }
    }
}

/**
 * Instancia del ViewModel compartida por las 3 pantallas del módulo
 * (formulario, listado y detalle). Se ancla a la Activity para que la
 * lista de actividades sobreviva a los cambios de destino de navegación.
 */
@Composable
fun rememberActivityViewModel(): ActivityViewModel {
    val context = LocalContext.current
    val owner = context as ComponentActivity
    return viewModel(
        viewModelStoreOwner = owner,
        key = "gaply_activities",
        factory = ActivityViewModel.factory(context.appContainer()),
    )
}
