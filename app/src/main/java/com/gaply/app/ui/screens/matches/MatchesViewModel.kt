package com.gaply.app.ui.screens.matches

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.StringsEs
import com.gaply.app.data.mapper.aMatchProfile
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.agent.AgenteMatches
import com.gaply.app.domain.agent.CandidatoMatch
import com.gaply.app.domain.agent.PerfilAgente
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.MatchAction
import com.gaply.app.domain.model.MatchEntry
import com.gaply.app.domain.model.MatchKind
import com.gaply.app.domain.model.MatchProfile
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.huecosDelHorario
import com.gaply.app.domain.repository.ActivityRepository
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.ChatsRepository
import com.gaply.app.domain.repository.MatchesRepository
import com.gaply.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/** Estados del mazo de Matches (Loading / Ready / Empty / Error). */
enum class MatchesPhase { LOADING, READY, EMPTY, ERROR }

/** Carta del mazo: un perfil del mazo o una actividad rápida ("Flash Activity"). */
sealed interface DeckCard {
    val id: String

    data class ProfileCard(val profile: MatchProfile) : DeckCard {
        override val id: String get() = "profile_${profile.userId}"
    }

    data class ActivityCard(val activity: ActivityItem) : DeckCard {
        override val id: String get() = "activity_${activity.id}"
    }
}

/** Datos del diálogo "¡Es un match!" (el CTA navega a [chatId]). */
data class MatchDialogData(
    val name: String,
    val photoUrl: String,
    val chatId: String,
)

/** Datos del diálogo tras unirse a una actividad (el CTA abre su chat). */
data class ActivityJoinedData(
    val title: String,
    val chatId: String,
)

data class MatchesUiState(
    val phase: MatchesPhase = MatchesPhase.LOADING,
    val cartas: List<DeckCard> = emptyList(),
    /** true cuando el mazo tuvo cartas y ya no queda ninguna. */
    val agotado: Boolean = false,
    val error: RepoError? = null,
    val matchDialog: MatchDialogData? = null,
    val activityJoined: ActivityJoinedData? = null,
    /**
     * Perfiles que me dieron like y **yo aún no respondí**: encabezan el mazo
     * (darles like de vuelta cierra el match al instante).
     */
    val pendientes: List<MatchProfile> = emptyList(),
)

/**
 * Módulo 5 · Matches.
 *
 * Carga el mazo: primero mis swipes (`swipes` con `fromUid == yo`, las
 * exclusiones), después los
 * candidatos de `users` y sus actividades; cruza los perfiles con
 * [AgenteMatches] (percepción -> decisión -> acción) y mezcla el top con las
 * actividades rápidas.
 *
 * Cada swipe persiste la entrada y, si es un LIKE a un perfil, comprueba el
 * match mutuo leyendo la entrada del otro usuario. Si coincide, se crea el
 * chat directo con id determinístico y se expone el diálogo de match.
 */
class MatchesViewModel(
    private val authRepository: AuthRepository,
    private val matchesRepository: MatchesRepository,
    private val chatsRepository: ChatsRepository,
    private val scheduleRepository: ScheduleRepository,
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    /** Textos activos: los usa el agente para redactar las razones. */
    var strings: AppStrings = StringsEs

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = MatchesPhase.LOADING, error = null) }

            val miUid = authRepository.currentUser.value?.userId
            if (miUid.isNullOrBlank()) {
                Log.w(TAG, "No hay usuario autenticado: el mazo no se puede cargar.")
                _uiState.update { it.copy(phase = MatchesPhase.ERROR, error = RepoError.PERMISSION) }
                return@launch
            }

            // Cada fuente es independiente: si Firestore niega leer los
            // perfiles, las actividades del mazo siguen mostrándose (y al
            // revés). El error se reporta aparte para no dejar la pestaña muda.
            val fallos = mutableListOf<RepoError>()

            val entradas = when (val resultado = matchesRepository.getMyEntries()) {
                is RepoResult.Success -> resultado.data
                is RepoResult.Error -> {
                    registrarFallo(fallos, "mis swipes", resultado.type)
                    emptyList()
                }
            }

            val candidatos = when (val resultado = matchesRepository.getDeckCandidates(CANDIDATOS)) {
                is RepoResult.Success -> resultado.data
                is RepoResult.Error -> {
                    registrarFallo(fallos, "los perfiles candidatos", resultado.type)
                    emptyList()
                }
            }

            // Likes recibidos que aún no respondí: van primero del mazo.
            val pendientesUids = when (val resultado = matchesRepository.getPendingLikeUids()) {
                is RepoResult.Success -> resultado.data.take(LIMIT_PENDIENTES)
                is RepoResult.Error -> {
                    registrarFallo(fallos, "los likes recibidos", resultado.type)
                    emptyList()
                }
            }
            val pendientes = resolverPendientes(pendientesUids, candidatos)

            val actividades = when (val resultado = activityRepository.getActivities()) {
                is RepoResult.Success -> resultado.data
                is RepoResult.Error -> {
                    registrarFallo(fallos, "las actividades", resultado.type)
                    emptyList()
                }
            }

            val perfiles = perfilesSinSwipar(miUid, entradas, candidatos)
            val sugeridos = evaluar(perfiles)
            val flash = actividadesSinSwipar(entradas, actividades)

            val idsPendientes = pendientes.map { "profile_${it.userId}" }.toSet()
            val resto = intercalar(
                sugeridos.map { DeckCard.ProfileCard(it) },
                flash.map { DeckCard.ActivityCard(it) },
            )
            // Encabezan el mazo: son los que faltan para cerrar el match.
            val cartas = pendientes.map { DeckCard.ProfileCard(it) } +
                resto.filterNot { it.id in idsPendientes }

            _uiState.update {
                it.copy(
                    phase = when {
                        cartas.isEmpty() && fallos.isNotEmpty() -> MatchesPhase.ERROR
                        cartas.isEmpty() -> MatchesPhase.EMPTY
                        else -> MatchesPhase.READY
                    },
                    cartas = cartas,
                    pendientes = pendientes,
                    // Al cargar nunca está agotado: EMPTY = "no hay candidatos".
                    // "Se acabaron las sugerencias" aparece al swipear la última.
                    agotado = false,
                    error = fallos.firstOrNull(),
                )
            }
        }
    }

    /**
     * Registra el swipe de la carta visible y avanza el mazo.
     * Devuelve de inmediato el estado con la carta fuera para que la animación
     * nunca espere a Firestore.
     */
    fun swipe(card: DeckCard, action: MatchAction) {
        _uiState.update { estado ->
            val perfilSwipeado = (card as? DeckCard.ProfileCard)?.profile?.userId
            estado.copy(
                cartas = estado.cartas.filterNot { it.id == card.id },
                // Ya respondí: deja de contar como pendiente y de encabezar.
                pendientes = if (perfilSwipeado == null) {
                    estado.pendientes
                } else {
                    estado.pendientes.filterNot { it.userId == perfilSwipeado }
                },
            )
        }
        marcarAgotado()

        viewModelScope.launch {
            when (card) {
                is DeckCard.ProfileCard -> swiparPerfil(card.profile, action)
                is DeckCard.ActivityCard -> swiparActividad(card.activity, action)
            }
        }
    }

    private suspend fun swiparPerfil(profile: MatchProfile, action: MatchAction) {
        val guardado = matchesRepository.saveEntry(
            MatchEntry(
                targetId = profile.userId,
                kind = MatchKind.PROFILE,
                action = action,
            ),
        )
        if (guardado is RepoResult.Error) {
            fallo(guardado.type)
            return
        }
        if (action == MatchAction.PASS) return

        // LIKE o SUPER: si el otro ya me gustó, ¡hay match! y toca abrir chat.
        when (val mutuo = matchesRepository.hasMutualLike(profile.userId)) {
            is RepoResult.Success -> if (mutuo.data) abrirChatDirecto(profile)
            is RepoResult.Error -> fallo(mutuo.type)
        }
    }

    private suspend fun swiparActividad(activity: ActivityItem, action: MatchAction) {
        if (action == MatchAction.PASS) {
            // El "no me interesa" se persiste para no volver a mostrarla.
            when (val guardado = matchesRepository.saveEntry(
                MatchEntry(
                    targetId = activity.id,
                    kind = MatchKind.ACTIVITY,
                    action = MatchAction.PASS,
                ),
            )) {
                is RepoResult.Error -> fallo(guardado.type)
                is RepoResult.Success -> Unit
            }
            return
        }

        // Derecha = unirse (misma mecánica attendeeIds/attendees del módulo
        // de Actividades); NO crea match entre personas. En una actividad propia
        // no hay nada que unirse: solo se garantiza su chat de grupo.
        val uid = authRepository.currentUser.value?.userId.orEmpty()
        val nombre = nombreVisible()
        val resultado: RepoResult<String> = if (activity.isCreatedByMe) {
            chatsRepository.ensureGroupChat(activity)
        } else {
            when (val unirse = activityRepository.join(activity.id, nombre)) {
                is RepoResult.Error -> unirse
                is RepoResult.Success -> {
                    // Entra al chat y publica el aviso "X se unió" para los
                    // que ya estaban dentro (mismo flujo que desde el detalle).
                    chatsRepository.joinGroupChat(
                        activity.copy(
                            attendeeIds = (activity.attendeeIds + uid).distinct(),
                            attendees = (activity.attendees + nombre).distinct(),
                            isJoined = true,
                        ),
                        memberName = nombre,
                    )
                }
            }
        }

        when (resultado) {
            is RepoResult.Success -> {
                fallo(null)
                // Ya es tuya y su chat existe: se ofrece entrar (el usuario
                // decide si sigue swipando o abre la conversación).
                _uiState.update {
                    it.copy(
                        activityJoined = ActivityJoinedData(
                            title = activity.title,
                            chatId = resultado.data,
                        ),
                    )
                }
            }

            is RepoResult.Error -> fallo(resultado.type)
        }
    }

    private suspend fun abrirChatDirecto(profile: MatchProfile) {
        when (
            val resultado = chatsRepository.ensureDirectChat(
                otherUid = profile.userId,
                otherName = profile.displayName,
                otherPhotoUrl = profile.profilePictureUrl,
            )
        ) {
            is RepoResult.Success -> _uiState.update {
                it.copy(
                    matchDialog = MatchDialogData(
                        name = profile.displayName,
                        photoUrl = profile.profilePictureUrl,
                        chatId = resultado.data,
                    ),
                )
            }

            is RepoResult.Error -> fallo(resultado.type)
        }
    }

    fun consumirMatch() {
        _uiState.update { it.copy(matchDialog = null) }
    }

    /**
     * Botón "N personas te dieron like": vuelve a poner esos perfiles al frente
     * del mazo (darles like de vuelta cierra el match y abre el chat).
     */
    fun priorizarPendientes() {
        _uiState.update { estado ->
            if (estado.pendientes.isEmpty()) return@update estado
            val ids = estado.pendientes.map { "profile_${it.userId}" }.toSet()
            estado.copy(
                cartas = estado.pendientes.map { DeckCard.ProfileCard(it) } +
                    estado.cartas.filterNot { it.id in ids },
            )
        }
    }

    fun consumirActivityJoined() {
        _uiState.update { it.copy(activityJoined = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    fun recargar() = cargar()

    // ------------------------------------------------------------------
    // Carga del mazo
    // ------------------------------------------------------------------

    private fun perfilesSinSwipar(
        miUid: String,
        entradas: List<MatchEntry>,
        candidatos: List<MatchProfile>,
    ): List<MatchProfile> {
        val yaValorados = entradas
            .filter { it.kind == MatchKind.PROFILE }
            .map { it.targetId }
            .toSet()
        return candidatos.filter { it.userId.isNotBlank() && it.userId != miUid && it.userId !in yaValorados }
    }

    /**
     * Cartas de actividad rápida: **las futuras** (de hoy en adelante),
     * incluidas las mías, ordenadas por fecha más cercana. Si no hay ninguna
     * futura se cae a todas por fecha de publicación, para que el mazo no
     * quede vacío sin motivo.
     */
    private fun actividadesSinSwipar(
        entradas: List<MatchEntry>,
        actividades: List<ActivityItem>,
    ): List<ActivityItem> {
        val descartadas = entradas
            .filter { it.kind == MatchKind.ACTIVITY }
            .map { it.targetId }
            .toSet()
        val disponibles = actividades.filter { it.id.isNotBlank() && it.id !in descartadas }
        if (disponibles.isEmpty()) return emptyList()

        val futuras = disponibles.filter { esFutura(it) }
        return if (futuras.isNotEmpty()) {
            futuras.sortedBy { fechaDe(it) ?: Long.MAX_VALUE }.take(ACTIVIDADES_MAX)
        } else {
            disponibles.sortedByDescending { it.createdAt }.take(ACTIVIDADES_MAX)
        }
    }

    /** true si la actividad es de hoy en adelante (sin fecha legible también cuenta). */
    private fun esFutura(activity: ActivityItem): Boolean =
        fechaDe(activity)?.let { it >= inicioDeHoy() } ?: true

    /** `date` se guarda como millis en texto o como ISO "yyyy-MM-dd". */
    private fun fechaDe(activity: ActivityItem): Long? =
        activity.date.toLongOrNull()
            ?: runCatching {
                LocalDate.parse(activity.date)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()

    private fun inicioDeHoy(): Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    /** Deja constancia en Logcat de qué fuente falló (reglas, sesión o red). */
    private fun registrarFallo(fallos: MutableList<RepoError>, origen: String, tipo: RepoError) {
        Log.w(TAG, "Matches: Firestore no devolvió $origen ($tipo). ¿Reglas publicadas?")
        fallos += tipo
    }

    /**
     * Perfiles de quienes me dieron like: primero se resuelven con los
     * candidatos ya cargados (sin llamadas extra); si alguno quedó fuera del
     * límite del deck se pide su perfil individual.
     */
    private suspend fun resolverPendientes(
        uids: List<String>,
        candidatos: List<MatchProfile>,
    ): List<MatchProfile> {
        if (uids.isEmpty()) return emptyList()
        val porUid = candidatos.associateBy { it.userId }
        val perfiles = mutableListOf<MatchProfile>()
        val faltantes = mutableListOf<String>()
        uids.forEach { uid ->
            val perfil = porUid[uid]
            if (perfil != null) {
                perfiles.add(perfil)
            } else {
                faltantes.add(uid)
            }
        }
        faltantes.forEach { uid ->
            when (val resultado = matchesRepository.getUserProfile(uid)) {
                // Sin horario no se descarta: puntúa por intereses.
                is RepoResult.Success -> {
                    val perfil = resultado.data
                    if (perfil != null) perfiles.add(perfil.aMatchProfile(emptyList()))
                }

                // Un perfil no encontrado no tumba el mazo.
                is RepoResult.Error -> Unit
            }
        }
        return perfiles
    }

    /** Percepción -> decisión -> acción sobre los perfiles candidatos. */
    private suspend fun evaluar(perfiles: List<MatchProfile>): List<MatchProfile> {
        if (perfiles.isEmpty()) return emptyList()

        val misHuecos = when (val clases = scheduleRepository.getClasses()) {
            is RepoResult.Success ->
                huecosDelHorario(clases.data, HuecoLibre.MINIMO_MINUTOS)

            is RepoResult.Error -> emptyList()
        }
        val miPerfil = PerfilAgente(
            intereses = authRepository.currentUser.value?.interests.orEmpty(),
            huecos = misHuecos,
        )
        val candidatos = perfiles.map { perfil ->
            CandidatoMatch(
                userId = perfil.userId,
                nombre = perfil.displayName,
                intereses = perfil.interests,
                huecos = perfil.freeSlots,
            )
        }
        val porUid = perfiles.associateBy { it.userId }
        return AgenteMatches(strings).recomendar(miPerfil, candidatos).sugerencias
            .mapNotNull { puntuacion ->
                porUid[puntuacion.candidato.userId]?.copy(
                    commonFreeSlots = puntuacion.huecosEnComun,
                    utilityScore = puntuacion.puntuacion,
                    reasons = puntuacion.razones,
                )
            }
    }

    /** Perfiles y actividades alternados: el mazo nunca empieza todo con lo mismo. */
    private fun intercalar(perfiles: List<DeckCard>, actividades: List<DeckCard>): List<DeckCard> {
        val mezcla = mutableListOf<DeckCard>()
        val hasta = maxOf(perfiles.size, actividades.size)
        for (indice in 0 until hasta) {
            perfiles.getOrNull(indice)?.let { mezcla += it }
            actividades.getOrNull(indice)?.let { mezcla += it }
        }
        return mezcla
    }

    private fun marcarAgotado() {
        _uiState.update { estado ->
            estado.copy(agotado = estado.cartas.isEmpty())
        }
    }

    private fun fallo(error: RepoError?) {
        // Deja constancia en Logcat de qué operación del swipe se cayó
        // (permisos/reglas, sesión o red) antes de mostrar el diálogo.
        if (error != null) Log.w(TAG, "Matches: acción del mazo falló ($error)")
        _uiState.update { it.copy(error = error) }
    }

    private fun nombreVisible(): String {
        val user = authRepository.currentUser.value
        return user?.firstName?.ifBlank { null }
            ?: user?.username?.ifBlank { null }
            ?: strings.profileFallbackUsername
    }

    companion object {
        private const val TAG = "Gaply"

        /** Perfiles que se piden a Firestore en cada corrida del mazo. */
        private const val CANDIDATOS = 40

        /** Tope de "likes recibidos" que encabezan el mazo en cada carga. */
        private const val LIMIT_PENDIENTES = 10

        /** Actividades rápidas visibles a la vez. */
        private const val ACTIVIDADES_MAX = 8

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MatchesViewModel(
                        authRepository = container.authRepository,
                        matchesRepository = container.matchesRepository,
                        chatsRepository = container.chatsRepository,
                        scheduleRepository = container.scheduleRepository,
                        activityRepository = container.activityRepository,
                    ) as T
                }
            }
    }
}
