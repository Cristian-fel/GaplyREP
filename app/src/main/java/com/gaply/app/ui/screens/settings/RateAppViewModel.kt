package com.gaply.app.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.AppRating
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.repository.RatingsRepository
import kotlinx.coroutines.launch

/**
 * Módulo 2 · Calificar la app.
 * Carga la última calificación enviada (para rellenar la pantalla) y la
 * persiste en [RatingsRepository] al pulsar "Enviar".
 */
class RateAppViewModel(
    private val ratingsRepository: RatingsRepository,
) : ViewModel() {

    var rating by mutableIntStateOf(0)
        private set

    var comment by mutableStateOf("")
        private set

    /** true mientras se guarda la calificación en segundo plano. */
    var loading by mutableStateOf(false)
        private set

    /** true = se muestra el modal "¡Gracias por tu opinión!". */
    var sent by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val canSubmit: Boolean
        get() = rating > 0 && !loading

    init {
        cargarUltima()
    }

    fun onRatingChange(value: Int) {
        rating = value
    }

    fun onCommentChange(value: String) {
        comment = value
    }

    fun submit() {
        if (loading || rating <= 0) return

        errorMessage = null
        loading = true
        viewModelScope.launch {
            val result = ratingsRepository.saveRating(
                AppRating(stars = rating, comment = comment.trim()),
            )
            when (result) {
                is RepoResult.Success -> sent = true
                is RepoResult.Error -> errorMessage = result.type.toMessage()
            }
            loading = false
        }
    }

    fun consumeSent() {
        sent = false
    }

    fun consumeError() {
        errorMessage = null
    }

    private fun cargarUltima() {
        viewModelScope.launch {
            val result = ratingsRepository.getRating()
            if (result is RepoResult.Success) {
                result.data?.let { ultima ->
                    rating = ultima.stars
                    comment = ultima.comment
                }
            }
        }
    }

    private fun RepoError.toMessage(): String = when (this) {
        RepoError.PERMISSION -> "No hay una sesión activa. Inicia sesión para calificar."
        RepoError.CONNECTION -> "Sin conexión. Revisa tu internet e intenta de nuevo."
        else -> "No se pudo guardar tu calificación. Inténtalo de nuevo."
    }

    companion object {

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RateAppViewModel(
                        ratingsRepository = container.ratingsRepository,
                    ) as T
                }
            }
    }
}
