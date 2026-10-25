package com.gaply.app.ui.screens.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.domain.model.MatchAction
import com.gaply.app.domain.model.RepoError
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.mensajeDe
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.StarGold
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

/**
 * Módulo 5 · Pestaña "Matches" (embebida en la Home, sin ruta propia).
 *
 * Muestra el mazo del [MatchesViewModel] con sus estados Loading / Ready /
 * Empty / Error, los botones de respaldo del swipe y el diálogo de match
 * mutuo con el CTA que abre la conversación (navegación a ChatDetail).
 */
@Composable
fun MatchesScreen(
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: MatchesViewModel = viewModel(
        factory = MatchesViewModel.factory(context.appContainer()),
    )
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    // Carga inicial y recarga si cambia el idioma (las razones del agente se
    // redactan con los textos activos).
    LaunchedEffect(s) {
        viewModel.strings = s
        viewModel.cargar()
    }

    // Los botones actúan sobre la carta visible del mazo (misma acción que
    // el arrastre); el mazo la retira de inmediato.
    fun accionSwipe(action: MatchAction) {
        state.cartas.firstOrNull()?.let { viewModel.swipe(it, action) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundApp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = s.tabMatches,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = s.matchesUtilityExplanation,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 2,
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.phase == MatchesPhase.LOADING -> EstadoCargando(s)

            state.cartas.isEmpty() -> EstadoVacio(
                mensaje = when {
                    // Sin candidatos = "tus matches aparecerán aquí"; mazo
                    // recorrido = "no tenemos más sugerencias"; error = el
                    // motivo legible (sesión o reglas de Firestore).
                    state.phase == MatchesPhase.ERROR ->
                        mensajeDe(state.error ?: RepoError.UNKNOWN, s)

                    state.phase == MatchesPhase.EMPTY -> s.matchesEmptyMsg
                    else -> s.matchesNoMoreSuggestions
                },
            )

            else -> {
                SwipeDeck(
                    cartas = state.cartas,
                    onSwipe = viewModel::swipe,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.height(14.dp))
                BotonesSwipe(
                    puedeSuper = state.cartas.firstOrNull() is DeckCard.ProfileCard,
                    onPass = { accionSwipe(MatchAction.PASS) },
                    onSuper = { accionSwipe(MatchAction.SUPER) },
                    onLike = { accionSwipe(MatchAction.LIKE) },
                    s = s,
                )
            }
        }
    }

    state.error?.let { error ->
        AppDialog(
            style = if (error == RepoError.CONNECTION) DialogStyle.ERROR else DialogStyle.WARNING,
            title = if (error == RepoError.CONNECTION) s.connectionErrorTitle else s.upsTitle,
            message = mensajeDe(error, s),
            onDismiss = viewModel::limpiarError,
        )
    }

    state.matchDialog?.let { match ->
        AppDialog(
            style = DialogStyle.SUCCESS,
            title = s.matchTitle,
            message = s.matchMessage(match.name),
            confirmText = s.openChat,
            onConfirm = {
                viewModel.consumirMatch()
                onOpenChat(match.chatId)
            },
            onDismiss = viewModel::consumirMatch,
            icon = Icons.Filled.Favorite,
            iconTint = ErrorRed,
            iconBackgroundColor = Mint,
            borderColor = BrandGreen,
        )
    }

    // Swipe derecho a una actividad: ya estás dentro y su chat existe.
    state.activityJoined?.let { unida ->
        AppDialog(
            style = DialogStyle.SUCCESS,
            title = s.activityJoinedTitle,
            message = s.activityJoinedMsg(unida.title),
            confirmText = s.openChat,
            onConfirm = {
                viewModel.consumirActivityJoined()
                onOpenChat(unida.chatId)
            },
            onDismiss = viewModel::consumirActivityJoined,
            borderColor = BrandGreen,
        )
    }
}

@Composable
private fun EstadoCargando(s: AppStrings) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = BrandGreen)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = s.loadingData, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EstadoVacio(mensaje: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DeckEmptyIcon()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Tres botones como respaldo del arrastre: pasar, super me gusta (solo en
 * perfiles) y me gusta. Mismas acciones que los gestos del mazo.
 */
@Composable
private fun BotonesSwipe(
    puedeSuper: Boolean,
    onPass: () -> Unit,
    onSuper: () -> Unit,
    onLike: () -> Unit,
    s: AppStrings,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleAction(
            icon = Icons.Filled.Close,
            contentDescription = s.swipePass,
            container = SurfaceWhite,
            tint = ErrorRed,
            border = ErrorRed,
            size = 56.dp,
            onClick = onPass,
        )
        if (puedeSuper) {
            Spacer(modifier = Modifier.width(28.dp))
            CircleAction(
                icon = Icons.Filled.Star,
                contentDescription = s.superLikeCd,
                container = StarGold,
                tint = SurfaceWhite,
                border = null,
                size = 56.dp,
                onClick = onSuper,
            )
        }
        Spacer(modifier = Modifier.width(28.dp))
        CircleAction(
            icon = Icons.Filled.Favorite,
            contentDescription = s.likeCd,
            container = BrandGreen,
            tint = SurfaceWhite,
            border = null,
            size = 64.dp,
            onClick = onLike,
        )
    }
}

@Composable
private fun CircleAction(
    icon: ImageVector,
    contentDescription: String,
    container: Color,
    tint: Color,
    border: Color?,
    size: Dp,
    onClick: () -> Unit,
) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(container)
                .then(
                    if (border != null) {
                        Modifier.border(2.dp, border, CircleShape)
                    } else {
                        Modifier
                    },
                ),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size / 2f),
            )
        }
}

