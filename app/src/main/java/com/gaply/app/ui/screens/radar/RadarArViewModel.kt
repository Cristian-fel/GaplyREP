package com.gaply.app.ui.screens.radar

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import android.view.Surface
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

/** Estado observable del Radar AR (brújula, GPS y rumbo al destino). */
data class RadarUiState(
    /** Azimuth de la cámara: grados de brújula (0 = norte) hacia donde mira el teléfono. */
    val azimuthDeg: Float = 0f,
    /** Última ubicación GPS del usuario, o null si aún no hay señal. */
    val userLocation: Location? = null,
    /** Coordenada destino resuelta con [LocationMapper]. */
    val destination: Location? = null,
    /** Distancia en metros hasta el destino (null sin GPS). */
    val distanceM: Float? = null,
    /** Rumbo (bearing) desde el usuario hasta el destino, en grados. */
    val bearingDeg: Float? = null,
    /** Diferencia angular bearing − azimuth, normalizada a [−180, 180]. */
    val angleDeltaDeg: Float = 0f,
    /** true si la última fix GPS tiene menos de 8 s (señal viva). */
    val signalAlive: Boolean = false,
    /**
     * Precisión del sensor de brújula (`SENSOR_STATUS_*`): 0/1 = sin
     * calibrar (hay que hacer un "8" con el celular), −1 = sin dato aún.
     */
    val compassAccuracy: Int = -1,
)

/**
 * Radar AR · Lógica de sensores y GPS.
 *
 * - `TYPE_ROTATION_VECTOR` (o acelerómetro + magnetómetro como respaldo)
 *   calcula el azimuth de la cámara.
 * - `FusedLocationProviderClient` obtiene la ubicación GPS del usuario.
 * - Con ambas coordenadas deriva distancia, rumbo al destino y la
 *   diferencia angular que usa la tarjeta para deslizarse en pantalla.
 */
class RadarArViewModel(application: Application) :
    AndroidViewModel(application),
    SensorEventListener {

    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private var destination: Location? = null

    private var rotationSensor: Sensor? = null
    private var accelSensor: Sensor? = null
    private var magSensor: Sensor? = null
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var accelValues: FloatArray? = null
    private var magValues: FloatArray? = null
    private var lastPublishedAzimuth = -1f
    private var lastFixAtMs = 0L
    private var tickerJob: kotlinx.coroutines.Job? = null
    private val remappedMatrix = FloatArray(9)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let(::onUserLocation)
        }
    }

    /** Arranca brújula y GPS apuntando a [destination]. Idempotente. */
    fun start(destination: Location) {
        this.destination = destination
        _uiState.update { it.copy(destination = destination) }
        registerSensors()
        requestLocation()
        // Reloj de 1 s: apaga la "señal viva" si el GPS deja de llegar
        // (si no, la distancia quedaría congelada mintiendo "llegaste").
        if (tickerJob == null) {
            tickerJob = viewModelScope.launch {
                while (true) {
                    kotlinx.coroutines.delay(1_000)
                    _uiState.update { state ->
                        val vivo = lastFixAtMs > 0L &&
                            System.currentTimeMillis() - lastFixAtMs < 8_000L
                        if (state.signalAlive == vivo) state
                        else state.copy(signalAlive = vivo)
                    }
                }
            }
        }
    }

    /** Detiene sensores y actualizaciones de ubicación. */
    fun stop() {
        sensorManager.unregisterListener(this)
        tickerJob?.cancel()
        tickerJob = null
        runCatching { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    // ---- Sensores (azimuth de la cámara) ----

    private fun registerSensors() {
        sensorManager.unregisterListener(this)
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            // Respaldo: magnetómetro + acelerómetro.
            accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            magSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                publishAzimuth()
            }

            Sensor.TYPE_ACCELEROMETER -> {
                accelValues = event.values.clone()
                computeRotationFromAccelMag()
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                magValues = event.values.clone()
                computeRotationFromAccelMag()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Brújula sin calibrar (0/1) → la UI muestra el aviso del "8".
        _uiState.update { it.copy(compassAccuracy = accuracy) }
    }

    private fun computeRotationFromAccelMag() {
        val accel = accelValues ?: return
        val mag = magValues ?: return
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, mag)) publishAzimuth()
    }

    private fun publishAzimuth() {
        // Remapea el sistema de ejes según la rotación de la pantalla:
        // el azimuth debe apuntar al "arriba" visible en vertical o
        // horizontal (si no, en landscape queda desfasado 90°).
        val rotation = displayRotation()
        val (axisX, axisY) = when (rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        val remapped = SensorManager.remapCoordinateSystem(
            rotationMatrix, axisX, axisY, remappedMatrix,
        )
        SensorManager.getOrientation(if (remapped) remappedMatrix else rotationMatrix, orientation)
        var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        azimuth = (azimuth + 360f) % 360f
        // Publica solo cuando el valor cambió (menos recomposiciones).
        if (lastPublishedAzimuth >= 0f && abs(azimuth - lastPublishedAzimuth) < 0.5f) return
        lastPublishedAzimuth = azimuth
        _uiState.update { it.copy(azimuthDeg = azimuth).withDerived() }
    }

    /** Rotación actual de la pantalla (ROTATION_0/90/180/270). */
    @Suppress("DEPRECATION")
    private fun displayRotation(): Int =
        (getApplication<Application>().getSystemService(Context.WINDOW_SERVICE)
            as WindowManager).defaultDisplay.rotation

    // ---- GPS ----

    @SuppressLint("MissingPermission") // Los permisos se piden en RadarArScreen antes de start().
    private fun requestLocation() {
        viewModelScope.launch {
            // 1) Última ubicación conocida: aparece al instante (puede ser vieja).
            try {
                fusedLocationClient.lastLocation.await()?.let(::onUserLocation)
            } catch (_: SecurityException) {
                // Sin permiso de ubicación: la tarjeta queda en "buscando…".
            }
            // 2) Ubicación actual con espera corta (≤8 s): cubre el caso de
            //    que no haya última ubicación, sin frenar el paso anterior.
            val actual = withTimeoutOrNull(8_000) {
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token,
                    ).await()
                } catch (_: SecurityException) {
                    null
                }
            }
            actual?.let(::onUserLocation)
        }
        // 1 s y sin distancia mínima: quien está parado sigue recibiendo
        // fixes. Con 2 m de umbral la "señal viva" moría a los 8 s y la
        // tarjeta volvía a "Buscando…" aunque el GPS estaba bien.
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
            .setMinUpdateDistanceMeters(0f)
            .build()
        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper(),
            )
        } catch (_: SecurityException) {
            // Igual que arriba: la UI sigue funcionando sin rumbo calculado.
        }
    }

    private fun onUserLocation(location: Location) {
        lastFixAtMs = System.currentTimeMillis()
        _uiState.update { state ->
            state.copy(
                userLocation = location,
                destination = destination,
                signalAlive = true,
            ).withDerived()
        }
    }

    // ---- Cálculo de rumbo ----

    /** Recalcula distancia, bearing y diferencia angular si hay usuario + destino. */
    private fun RadarUiState.withDerived(): RadarUiState {
        val dest = destination ?: return this
        val user = userLocation ?: return this
        val distance = user.distanceTo(dest)
        val bearing = user.bearingTo(dest)
        return copy(
            distanceM = distance,
            bearingDeg = bearing,
            angleDeltaDeg = normalizeDeg(bearing - azimuthDeg),
        )
    }

    /** Diferencia angular en [−180, 180] (positiva = destino a la derecha). */
    private fun normalizeDeg(deg: Float): Float {
        var d = deg
        while (d > 180f) d -= 360f
        while (d < -180f) d += 360f
        return d
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RadarArViewModel(application) as T
            }
    }
}
