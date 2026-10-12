package com.gaply.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.HuecoLibre
import com.gaply.app.domain.model.RepoError
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.ScheduleClass
import com.gaply.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ScheduleUiState(
    val cargando: Boolean = false,
    /** Clases del día visible solamente (no de toda la semana). */
    val clases: List<ScheduleClass> = emptyList(),
    /** Día que se está viendo: "Lunes". */
    val dia: String = "",
    /** Fecha del día visible: "5 de octubre". */
    val fecha: String = "",
    /** Mes abreviado para la etiqueta pequeña: "oct.". */
    val mesCorto: String = "",
    /** Huecos libres entre clases del día visible. */
    val huecos: List<HuecoLibre> = emptyList(),
    val error: String? = null,
)

/**
 * Módulo 2B · "Mi horario".
 *
 * Carga las clases del usuario, decide qué día mostrar, calcula los huecos
 * libres entre clases y expone las operaciones de guardado.
 */
class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState(cargando = true))
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    // La carga inicial la dispara LaunchedEffect(Unit) en ScheduleScreen: así la
    // lista se recarga también cada vez que se vuelve del formulario (clase
    // creada, editada o eliminada), no solo la primera vez.

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            when (val resultado = scheduleRepository.getClasses()) {
                is RepoResult.Success -> _uiState.value = construir(resultado.data)

                is RepoResult.Error -> _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = mensajeDe(resultado.type),
                )
            }
        }
    }

    fun eliminar(classId: String) {
        viewModelScope.launch {
            scheduleRepository.deleteClass(classId)
            cargar()
        }
    }

    /**
     * Muestra el día de hoy si tiene clases; si no, el primer día con clases;
     * si el horario está vacío, hoy mismo (o lunes si hoy es domingo).
     */
    private fun construir(clases: List<ScheduleClass>): ScheduleUiState {
        val hoy = LocalDate.now()
        val diaDeHoy = tituloDia(hoy)

        // El modelo solo programó clases de lunes a sábado: si hoy es domingo y
        // el horario está vacío, el día visible arranca en lunes.
        val diaVisible = diaDeHoy.takeIf { dia -> clases.any { it.day == dia } }
            ?: DIAS_ORDEN.firstOrNull { dia -> clases.any { it.day == dia } }
            ?: diaDeHoy.takeIf { it in DIAS_ORDEN }
            ?: DIAS_ORDEN.first()

        val clasesDelDia = clases.filter { it.day == diaVisible }
            .sortedBy { it.startMinutes }

        // Próxima fecha en la que cae ese día de la semana (hoy mismo si coincide).
        // Se compara por título en español: DayOfWeek.valueOf() espera nombres en
        // inglés (MONDAY, SUNDAY…) y tronaba con "DOMINGO"/"LUNES".
        val fechaDeHoy = (0..7)
            .map { hoy.plusDays(it.toLong()) }
            .firstOrNull { tituloDia(it) == diaVisible }
            ?: hoy

        val formatoLargo = DateTimeFormatter.ofPattern("d 'de' MMMM", LOCALE)
        val formatoCorto = DateTimeFormatter.ofPattern("MMM", LOCALE)

        return ScheduleUiState(
            cargando = false,
            clases = clasesDelDia,
            dia = diaVisible,
            fecha = formatoLargo.format(fechaDeHoy),
            mesCorto = formatoCorto.format(fechaDeHoy),
            huecos = calcularHuecos(clasesDelDia),
        )
    }

    /** Huecos de al menos una hora entre clases consecutivas del día. */
    private fun calcularHuecos(clasesDelDia: List<ScheduleClass>): List<HuecoLibre> {
        return clasesDelDia
            .filter { it.endMinutes > it.startMinutes }
            .sortedBy { it.startMinutes }
            .zipWithNext()
            .mapNotNull { (anterior, siguiente) ->
                val inicio = anterior.endMinutes
                val fin = siguiente.startMinutes
                if (fin - inicio >= HUECO_MINIMO_MINUTOS) {
                    HuecoLibre(inicioMinutos = inicio, finMinutos = fin)
                } else {
                    null
                }
            }
    }

    private fun mensajeDe(error: RepoError): String = when (error) {
        RepoError.CONNECTION -> "Sin conexión. Revisa tu internet e intenta de nuevo."
        RepoError.PERMISSION -> "Firestore no permitió leer tu horario (sesión o reglas)."
        else -> "No se pudo cargar el horario. Intenta de nuevo."
    }

    companion object {

        /** Días en los que el Figma permite programar clases. */
        val DIAS_ORDEN = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

        /** El hueco se muestra a partir de 60 minutos libres. */
        const val HUECO_MINIMO_MINUTOS = 60

        private val LOCALE = Locale("es", "CO")

        fun tituloDia(fecha: LocalDate): String = DateTimeFormatter
            .ofPattern("EEEE", LOCALE)
            .format(fecha)
            .replaceFirstChar { it.uppercase() }

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScheduleViewModel(scheduleRepository = container.scheduleRepository) as T
                }
            }
    }
}
