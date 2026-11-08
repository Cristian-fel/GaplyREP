package com.gaply.app.ui.screens.radar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.ui.theme.BrandGreen
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Permisos que el Radar AR necesita: cámara + ubicación (fina o aproximada). */
private val RADAR_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

/** Cámara concedida y (ubicación fina o aproximada) concedida. */
private fun hasRadarPermissions(context: Context): Boolean {
    fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    return granted(Manifest.permission.CAMERA) &&
        (granted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            granted(Manifest.permission.ACCESS_COARSE_LOCATION))
}

/**
 * Radar AR · Brújula espacial hacia la actividad.
 *
 * Pide cámara y ubicación al entrar y muestra la cámara en vivo con
 * CameraX. La superposición [RadarOverlay] es interactiva:
 *
 *  - Tarjeta verde del destino dentro del campo de visión (±35°),
 *    moviéndose levemente con la diferencia angular.
 *  - Fuera del campo de visión la tarjeta se oculta y aparece una
 *    flecha grande animada al borde izquierdo o derecho que indica
 *    hacia qué lado girar el cuerpo.
 *  - Minimapa [MiniRadarCompass] (inferior derecha) y panel de
 *    diagnóstico [RadarDebugPanel] (superior derecha) en tiempo real.
 *
 * @param title Título de la actividad (título de la tarjeta).
 * @param destinationPlace Nombre del lugar destino (se resuelve con [LocationMapper]).
 * @param onBack Cierra el radar y vuelve al detalle.
 */
@Composable
fun RadarArScreen(
    title: String,
    destinationPlace: String,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    val context = LocalContext.current

    var permissionsGranted by remember { mutableStateOf(false) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        permissionsGranted = hasRadarPermissions(context)
    }

    // Solicitud dinámica de permisos al entrar a la pantalla.
    LaunchedEffect(Unit) {
        if (hasRadarPermissions(context)) {
            permissionsGranted = true
        } else if (!permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(RADAR_PERMISSIONS)
        }
    }

    val destination = remember(destinationPlace) { LocationMapper.resolve(destinationPlace) }
    val viewModel: RadarArViewModel = viewModel(
        factory = RadarArViewModel.factory(context.applicationContext as Application),
    )
    val radarState by viewModel.uiState.collectAsState()

    // Enciende brújula + GPS solo con permisos; los apaga al salir.
    DisposableEffect(permissionsGranted, destination) {
        if (permissionsGranted) viewModel.start(destination)
        onDispose { viewModel.stop() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (permissionsGranted) {
            CameraRadarPreview(modifier = Modifier.fillMaxSize())

            RadarOverlay(
                state = radarState,
                title = title.ifBlank { destinationPlace },
            )
        } else {
            PermissionNeededContent(
                message = s.radarPermissionsMsg,
                retryText = s.retry,
                onRetry = { permissionLauncher.launch(RADAR_PERMISSIONS) },
            )
        }

        // Botón superior izquierdo: siempre visible (ambos estados).
        RadarBackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

/** Cámara en vivo a pantalla completa (CameraX + PreviewView). */
@Composable
private fun CameraRadarPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = remember(context) { findLifecycleOwner(context) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    var boundProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    AndroidView(factory = { previewView }, modifier = modifier)

    if (lifecycleOwner != null) {
        LaunchedEffect(lifecycleOwner) {
            val provider = awaitCameraProvider(context)
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            try {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                )
                boundProvider = provider
            } catch (_: Exception) {
                // Sin cámara disponible: el radar sigue navegable.
            }
        }

        DisposableEffect(lifecycleOwner) {
            onDispose { boundProvider?.unbindAll() }
        }
    }
}

/** Suspensión sobre el ListenableFuture de ProcessCameraProvider (sin guava). */
private suspend fun awaitCameraProvider(context: Context): ProcessCameraProvider =
    suspendCancellableCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            { cont.resume(future.get()) },
            ContextCompat.getMainExecutor(context),
        )
    }

/** ComponentActivity de la cadena de contextos (patrón del proyecto). */
private fun findLifecycleOwner(context: Context): LifecycleOwner? =
    generateSequence(context) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<ComponentActivity>()
        .firstOrNull()

/**
 * Campo de visión del radar en grados: dentro de ±35° el usuario "mira
 * hacia" el destino y se muestra la tarjeta; fuera se muestra la flecha
 * de borde.
 */
private const val FIELD_OF_VIEW_DEG = 35f

/**
 * Interfaz AR superpuesta a la cámara:
 *
 *  - Destino dentro del campo de visión (±[FIELD_OF_VIEW_DEG]): tarjeta
 *    flotante centrada que se desplaza levemente con la diferencia angular.
 *  - Fuera del campo: la tarjeta se oculta ([AnimatedVisibility]) y una
 *    flecha grande animada pegada al borde ([EdgeDirectionArrow]) indica
 *    hacia qué lado girar.
 *  - Minimapa [MiniRadarCompass] (inferior derecha) y panel de
 *    diagnóstico [RadarDebugPanel] (superior derecha) siempre visibles.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun RadarOverlay(
    state: RadarUiState,
    title: String,
) {
    val s = LocalStrings.current
    var cardWidthPx by remember { mutableIntStateOf(0) }
    val marginPx = with(LocalDensity.current) { 24.dp.toPx() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxOffsetX = (
            constraints.maxWidth / 2f -
                cardWidthPx / 2f -
                marginPx
            ).coerceAtLeast(0f)
        val delta = state.angleDeltaDeg
        val inFov = abs(delta) <= FIELD_OF_VIEW_DEG
        // Dentro del campo la tarjeta se mueve suave (±50% de la amplitud).
        val offsetX = (delta / FIELD_OF_VIEW_DEG) * maxOffsetX * 0.5f
        val offsetY = -constraints.maxHeight * 0.15f

        // Tarjeta: solo cuando el usuario mira hacia el destino.
        AnimatedVisibility(
            visible = inFov,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .onGloballyPositioned { cardWidthPx = it.size.width },
            ) {
                RadarDestinationCard(
                    title = title,
                    distanceM = state.distanceM,
                    searchingText = s.radarSearchingGps,
                )
            }
        }

        // Flecha de borde: solo fuera del campo, hacia el lado del destino.
        AnimatedVisibility(
            visible = !inFov,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(
                if (delta > 0f) Alignment.CenterEnd else Alignment.CenterStart,
            ),
        ) {
            EdgeDirectionArrow(toRight = delta > 0f)
        }

        // Minimapa / brújula 2D (esquina inferior derecha).
        MiniRadarCompass(
            angleDeltaDeg = delta,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
        )

        // Diagnóstico en tiempo real (esquina superior derecha).
        RadarDebugPanel(
            state = state,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 12.dp),
        )
    }
}

/** Tarjeta verde flotante: ícono del lugar, título de la actividad y distancia. */
@Composable
private fun RadarDestinationCard(
    title: String,
    distanceM: Float?,
    searchingText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .widthIn(max = 270.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BrandGreen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = distanceM?.let { "${it.roundToInt()} m" } ?: searchingText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

/**
 * Flecha grande animada pegada al borde de la pantalla: indica hacia qué
 * lado girar el cuerpo para encontrar el destino dentro del campo de
 * visión ([toRight] = borde derecho).
 */
@Composable
private fun EdgeDirectionArrow(
    toRight: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "edgeArrow")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "pulseAlpha",
    )
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "pulseScale",
    )
    val bobMaxPx = with(LocalDensity.current) { 10.dp.toPx() }
    val bobPx by transition.animateFloat(
        initialValue = 0f,
        targetValue = bobMaxPx,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "bob",
    )

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .size(64.dp)
            .graphicsLayer {
                alpha = pulseAlpha
                scaleX = pulseScale
                scaleY = pulseScale
                translationY = bobPx
            }
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (toRight) {
                Icons.AutoMirrored.Filled.ArrowForward
            } else {
                Icons.AutoMirrored.Filled.ArrowBack
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp),
        )
    }
}

/**
 * Minimapa / brújula 2D estilo radar (esquina inferior derecha): círculo
 * oscuro semitransparente con el usuario fijo en el centro y el destino
 * orbitando según la diferencia exacta azimuth − bearing: 0° = arriba
 * (frente), +90° = derecha, 180° = abajo (detrás del usuario).
 */
@Composable
private fun MiniRadarCompass(
    angleDeltaDeg: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(96.dp)) {
        val radius = size.minDimension / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val deltaRad = Math.toRadians(angleDeltaDeg.toDouble()).toFloat()

        // Disco radar + aros exterior (verde) y de referencia interior.
        drawCircle(color = Color.Black.copy(alpha = 0.45f), radius = radius)
        drawCircle(
            color = BrandGreen,
            radius = radius,
            style = Stroke(width = 2.dp.toPx()),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = radius * 0.6f,
            style = Stroke(width = 1.dp.toPx()),
        )

        // Punto fijo del usuario en el centro.
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(cx, cy))

        // Destino orbitando por la diferencia exacta de ángulos.
        val orbit = radius * 0.62f
        val destCenter = Offset(
            x = cx + orbit * sin(deltaRad),
            y = cy - orbit * cos(deltaRad),
        )
        drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = destCenter)
        drawCircle(color = BrandGreen, radius = 3.5.dp.toPx(), center = destCenter)
    }
}

/**
 * Panel de diagnóstico semitransparente (esquina superior derecha):
 * distancia, coordenadas del destino y ángulo diff en tiempo real para
 * validar que los sensores calculan la rotación correctamente.
 */
@Composable
private fun RadarDebugPanel(
    state: RadarUiState,
    modifier: Modifier = Modifier,
) {
    val destino = state.destination
    val distancia = state.distanceM?.let { "${it.roundToInt()} metros" } ?: "—"
    val coordenadas = if (destino != null) {
        "Lat: ${fmtCoord(destino.latitude)}, Lng: ${fmtCoord(destino.longitude)} (del destino)"
    } else {
        "Lat: —, Lng: — (del destino)"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = "Distancia: $distancia",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp,
        )
        Text(
            text = coordenadas,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp,
        )
        Text(
            text = "Ángulo diff: ${fmtAngle(state.angleDeltaDeg)}°",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp,
        )
    }
}

/** Coordenada con 5 decimales en formato numérico estable, ej. "4.65810". */
private fun fmtCoord(value: Double): String = String.format(Locale.US, "%.5f", value)

/** Ángulo con 1 decimal en formato numérico estable, ej. "-12.4". */
private fun fmtAngle(value: Float): String = String.format(Locale.US, "%.1f", value)

/** Estado sin permisos: aviso + botón para reintentar la solicitud. */
@Composable
private fun PermissionNeededContent(
    message: String,
    retryText: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = BrandGreen,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGreen,
                contentColor = Color.White,
            ),
        ) {
            Text(text = retryText, fontWeight = FontWeight.Bold)
        }
    }
}

/** Botón circular de volver atrás (superior izquierdo). */
@Composable
private fun RadarBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(12.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = s.back,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}
