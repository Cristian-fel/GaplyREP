package com.gaply.app.ui.screens.matches

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.domain.model.MatchAction
import com.gaply.app.ui.components.InterestPill
import com.gaply.app.ui.screens.activities.ActivityImage
import com.gaply.app.ui.screens.activities.CategoryPill
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.BrandGreenDark
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.StarGold
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Módulo 5 · Mazo de swipe de Matches.
 *
 * Pinta la carta visible (interactiva) y la siguiente por detrás con un
 * escalado suave. Al arrastrar aparecen los indicadores PASS / LIKE / SUPER y,
 * al soltar, la carta se va del mazo si superó el umbral o vuelve a su sitio
 * con una animación. Los botones de la pantalla ofrecen la misma acción sin
 * arrastrar (respaldo accesible).
 */
@Composable
internal fun SwipeDeck(
    cartas: List<DeckCard>,
    onSwipe: (DeckCard, MatchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        cartas.getOrNull(1)?.let { siguiente ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 0.95f
                        scaleY = 0.95f
                        translationY = 18.dp.toPx()
                    },
            ) {
                CartaContenido(siguiente)
            }
        }

        cartas.getOrNull(0)?.let { visible ->
            CartaDeslizable(
                card = visible,
                onSwipe = onSwipe,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Carta superior: acumula el arrastre, decide la acción y anima el retorno. */
@Composable
private fun CartaDeslizable(
    card: DeckCard,
    onSwipe: (DeckCard, MatchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alcance = rememberCoroutineScope()
    var offsetX by remember(card.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(card.id) { mutableFloatStateOf(0f) }
    var ancho by remember(card.id) { mutableIntStateOf(1) }

    fun volverAlCentro() {
        alcance.launch {
            launch {
                animate(offsetX, 0f, animationSpec = spring()) { valor, _ -> offsetX = valor }
            }
            launch {
                animate(offsetY, 0f, animationSpec = spring()) { valor, _ -> offsetY = valor }
            }
        }
    }

    fun soltar() {
        val limite = ancho * UMBRAL_RELATIVO
        when {
            offsetX > limite -> onSwipe(card, MatchAction.LIKE)
            offsetX < -limite -> onSwipe(card, MatchAction.PASS)
            offsetY < -limite && card is DeckCard.ProfileCard ->
                onSwipe(card, MatchAction.SUPER)

            else -> volverAlCentro()
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { ancho = it.width }
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                rotationZ = if (ancho > 0) (offsetX / ancho) * 14f else 0f
            }
            .pointerInput(card.id) {
                detectDragGestures(
                    onDragEnd = ::soltar,
                    onDragCancel = ::volverAlCentro,
                    onDrag = { cambio, cantidad ->
                        cambio.consume()
                        offsetX += cantidad.x
                        offsetY += cantidad.y
                    },
                )
            },
    ) {
        CartaContenido(card)
        IndicadoresSwipe(
            card = card,
            offsetX = offsetX,
            offsetY = offsetY,
            umbralPx = ancho * UMBRAL_RELATIVO,
        )
    }
}

/** Etiquetas que aparecen encima de la carta mientras se arrastra. */
@Composable
private fun IndicadoresSwipe(
    card: DeckCard,
    offsetX: Float,
    offsetY: Float,
    umbralPx: Float,
) {
    if (umbralPx <= 0f) return
    val s = LocalStrings.current
    val alfaIzquierda = (-offsetX / umbralPx).coerceIn(0f, 1f)
    val alfaDerecha = (offsetX / umbralPx).coerceIn(0f, 1f)
    val alfaArriba = (-offsetY / umbralPx).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize().padding(18.dp)) {
        if (alfaIzquierda > 0f) {
            EtiquetaSwipe(
                texto = s.swipePass,
                color = ErrorRed,
                alfa = alfaIzquierda,
                rotacion = -14f,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
        if (alfaDerecha > 0f) {
            EtiquetaSwipe(
                texto = s.swipeLike,
                color = BrandGreen,
                alfa = alfaDerecha,
                rotacion = 14f,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
        if (alfaArriba > 0f && card is DeckCard.ProfileCard) {
            EtiquetaSwipe(
                texto = s.swipeSuper,
                color = StarGold,
                alfa = alfaArriba,
                rotacion = 0f,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun EtiquetaSwipe(
    texto: String,
    color: Color,
    alfa: Float,
    rotacion: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alfa; rotationZ = rotacion }
            .clip(RoundedCornerShape(10.dp))
            .border(3.dp, color, RoundedCornerShape(10.dp))
            .background(SurfaceWhite.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = texto,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
        )
    }
}

// ------------------------------------------------------------------
// Contenido de cada tipo de carta
// ------------------------------------------------------------------

@Composable
internal fun CartaContenido(card: DeckCard, modifier: Modifier = Modifier) {
    when (card) {
        is DeckCard.ProfileCard -> PerfilCarta(card, modifier)
        is DeckCard.ActivityCard -> FlashActivityCarta(card, modifier)
    }
}

/** Tarjeta de perfil: foto, nombre + edad, universidad, intereses y motivos. */
@Composable
private fun PerfilCarta(card: DeckCard.ProfileCard, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val profile = card.profile

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (profile.profilePictureUrl.isNotBlank()) {
                    AsyncImage(
                        model = profile.profilePictureUrl,
                        contentDescription = profile.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(BrandGreen, BrandGreenDark),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = profile.displayName.take(1).uppercase(),
                            color = SurfaceWhite,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                            ),
                        )
                        .padding(16.dp),
                ) {
                    Column {
                        Text(
                            text = profile.displayName + profile.age?.let { ", ${s.ageYears(it)}" }.orEmpty(),
                            color = SurfaceWhite,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (profile.university.isNotBlank()) {
                            Text(
                                text = profile.university,
                                color = SurfaceWhite.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                profile.utilityScore?.let { puntaje ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Mint)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "$puntaje%",
                            color = BrandGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (profile.bio.isNotBlank()) {
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (profile.interests.isNotEmpty()) {
                    InterestRow(profile.interests)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profile.commonMinutes > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BrandGreen)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = s.commonFreeDuration(profile.commonMinutes),
                                color = SurfaceWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }

                profile.reasons.firstOrNull()?.let { motivo ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = motivo,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Tarjeta de actividad rápida: título, creador, horario, lugar y categoría. */
@Composable
private fun FlashActivityCarta(card: DeckCard.ActivityCard, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val activity = card.activity

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                ActivityImage(
                    imageUri = activity.imageUri,
                    defaultColorHex = activity.defaultColorHex,
                    contentDescription = activity.title,
                    modifier = Modifier.fillMaxSize(),
                )
                CategoryPill(
                    text = activity.category,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            ),
                        )
                        .padding(16.dp),
                ) {
                    Text(
                        text = activity.title,
                        color = SurfaceWhite,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${s.organizerLabel}: ${activity.ownerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activity.timeRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activity.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (activity.isJoined) s.alreadyJoined else s.joinActivity,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen,
                )
            }
        }
    }
}

/** Fila de intereses de la tarjeta (solo lectura: el swipe no los modifica). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestRow(interests: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        interests.forEach { interes ->
            InterestPill(
                label = interes,
                selected = true,
                onClick = {},
                modifier = Modifier.height(32.dp),
            )
        }
    }
}

/** Icono decorativo del estado vacío del mazo. */
@Composable
internal fun DeckEmptyIcon() {
    Icon(
        imageVector = Icons.Filled.AccountBalance,
        contentDescription = null,
        tint = Sage,
        modifier = Modifier.size(56.dp),
    )
}

private const val UMBRAL_RELATIVO = 0.3f
