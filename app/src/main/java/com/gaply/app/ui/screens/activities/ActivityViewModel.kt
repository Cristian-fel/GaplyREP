package com.gaply.app.ui.screens.activities

import android.content.ContextWrapper
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
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.ActivityRepository
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class ActivitiesUiState(
    /** Lista pública de actividades publicadas (cargada desde el repositorio). */
    val activities: List<ActivityItem> = emptyList(),
    /** Usuario actual: foto de perfil e inicial del organizador. */
    val user: User? = null,
    /** Fotos de perfil (uid -> URL) del dueño y los asistentes, para el detalle. */
    val fotos: Map<String, String> = emptyMap(),
    /** true mientras se carga o se guarda en el repositorio. */
    val cargando: Boolean = true,
    /** Último error de guardado/borrado a mostrar en pantalla; null = sin error. */
    val error: String? = null,
)

/**
 * Módulo de Actividades · Estado compartido del módulo.
 *
 * Una sola instancia (scoped a la Activity) mantiene la lista de
 * actividades para que el formulario, el listado y el detalle vean los
 * mismos datos sin importar el orden de navegación.
 *
 * La lista se carga desde [ActivityRepository] (Firestore en producción) al
 * abrir el módulo y cada operación de escritura pasa por el repositorio: lo
 * que publica un usuario sobrevive al cierre de la app.
 */
class ActivityViewModel(
    private val authRepository: AuthRepository,
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    private val _activities = MutableStateFlow<List<ActivityItem>>(emptyList())
    private val _fotos = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _cargando = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ActivitiesUiState> = combine(
        _activities,
        authRepository.currentUser,
        _cargando,
        _error,
        _fotos,
    ) { activities, user, cargando, error, fotos ->
        ActivitiesUiState(
            activities = activities,
            user = user,
            fotos = fotos,
            cargando = cargando,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivitiesUiState(),
    )

    init {
        refresh()
    }

    /** Recarga la lista desde el repositorio (al abrir y tras cada escritura). */
    fun refresh() {
        viewModelScope.launch {
            _cargando.value = true
            when (val result = activityRepository.getActivities()) {
                is RepoResult.Success -> {
                    _activities.value = result.data
                    _error.value = null
                    cargarFotos(result.data)
                }

                is RepoResult.Error -> {
                    // Si ya había lista en memoria (p. ej. error tras publicar),
                    // se conserva y solo se avisa del fallo.
                    _error.value = mensajeDe(result)
                }
            }
            _cargando.value = false
        }
    }

    /**
     * Publica una actividad nueva: la guarda en el repositorio con los
     * datos de control (dueño, fecha de creación) y la refleja en la lista.
     */
    fun publish(activity: ActivityItem) {
        viewModelScope.launch {
            _cargando.value = true
            val dueno = subirFoto(
                finalize(activity).copy(
                    ownerId = userId().orEmpty(),
                    ownerName = userName(),
                    organizerInitial = organizerInitial(),
                    attendeeIds = emptyList(),
                    attendees = emptyList(),
                    isJoined = false,
                    isCreatedByMe = true,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            when (val result = activityRepository.save(dueno)) {
                is RepoResult.Success -> {
                    _error.value = null
                    refresh()
                }

                is RepoResult.Error -> _error.value = mensajeDe(result)
            }
            _cargando.value = false
        }
    }

    /** Actualiza una actividad existente (modo edición de "Tus actividades"). */
    fun updateActivity(activity: ActivityItem) {
        viewModelScope.launch {
            _cargando.value = true
            val previa = _activities.value.find { it.id == activity.id }
            // Se conservan dueño, asistentes y fecha de creación originales:
            // el formulario solo toca los datos del evento.
            val editada = subirFoto(
                finalize(activity).copy(
                    ownerId = previa?.ownerId ?: activity.ownerId,
                    ownerName = previa?.ownerName ?: activity.ownerName,
                    organizerInitial = previa?.organizerInitial ?: activity.organizerInitial,
                    attendeeIds = previa?.attendeeIds ?: activity.attendeeIds,
                    attendees = previa?.attendees ?: activity.attendees,
                    createdAt = previa?.createdAt ?: activity.createdAt,
                    isJoined = previa?.isJoined ?: activity.isJoined,
                    isCreatedByMe = previa?.isCreatedByMe ?: activity.isCreatedByMe,
                ),
            )
            when (val result = activityRepository.save(editada)) {
                is RepoResult.Success -> {
                    _error.value = null
                    refresh()
                }

                is RepoResult.Error -> _error.value = mensajeDe(result)
            }
            _cargando.value = false
        }
    }

    /** Elimina (cancela) una actividad publicada por el usuario. */
    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            _cargando.value = true
            when (val result = activityRepository.delete(activityId)) {
                is RepoResult.Success -> {
                    _error.value = null
                    refresh()
                }

                is RepoResult.Error -> _error.value = mensajeDe(result)
            }
            _cargando.value = false
        }
    }

    /** Limpia el error mostrado (al cerrar el diálogo). */
    fun clearError() {
        _error.value = null
    }

    /** Normaliza los campos derivados antes de guardar (publicar o editar). */
    private fun finalize(activity: ActivityItem): ActivityItem = activity.copy(
        imageUri = activity.imageUri?.takeIf { it.isNotBlank() },
        defaultColorHex = activity.defaultColorHex.ifBlank { ActivityItem.DEFAULT_COLOR_HEX },
        attendees = activity.attendees.distinct(),
    )

    /**
     * Si la foto viene de la galería (content:// o file://) se sube a Storage
     * en `activities/{uid}/{activityId}.jpg` y aquí queda su downloadUrl, para
     * que se muestre desde cualquier perfil. Si no hay foto o la subida falla,
     * se guarda sin foto y la UI pinta el color sólido por defecto.
     */
    private suspend fun subirFoto(activity: ActivityItem): ActivityItem {
        val uri = activity.imageUri ?: return activity
        val esLocal = uri.startsWith("content://") || uri.startsWith("file://")
        if (!esLocal) return activity
        val subida = withTimeoutOrNull(SUBIDA_FOTO_TIMEOUT_MS) {
            activityRepository.uploadPlacePhoto(activity.id, uri)
        }
        return when (subida) {
            is RepoResult.Success -> activity.copy(imageUri = subida.data)
            else -> activity.copy(imageUri = null)
        }
    }

    /** Fotos de perfil (uid -> URL) del dueño y los asistentes de la lista. */
    private fun cargarFotos(actividades: List<ActivityItem>) {
        val uids = actividades
            .flatMap { listOf(it.ownerId) + it.attendeeIds }
            .filter { it.isNotBlank() }
            .distinct()
        if (uids.isEmpty()) return
        viewModelScope.launch {
            when (val result = activityRepository.getUserPhotos(uids)) {
                is RepoResult.Success -> _fotos.value = result.data
                is RepoResult.Error -> Unit
            }
        }
    }

    fun findById(id: String): ActivityItem? = _activities.value.find { it.id == id }

    /** Marca "Unirme": +1 asistente en el repositorio (uid y nombre). */
    fun join(activityId: String) {
        val attendeeName = userName()
        viewModelScope.launch {
            when (val result = activityRepository.join(activityId, attendeeName)) {
                is RepoResult.Success -> {
                    _error.value = null
                    refresh()
                }

                is RepoResult.Error -> _error.value = mensajeDe(result)
            }
        }
    }

    /** Sale de la actividad (deshace "Unirme"): quita al usuario de los asistentes. */
    fun leave(activityId: String) {
        val attendeeName = userName()
        viewModelScope.launch {
            when (val result = activityRepository.leave(activityId, attendeeName)) {
                is RepoResult.Success -> {
                    _error.value = null
                    refresh()
                }

                is RepoResult.Error -> _error.value = mensajeDe(result)
            }
        }
    }

    /** Uid del usuario autenticado (dueño de las actividades que publica). */
    private fun userId(): String? = authRepository.currentUser.value?.userId

    /** Nombre visible del usuario actual (asistente o dueño). */
    private fun userName(): String {
        val user = authRepository.currentUser.value
        return user?.firstName?.ifBlank { null }
            ?: user?.username?.ifBlank { null }
            ?: "Tú"
    }

    /** Inicial del organizador: la del usuario logueado o "T". */
    fun organizerInitial(): String {
        val name = userName()
        return name.firstOrNull()?.uppercaseChar()?.toString() ?: "T"
    }

    /** Texto legible del error para mostrar en el diálogo de la UI. */
    private fun mensajeDe(result: RepoResult.Error): String = when (result.type) {
        com.gaply.app.domain.model.RepoError.CONNECTION ->
            "Sin conexión con el servidor. Revisa tu internet e inténtalo de nuevo."

        com.gaply.app.domain.model.RepoError.PERMISSION ->
            "No tienes permiso para esta acción (revisa que iniciaste sesión)."

        else -> "No se pudo guardar el cambio. Inténtalo de nuevo."
    }

    companion object {
        /** Tope para subir la foto del lugar: sin red no frena el guardado. */
        private const val SUBIDA_FOTO_TIMEOUT_MS = 20_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ActivityViewModel(
                        authRepository = container.authRepository,
                        activityRepository = container.activityRepository,
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
    val owner = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<ComponentActivity>()
        .firstOrNull()
        ?: error("No se encontró ComponentActivity en la cadena de contextos")
    return viewModel(
        viewModelStoreOwner = owner,
        key = "gaply_activities",
        factory = ActivityViewModel.factory(context.appContainer()),
    )
}
