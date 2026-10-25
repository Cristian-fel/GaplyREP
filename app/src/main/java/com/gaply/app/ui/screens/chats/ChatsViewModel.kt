package com.gaply.app.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.ChatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estados de la lista de chats (Loading / Ready / Empty / Error). */
enum class ChatsPhase { LOADING, READY, EMPTY, ERROR }

data class ChatsUiState(
    val phase: ChatsPhase = ChatsPhase.LOADING,
    val chats: List<Chat> = emptyList(),
    val error: RepoError? = null,
)

/**
 * Módulo 5 · Chats · Lista.
 *
 * Escucha `chats` en tiempo real (los del usuario actual, más reciente
 * primero): cada snapshot repinta la lista, así el badge de no leídos, el
 * último mensaje y el orden se actualizan sin recargar nada a mano.
 */
class ChatsViewModel(
    private val chatsRepository: ChatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatsRepository.observeChats().collect { resultado ->
                when (resultado) {
                    is RepoResult.Success -> _uiState.value = ChatsUiState(
                        phase = if (resultado.data.isEmpty()) ChatsPhase.EMPTY else ChatsPhase.READY,
                        chats = resultado.data,
                        error = null,
                    )

                    is RepoResult.Error -> _uiState.value = _uiState.value.copy(
                        phase = ChatsPhase.ERROR,
                        error = resultado.type,
                    )
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatsViewModel(chatsRepository = container.chatsRepository) as T
                }
            }
    }
}
