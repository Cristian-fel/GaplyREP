package com.gaply.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
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

data class ScheduleEditUiState(
    val cargando: Boolean = false,
    val esNuevo: Boolean = true,
    val classId: String = "",
    val titulo: String = "",
    val dia: String = "Lunes",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val horaInicio: String = "08:00",
    val horaFin: String = "10:00",
    val nota: String = "",
    val color: String = ScheduleClass.COLOR_POR_DEFECTO,
    val error: ScheduleEditError? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val eliminado: Boolean = false,
)

enum class ScheduleEditError {
    CONNECTION,
    READ_PERMISSION,
    WRITE_PERMISSION,
    DELETE_PERMISSION,
    LOAD_FAILED,
    LOAD_GENERIC,
    SAVE_GENERIC,
    DELETE_GENERIC,
    TITLE_REQUIRED,
    END_BEFORE_START,
    CLASS_MISSING,
}

/**
 * Módulo 2B · formulario "Horario" (crear o editar una clase).
 *
 * Los selectores de día, fecha, hora y color viven en la pantalla; aquí se
 * guarda el estado y se valida antes de enviarlo al repositorio.
 */
class ScheduleEditViewModel(
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleEditUiState(cargando = true))
    val uiState: StateFlow<ScheduleEditUiState> = _uiState.asStateFlow()

    /** Carga la clase [classId] o prepara una nueva en [diaPorDefecto]. */
    fun cargar(classId: String, diaPorDefecto: String) {
        if (classId.isBlank()) {
            val hoy = LocalDate.now()
            _uiState.value = ScheduleEditUiState(
                cargando = false,
                esNuevo = true,
                dia = diaPorDefecto.ifBlank { "Lunes" },
                fechaInicio = formatearFecha(hoy),
                fechaFin = formatearFecha(hoy),
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            when (val resultado = scheduleRepository.getClasses()) {
                is RepoResult.Success -> {
                    val clase = resultado.data.firstOrNull { it.id == classId }
                    _uiState.value = if (clase == null) {
                        _uiState.value.copy(
                            cargando = false,
                            error = ScheduleEditError.CLASS_MISSING,
                        )
                    } else {
                        ScheduleEditUiState(
                            cargando = false,
                            esNuevo = false,
                            classId = clase.id,
                            titulo = clase.title,
                            dia = clase.day,
                            fechaInicio = clase.startDate,
                            fechaFin = clase.endDate,
                            horaInicio = clase.startHour,
                            horaFin = clase.endHour,
                            nota = clase.note,
                            color = clase.color,
                        )
                    }
                }

                is RepoResult.Error -> _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = mensajeDe(resultado.type),
                )
            }
        }
    }

    fun onTitulo(valor: String) {
        _uiState.value = _uiState.value.copy(titulo = valor, error = null)
    }

    fun onDia(valor: String) {
        _uiState.value = _uiState.value.copy(dia = valor, error = null)
    }

    fun onFechaInicio(valor: String) {
        _uiState.value = _uiState.value.copy(fechaInicio = valor, error = null)
    }

    fun onFechaFin(valor: String) {
        _uiState.value = _uiState.value.copy(fechaFin = valor, error = null)
    }

    fun onHoraInicio(valor: String) {
        _uiState.value = _uiState.value.copy(horaInicio = valor, error = null)
    }

    fun onHoraFin(valor: String) {
        _uiState.value = _uiState.value.copy(horaFin = valor, error = null)
    }

    fun onNota(valor: String) {
        _uiState.value = _uiState.value.copy(nota = valor, error = null)
    }

    fun onColor(valor: String) {
        _uiState.value = _uiState.value.copy(color = valor, error = null)
    }

    fun consumirError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Valida y guarda la clase; si sale bien, [ScheduleEditUiState.guardado] queda en true.
     *
     * El guard `guardando` evita el doble toque del ✓: sin él, dos llamadas
     * rápidas con `id = ""` generarían dos clases nuevas (duplicados).
     */
    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando || estado.guardado || estado.eliminado) return

        if (estado.titulo.isBlank()) {
            _uiState.value = estado.copy(error = ScheduleEditError.TITLE_REQUIRED)
            return
        }
        if (ScheduleClass.minutosDeHora(estado.horaFin) <= ScheduleClass.minutosDeHora(estado.horaInicio)) {
            _uiState.value = estado.copy(error = ScheduleEditError.END_BEFORE_START)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null)

            val clase = ScheduleClass(
                id = if (estado.esNuevo) "" else estado.classId,
                title = estado.titulo.trim(),
                day = estado.dia,
                startDate = estado.fechaInicio,
                endDate = estado.fechaFin,
                startHour = estado.horaInicio,
                endHour = estado.horaFin,
                note = estado.nota.trim(),
                color = estado.color,
            )

            when (val resultado = scheduleRepository.saveClass(clase)) {
                is RepoResult.Success -> _uiState.value = _uiState.value.copy(
                    guardando = false,
                    guardado = true,
                )

                is RepoResult.Error -> _uiState.value = _uiState.value.copy(
                    guardando = false,
                    error = mensajeGuardadoDe(resultado.type),
                )
            }
        }
    }

    /** Elimina la clase abierta; al terminar la pantalla cierra el formulario. */
    fun eliminar() {
        val estado = _uiState.value
        if (estado.classId.isBlank() || estado.eliminado) return

        viewModelScope.launch {
            when (val resultado = scheduleRepository.deleteClass(estado.classId)) {
                is RepoResult.Success -> _uiState.value = _uiState.value.copy(eliminado = true)

                is RepoResult.Error -> _uiState.value = _uiState.value.copy(
                    error = mensajeEliminadoDe(resultado.type),
                )
            }
        }
    }

    private fun mensajeDe(error: RepoError): ScheduleEditError = when (error) {
        RepoError.CONNECTION -> ScheduleEditError.CONNECTION
        RepoError.PERMISSION -> ScheduleEditError.READ_PERMISSION
        else -> ScheduleEditError.LOAD_GENERIC
    }

    private fun mensajeGuardadoDe(error: RepoError): ScheduleEditError = when (error) {
        RepoError.CONNECTION -> ScheduleEditError.CONNECTION
        RepoError.PERMISSION -> ScheduleEditError.WRITE_PERMISSION
        else -> ScheduleEditError.SAVE_GENERIC
    }

    private fun mensajeEliminadoDe(error: RepoError): ScheduleEditError = when (error) {
        RepoError.CONNECTION -> ScheduleEditError.CONNECTION
        RepoError.PERMISSION -> ScheduleEditError.DELETE_PERMISSION
        else -> ScheduleEditError.DELETE_GENERIC
    }

    companion object {

        /** "2026-09-11" -> "11/09/2026" */
        fun formatearFecha(fecha: LocalDate): String =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").format(fecha)

        /** "11/09/2026" -> LocalDate */
        fun parsearFecha(texto: String): LocalDate? {
            val partes = texto.trim().split("/")
            val dia = partes.getOrNull(0)?.toIntOrNull() ?: return null
            val mes = partes.getOrNull(1)?.toIntOrNull() ?: return null
            val anio = partes.getOrNull(2)?.toIntOrNull() ?: return null
            return runCatching { LocalDate.of(anio, mes, dia) }.getOrNull()
        }

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScheduleEditViewModel(scheduleRepository = container.scheduleRepository) as T
                }
            }
    }
}
