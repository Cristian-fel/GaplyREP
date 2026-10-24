package com.gaply.app.ui.screens.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.agent.AgenteRecomendacion
import com.gaply.app.domain.agent.FranjaHoraria
import com.gaply.app.domain.agent.PerfilAgente
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.FeedItem
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.SwipeAction
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.ActivityRepository
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.MatchRepository
import com.gaply.app.domain.repository.ScheduleRepository
import com.gaply.app.ui.screens.activities.ActivityFormat
import com.gaply.app.ui.screens.schedule.ScheduleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatchesUiState(
    /** true mientras se carga el mazo (primera vez o reintento). */
    val loading: Boolean = true,
    /** Error de carga; null = todo bien. */
    val error: RepoError? = null,
    /** Cartas del mazo ya mezcladas (persona ↔ actividad). */
    val cartas: List<FeedItem> = emptyList(),
    /** Índice de la carta arriba del mazo. */
    val indice: Int = 0,
    /** chatId cuando nació un match; muestra el diálogo "¡Es un match!". */
    val matchChatId: String? = null,
) {
    /** Carta visible ahora (null = mazo vacío → estado "sin más sugerencias"). */
    val cartaActual: FeedItem? get() = cartas.getOrNull(indice)

    /** true cuando no quedan cartas por mostrar. */
    val mazoVacio: Boolean get() = cartas.isNotEmpty() && indice >= cartas.size
}

/**
 * ViewModel de la pestaña Matches: carga el mazo (personas + actividades),
 * registra cada gesto ✕/★/♥ y detecta el match mutuo (que abre el chat).
 *
 * El mazo **no va en orden alfabético**: el agente de utilidad de la app
 * prioriza las actividades (mayor utilidad arriba) y las personas se ordenan
 * por afinidad de intereses compartidos.
 */
class MatchesViewModel(
    private val authRepository: AuthRepository,
    private val matchRepository: MatchRepository,
    private val activityRepository: ActivityRepository,
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {

    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<RepoError?>(null)
    private val cartas = MutableStateFlow<List<FeedItem>>(emptyList())
    private val indice = MutableStateFlow(0)
    private val matchChatId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MatchesUiState> = combine(
        loading,
        error,
        cartas,
        indice,
        matchChatId,
    ) { cargando, err, mazo, pos, chatId ->
        MatchesUiState(
            loading = cargando,
            error = err,
            cartas = mazo,
            indice = pos,
            matchChatId = chatId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MatchesUiState(),
    )

    init {
        cargar()
    }

    /** Trae candidatos, los prioriza con el agente y mezcla persona ↔ actividad. */
    fun cargar() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            when (val resultado = matchRepository.getCandidates()) {
                is RepoResult.Success -> {
                    val yo = authRepository.currentUser.value
                    val huecos = huecosDeHoy()
                    cartas.value = mezclar(
                        personas = ordenarPorAfinidad(resultado.data.people, yo),
                        actividades = ordenarActividades(resultado.data.activities, yo, huecos),
                    )
                    indice.value = 0
                    loading.value = false
                }

                is RepoResult.Error -> {
                    error.value = resultado.type
                    loading.value = false
                }
            }
        }
    }

    /**
     * Acción sobre la carta de arriba: registra el swipe (y el "unirme" si es
     * ♥ en una actividad), avanza el mazo y abre el diálogo si hubo match.
     */
    fun onSwipe(accion: SwipeAction) {
        val carta = uiState.value.cartaActual ?: return
        viewModelScope.launch {
            val (targetId, esPersona) = when (carta) {
                is FeedItem.Person -> carta.user.userId to true
                is FeedItem.Activity -> carta.activity.id to false
            }

            val resultado = matchRepository.registerSwipe(targetId, accion, esPersona)

            // ♥ en actividad = unirse (mismo botón "Unirme" del detalle) y
            // entrar al chat del evento (grupo).
            if (carta is FeedItem.Activity && accion == SwipeAction.LIKE) {
                unirse(carta.activity)
                matchRepository.syncGroupChat(carta.activity.id, carta.activity.title, joined = true)
            }

            if (resultado is RepoResult.Success && resultado.data.isNotBlank()) {
                matchChatId.value = resultado.data
            } else if (resultado is RepoResult.Error) {
                error.value = resultado.type
            }

            indice.value += 1
        }
    }

    /** Cierra el diálogo "¡Es un match!". */
    fun onDismissMatch() {
        matchChatId.value = null
    }

    /** Abre el chat del match recién creado (cierra el diálogo). */
    fun onOpenMatchChat() {
        matchChatId.value = null
    }

    private suspend fun unirse(actividad: ActivityItem) {
        val nombre = authRepository.currentUser.value?.let { user ->
            listOf(user.firstName, user.lastName).filter { it.isNotBlank() }.joinToString(" ")
        }.orEmpty().ifBlank { "Alguien" }
        activityRepository.join(actividad.id, nombre)
    }

    // ---------------------------------------------------------------------
    // Priorización del mazo (IA)
    // ---------------------------------------------------------------------

    /**
     * Huecos libres de hoy (mismo cálculo que el banner del Home) convertidos
     * a las franjas que entiende el agente de utilidad.
     */
    private suspend fun huecosDeHoy(): List<FranjaHoraria> {
        val resultado = scheduleRepository.getClasses()
        if (resultado !is RepoResult.Success) return emptyList()
        val hoy = LocalDate.now()
        val clasesDeHoy = resultado.data.filter { it.day == ScheduleViewModel.tituloDia(hoy) }
        val dia = ActivityFormat.diaDe(hoy)
        return HuecoLibre.deClases(clasesDeHoy, ScheduleViewModel.HUECO_MINIMO_MINUTOS)
            .map { it.aFranjaHoraria(dia) }
    }

    /** Personas con las que más intereses comparto, primero (normalización del agente). */
    private fun ordenarPorAfinidad(personas: List<User>, yo: User?): List<User> {
        val mios = yo?.interests.orEmpty().map { normalizar(it) }.toSet()
        return personas.sortedByDescending { persona ->
            persona.interests.count { normalizar(it) in mios }
        }
    }

    /**
     * Actividades priorizadas por el agente de utilidad de la app: primero las
     * que recomienda (mayor utilidad), luego las descartadas y al final las no
     * evaluables. **Nada se elimina**: solo cambia el orden. Sin huecos libres
     * se ordena por afinidad de intereses.
     */
    private fun ordenarActividades(
        actividades: List<ActivityItem>,
        yo: User?,
        huecos: List<FranjaHoraria>,
    ): List<ActivityItem> {
        if (actividades.isEmpty()) return actividades
        val perfil = PerfilAgente(
            intereses = yo?.interests.orEmpty(),
            huecos = huecos,
        )
        val mios = perfil.intereses.map { normalizar(it) }.toSet()

        if (huecos.isEmpty()) {
            return actividades.sortedByDescending {
                if (normalizar(it.category) in mios) 1 else 0
            }
        }

        val candidatas = actividades.mapNotNull { it.aCandidataDelAgente() }
        if (candidatas.isEmpty()) return actividades

        val resultado = AgenteRecomendacion().recomendar(perfil, candidatas)
        val orden = mutableMapOf<String, Int>()
        resultado.recomendaciones.forEachIndexed { i, r -> orden[r.actividad.id] = i }
        val base = resultado.recomendaciones.size
        resultado.descartadas.forEachIndexed { i, d -> orden[d.actividad.id] = base + i }

        return actividades.sortedWith(
            compareBy<ActivityItem> { orden[it.id] ?: Int.MAX_VALUE }
                .thenByDescending { if (normalizar(it.category) in mios) 1 else 0 },
        )
    }

    /** Convierte una actividad del feed en el candidato que evalúa el agente. */
    private fun ActivityItem.aCandidataDelAgente(): com.gaply.app.domain.agent.Actividad? {
        val fecha = date.toLongOrNull()
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
            ?: runCatching { LocalDate.parse(date) }.getOrNull()
            ?: return null
        val franja = ActivityFormat.franjaDe(this, ActivityFormat.diaDe(fecha)) ?: return null
        return com.gaply.app.domain.agent.Actividad(
            id = id,
            nombre = title,
            intereses = listOf(category).filter { it.isNotBlank() },
            franja = franja,
            descripcion = description,
        )
    }

    /** Minúsculas sin acentos: la misma normalización que usa el agente. */
    private fun normalizar(texto: String): String =
        java.text.Normalizer.normalize(texto.trim().lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

    /**
     * Mezcla persona–actividad alternando, para que el mazo se vea variado
     * (igual que la idea de diseño: gente y actividades en el mismo deck).
     */
    private fun mezclar(
        personas: List<User>,
        actividades: List<ActivityItem>,
    ): List<FeedItem> {
        val personasCarta = personas.map { FeedItem.Person(it) }
        val actividadesCarta = actividades.map { FeedItem.Activity(it) }
        val resultado = mutableListOf<FeedItem>()
        var p = 0
        var a = 0
        var alternar = true
        while (p < personasCarta.size || a < actividadesCarta.size) {
            if (alternar && p < personasCarta.size) {
                resultado += personasCarta[p++]
            } else if (a < actividadesCarta.size) {
                resultado += actividadesCarta[a++]
            } else if (p < personasCarta.size) {
                resultado += personasCarta[p++]
            }
            alternar = !alternar
        }
        return resultado
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MatchesViewModel(
                        authRepository = container.authRepository,
                        matchRepository = container.matchRepository,
                        activityRepository = container.activityRepository,
                        scheduleRepository = container.scheduleRepository,
                    ) as T
                }
            }
    }
}
