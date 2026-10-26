package com.gaply.app.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.StringsEs
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.Message
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.ActivityRepository
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.ChatsRepository
import com.gaply.app.domain.repository.MatchesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Estados de la conversación (Loading / Ready / Empty / Error). */
enum class ChatDetailPhase { LOADING, READY, EMPTY, ERROR }

data class ChatDetailUiState(
    val phase: ChatDetailPhase = ChatDetailPhase.LOADING,
    /** Uid del usuario actual: la UI lo usa para pintar "mis" burbujas. */
    val miUid: String = "",
    val chat: Chat? = null,
    /** Mensajes en orden ascendente (Firestore devuelve los 50 más recientes). */
    val mensajes: List<Message> = emptyList(),
    val enviando: Boolean = false,
    val error: RepoError? = null,
    /** Solo para chats de grupo: la actividad, para el estado del top bar. */
    val actividad: ActivityItem? = null,
    /** Perfil abierto desde un aviso de sistema (vista previa). */
    val perfilPreview: User? = null,
)

/**
 * Módulo 5 · Chats · Conversación.
 *
 * - Escucha los mensajes en tiempo real (los 50 más recientes) y los expone
 *   en orden ascendente para pintarlos de abajo hacia arriba.
 * - Envío optimista: el mensaje sale al instante desde el estado local y se
 *   sustituye por el real en cuanto llega el snapshot.
 * - Al abrir, pone mi contador de no leídos en 0.
 */
class ChatDetailViewModel(
    private val chatId: String,
    private val chatsRepository: ChatsRepository,
    private val authRepository: AuthRepository,
    private val activityRepository: ActivityRepository,
    private val matchesRepository: MatchesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatDetailUiState(miUid = authRepository.currentUser.value?.userId.orEmpty()),
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    /** Textos activos: los usa para el nombre visible del que envía. */
    var strings: AppStrings = StringsEs

    /** Mensajes locales aún no confirmados por Firestore. */
    private val pendientes = MutableStateFlow<List<Message>>(emptyList())

    private var actividadCargada = false

    init {
        // Con Firebase el usuario puede tardar: se espera (con tope) para que
        // la UI sepa qué burbujas son mías.
        viewModelScope.launch {
            val usuario = withTimeoutOrNull(5_000L) {
                authRepository.currentUser.first { it != null }
            } ?: return@launch
            _uiState.update { it.copy(miUid = usuario.userId) }
        }

        viewModelScope.launch {
            chatsRepository.observeMessages(chatId).collect { resultado ->
                when (resultado) {
                    is RepoResult.Success -> {
                        // La query del servidor ya viene ordenada por fecha
                        // descendente (la autoridad es Firestore, no el reloj
                        // de cada teléfono): al invertirla queda "lo más
                        // antiguo arriba, lo nuevo abajo". No se reordena por
                        // el timestamp leído en el cliente, porque si un
                        // cliente no lo parsea (0) el orden se caía justo en
                        // lo contrario a lo pedido.
                        val reales = resultado.data.reversed()
                        _uiState.update {
                            it.copy(
                                phase = ChatDetailPhase.READY,
                                mensajes = combinar(reales, pendientes.value),
                                error = null,
                            )
                        }
                    }

                    is RepoResult.Error -> _uiState.update {
                        it.copy(phase = ChatDetailPhase.ERROR, error = resultado.type)
                    }
                }
            }
        }

        viewModelScope.launch {
            chatsRepository.observeChats().collect { resultado ->
                if (resultado is RepoResult.Success) {
                    val chat = resultado.data.find { it.id == chatId } ?: return@collect
                    _uiState.update { it.copy(chat = chat) }
                    cargarActividad(chat)
                }
            }
        }

        // El badge de no leídos se limpia al abrir la conversación.
        viewModelScope.launch { chatsRepository.markAsRead(chatId) }
    }

    fun enviar(texto: String) {
        val contenido = texto.trim()
        if (contenido.isEmpty() || _uiState.value.enviando) return

        val uid = authRepository.currentUser.value?.userId.orEmpty()
        val nombre = nombreVisible()
        val temporal = Message(
            id = "local_${System.currentTimeMillis()}",
            senderId = uid,
            senderName = nombre,
            text = contenido,
            timestamp = System.currentTimeMillis(),
        )
        pendientes.value = pendientes.value + temporal
        refrescarPendientes()
        _uiState.update { it.copy(enviando = true) }

        viewModelScope.launch {
            when (
                val resultado = chatsRepository.sendMessage(
                    chatId = chatId,
                    senderName = nombre,
                    text = contenido,
                    // Los miembros vienen del chat ya cargado: así no hay que
                    // releerlo (las reglas solo dejan leer siendo miembro).
                    members = _uiState.value.chat?.participants.orEmpty(),
                    // La foto va dentro del mensaje: cada burbuja la pinta sin
                    // releer perfiles.
                    senderPhotoUrl = authRepository.currentUser.value?.profilePictureUrl.orEmpty(),
                )
            ) {
                is RepoResult.Success -> {
                    // El snapshot del listener se encarga de mostrarlo "real".
                    Unit
                }

                is RepoResult.Error -> {
                    pendientes.value = pendientes.value.filterNot { it.id == temporal.id }
                    refrescarPendientes()
                    _uiState.update { it.copy(error = resultado.type) }
                }
            }
            _uiState.update { it.copy(enviando = false) }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    /** Vista previa del perfil de quien entró o salió del grupo (aviso tocado). */
    fun verPerfil(uid: String) {
        if (uid.isBlank()) return
        viewModelScope.launch {
            when (val resultado = matchesRepository.getUserProfile(uid)) {
                is RepoResult.Success -> _uiState.update {
                    it.copy(
                        perfilPreview = resultado.data
                            ?: return@update it.copy(error = RepoError.USER_NOT_FOUND),
                    )
                }

                is RepoResult.Error -> _uiState.update { it.copy(error = resultado.type) }
            }
        }
    }

    fun cerrarPerfil() {
        _uiState.update { it.copy(perfilPreview = null) }
    }

    /** Mezcla lo real con lo optimista sin duplicar (mismo emisor + texto). */
    private fun combinar(reales: List<Message>, locales: List<Message>): List<Message> {
        val confirmados = reales.map { it.senderId to it.text }.toSet()
        return reales + locales.filterNot { (it.senderId to it.text) in confirmados }
    }

    private fun refrescarPendientes() {
        _uiState.update { estado ->
            val reales = estado.mensajes.filterNot { mensaje ->
                pendientes.value.any { it.id == mensaje.id }
            }
            estado.copy(mensajes = combinar(reales, pendientes.value))
        }
    }

    /** Solo los chats de grupo traen actividad: el top bar muestra su estado. */
    private fun cargarActividad(chat: Chat) {
        val actividadId = chat.activityId
        if (actividadId.isNullOrBlank() || actividadCargada) return
        actividadCargada = true
        viewModelScope.launch {
            when (val resultado = activityRepository.getActivities()) {
                is RepoResult.Success -> _uiState.update {
                    it.copy(actividad = resultado.data.find { a -> a.id == actividadId })
                }

                is RepoResult.Error -> Unit
            }
        }
    }

    private fun nombreVisible(): String {
        val user = authRepository.currentUser.value
        return user?.firstName?.ifBlank { null }
            ?: user?.username?.ifBlank { null }
            ?: strings.profileFallbackUsername
    }

    companion object {
        fun factory(container: AppContainer, chatId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatDetailViewModel(
                        chatId = chatId,
                        chatsRepository = container.chatsRepository,
                        authRepository = container.authRepository,
                        activityRepository = container.activityRepository,
                        matchesRepository = container.matchesRepository,
                    ) as T
                }
            }
    }
}
