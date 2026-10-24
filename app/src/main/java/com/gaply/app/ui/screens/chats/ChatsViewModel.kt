package com.gaply.app.ui.screens.chats

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
import com.gaply.app.domain.model.ChatMessage
import com.gaply.app.domain.model.ChatRoom
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Lista de conversaciones (pestaña Chats)
// ---------------------------------------------------------------------------

data class ChatsUiState(
    val loading: Boolean = true,
    val error: RepoError? = null,
    /** uid del usuario actual (para saber quién es "el otro" en cada sala). */
    val yo: String = "",
    /** Salas donde participo, de la más reciente a la más vieja. */
    val salas: List<ChatRoom> = emptyList(),
) {
    val vacio: Boolean get() = !loading && error == null && salas.isEmpty()
}

class ChatsViewModel(
    private val authRepository: AuthRepository,
    private val matchRepository: MatchRepository,
) : ViewModel() {

    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<RepoError?>(null)
    private val salas = MutableStateFlow<List<ChatRoom>>(emptyList())

    val uiState: StateFlow<ChatsUiState> = combine(
        loading,
        error,
        salas,
    ) { cargando, err, lista ->
        ChatsUiState(
            loading = cargando,
            error = err,
            yo = authRepository.currentUser.value?.userId.orEmpty(),
            salas = lista,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatsUiState(),
    )

    init {
        cargar()
    }

    /** Trae mis conversaciones; se repite al entrar a la pestaña (refresco simple). */
    fun cargar() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            when (val resultado = matchRepository.getChats()) {
                is RepoResult.Success -> {
                    salas.value = resultado.data
                    loading.value = false
                }

                is RepoResult.Error -> {
                    error.value = resultado.type
                    loading.value = false
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatsViewModel(
                        authRepository = container.authRepository,
                        matchRepository = container.matchRepository,
                    ) as T
                }
            }
    }
}

/** Instancia compartida de la lista de chats (anclada a la Activity). */
@Composable
fun rememberChatsViewModel(): ChatsViewModel {
    val context = LocalContext.current
    val owner = activityAncla(context)
    return viewModel(
        viewModelStoreOwner = owner,
        key = "gaply_chats",
        factory = ChatsViewModel.factory(context.appContainer()),
    )
}

// ---------------------------------------------------------------------------
// Conversación (ruta chat/{chatId})
// ---------------------------------------------------------------------------

data class ChatUiState(
    val loading: Boolean = true,
    val error: RepoError? = null,
    /** uid del usuario actual (para saber qué burbujas son mías). */
    val yo: String = "",
    /** true = chat del evento (grupo), false = dm entre dos personas. */
    val esGrupo: Boolean = false,
    /** Nombre de la otra persona (título de la cabecera). */
    val titulo: String = "",
    /** Foto de la otra persona (respaldo: inicial). */
    val foto: String = "",
    val mensajes: List<ChatMessage> = emptyList(),
)

class ChatViewModel(
    private val chatId: String,
    private val authRepository: AuthRepository,
    private val matchRepository: MatchRepository,
) : ViewModel() {

    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<RepoError?>(null)
    private val sala = MutableStateFlow<ChatRoom?>(null)
    private val mensajes = MutableStateFlow<List<ChatMessage>>(emptyList())

    val uiState: StateFlow<ChatUiState> = combine(
        loading,
        error,
        sala,
        mensajes,
    ) { cargando, err, chat, lista ->
        val yo = authRepository.currentUser.value?.userId.orEmpty()
        val otro = chat?.memberIds?.firstOrNull { it != yo }.orEmpty()
        ChatUiState(
            loading = cargando,
            error = err,
            yo = yo,
            esGrupo = chat?.kind == "group",
            titulo = if (chat?.kind == "group") {
                chat.groupTitle
            } else {
                chat?.memberNames?.get(otro).orEmpty()
            },
            foto = chat?.memberPhotos?.get(otro).orEmpty(),
            mensajes = lista,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatUiState(),
    )

    init {
        // Título de la sala (una sola lectura)…
        viewModelScope.launch {
            when (val resultado = matchRepository.getChats()) {
                is RepoResult.Success -> sala.value = resultado.data.firstOrNull { it.id == chatId }
                is RepoResult.Error -> error.value = resultado.type
            }
        }
        // …y mensajes en vivo: cada cambio de Firestore re-emite la lista.
        viewModelScope.launch {
            try {
                matchRepository.observeMessages(chatId).collect { lista ->
                    mensajes.value = lista
                    loading.value = false
                }
            } catch (e: Exception) {
                error.value = RepoError.UNKNOWN
                loading.value = false
            }
        }
    }

    /** Envía el texto escrito (se limpia en la pantalla si el envío fue OK). */
    fun enviar(texto: String) {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch {
            when (matchRepository.sendMessage(chatId, limpio)) {
                is RepoResult.Success -> Unit
                is RepoResult.Error -> error.value = RepoError.UNKNOWN
            }
        }
    }

    companion object {
        fun factory(container: AppContainer, chatId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(
                        chatId = chatId,
                        authRepository = container.authRepository,
                        matchRepository = container.matchRepository,
                    ) as T
                }
            }
    }
}

/** Instancia de la conversación (una por chatId; se recrea al cambiar de chat). */
@Composable
fun rememberChatViewModel(chatId: String): ChatViewModel {
    val context = LocalContext.current
    val owner = activityAncla(context)
    return viewModel(
        viewModelStoreOwner = owner,
        key = "gaply_chat_$chatId",
        factory = ChatViewModel.factory(context.appContainer(), chatId),
    )
}

/** ComponentActivity dueño del ViewModelStore (mismo patrón que el módulo de Actividades). */
private fun activityAncla(context: android.content.Context): ComponentActivity =
    generateSequence(context) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<ComponentActivity>()
        .firstOrNull()
        ?: error("No se encontró ComponentActivity en la cadena de contextos")
