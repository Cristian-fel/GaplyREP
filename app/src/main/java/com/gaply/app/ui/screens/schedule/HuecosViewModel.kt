package com.gaply.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.agent.Actividad
import com.gaply.app.domain.agent.AgenteRecomendacion
import com.gaply.app.domain.agent.DiaSemana
import com.gaply.app.domain.agent.FranjaHoraria
import com.gaply.app.domain.agent.PerfilAgente
import com.gaply.app.domain.agent.ResultadoAgente
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HuecosUiState(
    val interesesSeleccionados: List<String> = emptyList(),
    val dia: DiaSemana = DiaSemana.MARTES,
    val horaInicio: String = "10:00",
    val horaFin: String = "12:00",
    val resultado: ResultadoAgente? = null,
    val aviso: String? = null,
)

/**
 * Módulo 2 · "Mis huecos".
 *
 * Recibe los datos de entrada (intereses + franja libre), los ejecuta con el
 * agente de utilidad y muestra el resultado en vivo: al cambiar cualquier dato
 * cambian las recomendaciones.
 *
 * @param diaInicial, @param inicioInicial, @param finInicial vienen desde el
 * hueco elegido en "Mi horario"; si están vacíos se usan los valores por defecto.
 */
class HuecosViewModel(
    private val authRepository: AuthRepository,
    diaInicial: String = "",
    inicioInicial: String = "",
    finInicial: String = "",
) : ViewModel() {

    private val agente = AgenteRecomendacion()

    private val _uiState = MutableStateFlow(
        HuecosUiState(
            // Prefiltra con los intereses que el usuario ya eligió en el registro.
            interesesSeleccionados = authRepository.currentUser.value?.interests
                .orEmpty()
                .take(MAX_INTERESES),
            dia = DiaSemana.values().firstOrNull { it.titulo == diaInicial } ?: DiaSemana.MARTES,
            horaInicio = inicioInicial.ifBlank { "10:00" },
            horaFin = finInicial.ifBlank { "12:00" },
        ),
    )
    val uiState: StateFlow<HuecosUiState> = _uiState.asStateFlow()

    init {
        recalcular()
    }

    fun alternarInteres(etiqueta: String) {
        val actual = _uiState.value.interesesSeleccionados
        val nuevo = when {
            etiqueta in actual -> actual - etiqueta
            actual.size >= MAX_INTERESES -> {
                _uiState.value = _uiState.value.copy(
                    aviso = "Puedes elegir hasta $MAX_INTERESES intereses.",
                )
                return
            }

            else -> actual + etiqueta
        }
        _uiState.value = _uiState.value.copy(interesesSeleccionados = nuevo, aviso = null)
        recalcular()
    }

    fun seleccionarDia(dia: DiaSemana) {
        _uiState.value = _uiState.value.copy(dia = dia)
        recalcular()
    }

    fun seleccionarInicio(hora: String) {
        _uiState.value = _uiState.value.copy(horaInicio = hora)
        recalcular()
    }

    fun seleccionarFin(hora: String) {
        _uiState.value = _uiState.value.copy(horaFin = hora)
        recalcular()
    }

    fun consumirAviso() {
        _uiState.value = _uiState.value.copy(aviso = null)
    }

    /** Vuelve a correr el agente con los datos actuales de entrada. */
    private fun recalcular() {
        val estado = _uiState.value
        val inicio = FranjaHoraria.aMinutos(estado.horaInicio)
        val fin = FranjaHoraria.aMinutos(estado.horaFin)

        if (inicio >= fin) {
            _uiState.value = estado.copy(
                resultado = null,
                aviso = "La hora final debe ser posterior a la inicial.",
            )
            return
        }

        val hueco = FranjaHoraria(estado.dia, inicio, fin)
        val perfil = PerfilAgente(
            intereses = estado.interesesSeleccionados,
            huecos = listOf(hueco),
        )

        _uiState.value = estado.copy(
            resultado = agente.recomendar(perfil = perfil, candidatas = CATALOGO_DEMO),
            aviso = null,
        )
    }

    companion object {
        const val MAX_INTERESES = 3

        fun factory(
            container: AppContainer,
            diaInicial: String = "",
            inicioInicial: String = "",
            finInicial: String = "",
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HuecosViewModel(
                        authRepository = container.authRepository,
                        diaInicial = diaInicial,
                        inicioInicial = inicioInicial,
                        finInicial = finInicial,
                    ) as T
                }
            }
    }
}

/**
 * Catálogo de demostración: son las actividades candidatas que el agente evalúa.
 * En la fase siguiente se reemplazará por la colección `activities` de Firestore.
 */
private val CATALOGO_DEMO = listOf(
    Actividad(
        id = "futbol",
        nombre = "Partido de fútbol",
        intereses = listOf("Deportes"),
        franja = FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "12:00"),
        descripcion = "Cancha del campus.",
    ),
    Actividad(
        id = "repaso",
        nombre = "Café y repaso",
        intereses = listOf("Estudio"),
        franja = FranjaHoraria.desde(DiaSemana.MARTES, "10:00", "11:00"),
        descripcion = "Repaso grupal en la cafetería.",
    ),
    Actividad(
        id = "mesa",
        nombre = "Torneo de juegos de mesa",
        intereses = listOf("Juegos de mesa"),
        franja = FranjaHoraria.desde(DiaSemana.MARTES, "10:30", "12:00"),
        descripcion = "Sala de actividades.",
    ),
    Actividad(
        id = "musica",
        nombre = "Sesión de música",
        intereses = listOf("Música"),
        franja = FranjaHoraria.desde(DiaSemana.MARTES, "11:00", "13:00"),
        descripcion = "Auditorio.",
    ),
    Actividad(
        id = "baile",
        nombre = "Clase de baile",
        intereses = listOf("Baile"),
        franja = FranjaHoraria.desde(DiaSemana.MARTES, "13:00", "14:00"),
        descripcion = "Piso multiusos.",
    ),
    Actividad(
        id = "artes",
        nombre = "Taller de artes",
        intereses = listOf("Artes"),
        franja = FranjaHoraria.desde(DiaSemana.MARTES, "16:00", "18:00"),
        descripcion = "Aula de artes.",
    ),
    Actividad(
        id = "cine",
        nombre = "Cine en el campus",
        intereses = listOf("Películas"),
        franja = FranjaHoraria.desde(DiaSemana.MARTES, "19:00", "21:00"),
        descripcion = "Auditorio.",
    ),
    Actividad(
        id = "evento",
        nombre = "Evento cultural",
        intereses = listOf("Eventos"),
        franja = FranjaHoraria.desde(DiaSemana.MIERCOLES, "10:00", "12:00"),
        descripcion = "Plaza principal.",
    ),
)
