package com.gaply.app.ui.screens.matches

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gaply.app.di.AppContainer
import com.gaply.app.di.appContainer
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.FeedItem
import com.gaply.app.domain.model.SwipeAction
import com.gaply.app.domain.model.User
import com.gaply.app.ui.components.InterestPill
import com.gaply.app.ui.screens.activities.ActivityFormat
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.OutlineGray
import com.gaply.app.ui.theme.StarGold
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextPrimary
import com.gaply.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

/**
 * Pestaña Matches · mazo de cartas con personas y actividades de la comunidad.
 *
 * Cada gesto (✕ descartar / ★ me interesa / ♥ me gusta) lo registra el
 * ViewModel en `swipes`; si es recíproco con una persona nace el chat y se
 * abre el diálogo "¡Es un match!".
 */
@Composable
fun MatchesScreen(
    onOpenChat: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel = rememberMatchesViewModel()
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundApp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.loading -> CircularProgressIndicator(color = BrandGreen)

            state.error != null -> EstadoError(s = s, onRetry = viewModel::cargar)

            state.cartaActual == null -> EstadoVacio(s = s, mazoVacio = state.mazoVacio)

            else -> Mazo(
                carta = state.cartaActual!!,
                s = s,
                onSwipe = viewModel::onSwipe,
            )
        }
    }

    // Diálogo cuando el like fue recíproco.
    state.matchChatId?.let { chatId ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissMatch,
            title = { Text(text = "🎉 ${s.matchTitle}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onOpenMatchChat()
                        onOpenChat(chatId)
                    },
                ) {
                    Text(text = s.accept, color = BrandGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissMatch) {
                    Text(text = s.close, color = TextSecondary)
                }
            },
        )
    }
}

// ---------------------------------------------------------------------------
// Estados
// ---------------------------------------------------------------------------

@Composable
private fun EstadoError(s: AppStrings, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "📡", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = s.connectionErrorTitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = s.tapToRetry,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onRetry() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun EstadoVacio(s: AppStrings, mazoVacio: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "👥", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = s.tabMatches,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (mazoVacio) s.matchesNoMoreMsg else s.matchesEmptyMsg,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Mazo: carta de arriba + tres botones
// ---------------------------------------------------------------------------

@Composable
private fun Mazo(
    carta: FeedItem,
    s: AppStrings,
    onSwipe: (SwipeAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (carta) {
                is FeedItem.Person -> CartaPersona(carta.user, s)
                is FeedItem.Activity -> CartaActividad(carta.activity, s)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            // ✕ Descartar
            BotonSwipe(
                icon = Icons.Filled.Close,
                cd = s.swipePassCd,
                background = SurfaceWhite,
                border = OutlineGray,
                tint = TextPrimary,
                onClick = { onSwipe(SwipeAction.PASS) },
            )
            // ★ Me interesa
            BotonSwipe(
                icon = Icons.Filled.Star,
                cd = s.swipeStarCd,
                background = SurfaceWhite,
                border = StarGold,
                tint = StarGold,
                onClick = { onSwipe(SwipeAction.STAR) },
            )
            // ♥ Me gusta
            BotonSwipe(
                icon = Icons.Filled.Favorite,
                cd = s.swipeLikeCd,
                background = BrandGreen,
                border = null,
                tint = SurfaceWhite,
                onClick = { onSwipe(SwipeAction.LIKE) },
            )
        }
    }
}

@Composable
private fun BotonSwipe(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cd: String,
    background: Color,
    border: Color?,
    tint: Color,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(background)
            .then(
                if (border != null) {
                    Modifier.border(width = 2.dp, color = border, shape = CircleShape)
                } else {
                    Modifier
                },
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = cd,
            tint = tint,
            modifier = Modifier.size(28.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Cartas
// ---------------------------------------------------------------------------

/** Carta de persona: foto (o inicial), nombre + edad, universidad, bio e intereses. */
@Composable
private fun CartaPersona(user: User, s: AppStrings) {
    val nombre = listOf(user.firstName, user.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { user.username }
    val edad = edadDe(user.birthDate)
    val inicial = user.firstName.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString() ?: "?"
    val foto = user.profilePictureUrl

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Foto o respaldo con inicial
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(BrandGreen),
                contentAlignment = Alignment.Center,
            ) {
                if (foto.isNotBlank()) {
                    AsyncImage(
                        model = foto,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = inicial,
                        color = SurfaceWhite,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (edad != null) "$nombre, $edad" else nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (user.university.isNotBlank()) {
                    Text(
                        text = user.university,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (user.bio.isNotBlank()) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    user.interests.take(3).forEach { interes ->
                        InterestPill(
                            label = interes,
                            selected = true,
                            onClick = {},
                        )
                    }
                }
            }
        }
    }
}

/** Carta de actividad: foto o color, título, quién la creó, cuándo, dónde y categoría. */
@Composable
private fun CartaActividad(activity: ActivityItem, s: AppStrings) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Foto del lugar o color sólido de la categoría
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(ActivityFormat.colorSolido(activity.defaultColorHex)),
                contentAlignment = Alignment.Center,
            ) {
                if (!activity.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = activity.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(text = "🎯", fontSize = 64.sp)
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (activity.ownerName.isNotBlank()) {
                    Text(
                        text = s.createdBy(activity.ownerName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val fecha = millisDe(activity.date)?.let { ActivityFormat.fechaCorta(it, s.javaLocale) }
                val cuando = listOfNotNull(fecha, activity.timeRange.takeIf { it.isNotBlank() })
                    .joinToString(" · ")
                if (cuando.isNotBlank()) {
                    Text(
                        text = cuando,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                    )
                }
                if (activity.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activity.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    if (activity.category.isNotBlank()) {
                        InterestPill(
                            label = activity.category,
                            selected = true,
                            onClick = {},
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Edad a partir de "dd/MM/yyyy"; null si el dato está vacío o no se puede leer. */
private fun edadDe(birthDate: String): Int? {
    if (birthDate.isBlank()) return null
    return try {
        val fecha = LocalDate.parse(birthDate, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        Period.between(fecha, LocalDate.now()).years.takeIf { it in 1..120 }
    } catch (e: Exception) {
        null
    }
}

/** Fecha "yyyy-MM-dd" → epoch millis para los formateadores del proyecto. */
private fun millisDe(fecha: String): Long? {
    if (fecha.isBlank()) return null
    return try {
        LocalDate.parse(fecha).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (e: Exception) {
        null
    }
}

/**
 * Instancia del ViewModel de la pestaña Matches, anclada a la Activity
 * (mismo patrón que [com.gaply.app.ui.screens.activities.rememberActivityViewModel]).
 */
@Composable
private fun rememberMatchesViewModel(): MatchesViewModel {
    val context = LocalContext.current
    val owner = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<ComponentActivity>()
        .firstOrNull()
        ?: error("No se encontró ComponentActivity en la cadena de contextos")
    return viewModel(
        viewModelStoreOwner = owner,
        key = "gaply_matches",
        factory = MatchesViewModel.factory(context.appContainer()),
    )
}
