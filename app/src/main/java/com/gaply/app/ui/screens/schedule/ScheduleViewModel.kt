package com.gaply.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.StringsEs
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
import java.time.DayOfWeek
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
    /** true si hay clases en otros días (para orientar en el estado vacío). */
    val hayClasesOtrosDias: Boolean = false,
    val error: ScheduleError? = null,
)

enum class ScheduleError { CONNECTION, PERMISSION, LOAD_FAILED }

/**
 * Módulo 2B · "Mi horario".
 *
 * Carga las clases de la semana, permite moverse entre los días con las
 * flechas ‹ ›, calcula los huecos libres del día visible y expone las
 * operaciones de guardado.
 */
class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {

    var strings: AppStrings = StringsEs

    private val _uiState = MutableStateFlow(ScheduleUiState(cargando = true))
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    /** Semana completa: sirve para cambiar de día sin volver a consultar. */
    private var clasesSemana: List<ScheduleClass> = emptyList()

    // La carga inicial la dispara LaunchedEffect(Unit) en ScheduleScreen: así la
    // lista se recarga también cada vez que se vuelve del formulario (clase
    // creada, editada o eliminada), no solo la primera vez.

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            when (val resultado = scheduleRepository.getClasses()) {
                is RepoResult.Success -> {
                    clasesSemana = resultado.data
                    // Se conserva el día que se estaba mirando: regresar del
                    // formulario no debe mandar a otro día.
                    _uiState.value = construir(diaPreferido = _uiState.value.dia)
                }

                is RepoResult.Error -> _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = mensajeDe(resultado.type),
                )
            }
        }
    }

    /** Mueve el día visible con las flechas ‹ › (cicla de sábado a lunes). */
    fun cambiarDia(paso: Int) {
        val actual = DIAS_ORDEN.indexOf(_uiState.value.dia).coerceAtLeast(0)
        val nuevo = (actual + paso + DIAS_ORDEN.size) % DIAS_ORDEN.size
        _uiState.value = construir(diaPreferido = DIAS_ORDEN[nuevo])
    }

    fun eliminar(classId: String) {
        viewModelScope.launch {
            scheduleRepository.deleteClass(classId)
            cargar()
        }
    }

    /**
     * Arma el estado del día visible. Orden de decisión:
     * 1. el día elegido con las flechas (o el que ya se estaba viendo);
     * 2. si no, hoy si tiene clases;
     * 3. si no, el primer día con clases;
     * 4. si el horario está vacío, hoy mismo (o lunes si hoy es domingo).
     */
    private fun construir(diaPreferido: String): ScheduleUiState {
        val hoy = LocalDate.now()
        val diaDeHoy = tituloDia(hoy)

        val diaVisible = diaPreferido.takeIf { it in DIAS_ORDEN }
            ?: diaDeHoy.takeIf { dia -> clasesSemana.any { it.day == dia } }
            ?: DIAS_ORDEN.firstOrNull { dia -> clasesSemana.any { it.day == dia } }
            ?: diaDeHoy.takeIf { it in DIAS_ORDEN }
            ?: DIAS_ORDEN.first()

        val clasesDelDia = clasesSemana.filter { it.day == diaVisible }
            .sortedBy { it.startMinutes }

        // Fecha de ese día dentro de la semana en curso (lunes de esta semana).
        // Si hoy es domingo, la semana "actual" del modelo empieza mañana lunes.
        val inicioSemana = if (hoy.dayOfWeek == DayOfWeek.SUNDAY) {
            hoy.plusDays(1)
        } else {
            hoy.with(DayOfWeek.MONDAY)
        }
        val fechaDelDia = inicioSemana.plusDays(DIAS_ORDEN.indexOf(diaVisible).toLong())

        return ScheduleUiState(
            cargando = false,
            clases = clasesDelDia,
            dia = diaVisible,
            fecha = strings.formatMonthDay(fechaDelDia),
            mesCorto = strings.formatMonthAbbrev(fechaDelDia),
            huecos = calcularHuecos(clasesDelDia),
            hayClasesOtrosDias = clasesSemana.any { it.day != diaVisible },
        )
    }

    /** Huecos de al menos una hora entre clases consecutivas del día. */
    private fun calcularHuecos(clasesDelDia: List<ScheduleClass>): List<HuecoLibre> =
        HuecoLibre.deClases(clasesDelDia, HUECO_MINIMO_MINUTOS)

    private fun mensajeDe(error: RepoError): ScheduleError = when (error) {
        RepoError.CONNECTION -> ScheduleError.CONNECTION
        RepoError.PERMISSION -> ScheduleError.PERMISSION
        else -> ScheduleError.LOAD_FAILED
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
