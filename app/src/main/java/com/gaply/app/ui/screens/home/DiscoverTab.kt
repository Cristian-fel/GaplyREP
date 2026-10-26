package com.gaply.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.domain.model.Chat
import com.gaply.app.ui.screens.activities.ActivityImage
import com.gaply.app.ui.screens.activities.InitialAvatar
import com.gaply.app.ui.screens.schedule.ScheduleViewModel
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary
import java.time.LocalDate

/**
 * Pestaña "Descubrir" de la Home.
 *
 * Orden pensado por prioridad para el estudiante:
 *  1. Yo hoy: saludo + campana y el banner del hueco (ya clicable).
 *  2. Para mi hueco: actividades reales que caben + acceso al listado completo.
 *  3. El campus: lugares del campus (filtran el listado por categoría).
 *  4. Social: mis matches, o una tarjeta que lleve a descubrir personas.
 *  5. Crear: publicar actividad / ver las mías, en fila compacta.
 *  6. Agenda cultural: contenido externo, al final.
 */
@Composable
internal fun DiscoverTab(
    state: HomeUiState,
    viewModel: HomeViewModel,
    sugeridas: List<ActivityItem>,
    s: AppStrings = LocalStrings.current,
    onOpenActivities: () -> Unit = {},
    onOpenActivitiesByCategory: (String) -> Unit = {},
    onCreateActivity: () -> Unit = {},
    onOpenMyActivities: () -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onOpenActivity: (String) -> Unit = {},
    onOpenHuecos: (dia: String, inicio: String, fin: String) -> Unit = { _, _, _ -> },
    onOpenSchedule: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val matches by viewModel.matchesBar.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // 1 · Cabecera de bienvenida
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = BrandGreen, shape = CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val avatarUrl = state.user?.profilePictureUrl.orEmpty()
                if (avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = s.profilePhotoCd,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = s.tabProfile,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = state.user?.firstName?.takeIf { it.isNotBlank() } ?: s.welcomeUser,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                ),
                color = Color.Black,
                modifier = Modifier.weight(1f),
            )
            // Campana con contador: mensajes sin leer + matches sin abrir.
            // Al tocarla se abre la pestaña Chats (índice 2).
            IconButton(onClick = { viewModel.onSelectTab(2) }) {
                BadgedBox(
                    badge = {
                        if (matches.badge > 0) {
                            Badge {
                                Text(
                                    text = if (matches.badge > 99) "99+" else matches.badge.toString(),
                                )
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = s.notificationsCd,
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2 · Banner del hueco de hoy. Clicable: con hueco abre "Mis huecos"
        // (agente) y sin horario lleva a "Mi horario" para añadir clases.
        BannerDeHueco(
            state = state,
            s = s,
            onOpenHuecos = onOpenHuecos,
            onOpenSchedule = onOpenSchedule,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3 · Para tu hueco: actividades reales que caben hoy + listado completo.
        if (sugeridas.isNotEmpty()) {
            SectionTitle(text = s.activitiesSectionTitle)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp),
            ) {
                items(sugeridas, key = { it.id }) { actividad ->
                    SugeridaCard(
                        activity = actividad,
                        onClick = { onOpenActivity(actividad.id) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4 · Lugares del campus: abren el listado filtrado por su categoría.
        SectionTitle(text = s.suggestionsToday)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp),
        ) {
            items(suggestedPlaces) { place ->
                PlaceCard(
                    place = place,
                    title = placeTitle(place.key, s),
                    onClick = { onOpenActivitiesByCategory(categoriaDe(place.key)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5 · Social: matches reales, o tarjeta que lleva a descubrir personas.
        MisMatchesSection(
            matches = matches,
            onOpenChat = onOpenChat,
            onVerPersonas = { viewModel.onSelectTab(1) },
            s = s,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 6 · Crear actividad / ver las mías: dos tarjetas compactas en fila.
        CompactCreateRow(
            onCreateActivity = onCreateActivity,
            onOpenMyActivities = onOpenMyActivities,
            s = s,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 7 · Agenda cultural con link al sitio general (lo último: es externo).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(text = s.culturalAgenda)
            Row(
                modifier = Modifier.clickable { uriHandler.openUri(URL_AGENDA_GENERAL) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = s.visitWebsite,
                    color = BrandGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = s.visitWebsite,
                    tint = BrandGreen,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mismas tarjetas 150 x 150 que los lugares: la fila visual y las
        // medidas quedan iguales en todo el Descubrir.
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp),
        ) {
            items(culturalEvents, key = { it.title }) { event ->
                EventCard(event = event, onOpenUrl = { url -> uriHandler.openUri(url) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Al final del todo (bajo la agenda cultural), con el MISMO tamaño
        // que el banner del hueco: misma forma de píldora y mismos 12 dp.
        BotonMisActividades(onClick = onOpenActivities, s = s)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * CTA "Ver mis actividades actuales". Reproduce la estructura del banner del
 * hueco (píldora verde, padding 12 vertical) para que midan exactamente lo
 * mismo; el contenido va centrado.
 */
@Composable
private fun BotonMisActividades(onClick: () -> Unit, s: AppStrings) {
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = BrandGreen),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Event,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            // Misma tipografía que el banner del hueco (14 sp / SemiBold):
            // los dos se ven igual dentro de su píldora verde.
            Text(
                text = s.joinedActivitiesButton,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

/**
 * Banner del hueco libre de hoy. Tocado navega: con hueco a "Mis huecos"
 * (agente de utilidad con la franja precargada); sin horario a "Mi horario".
 * Con horario pero sin hueco hoy no hay acción, así que no se pulsa.
 */
@Composable
private fun BannerDeHueco(
    state: HomeUiState,
    s: AppStrings,
    onOpenHuecos: (dia: String, inicio: String, fin: String) -> Unit,
    onOpenSchedule: () -> Unit,
) {
    val hueco = state.huecoDeHoy
    val accionable = hueco != null || !state.tieneHorario

    Card(
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = BrandGreen),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = accionable) {
                if (hueco != null) {
                    onOpenHuecos(
                        ScheduleViewModel.tituloDia(LocalDate.now()),
                        hueco.inicioHora,
                        hueco.finHora,
                    )
                } else {
                    onOpenSchedule()
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            // Texto centrado en el ancho de la píldora (el padding es
            // simétrico para que el centro no se desplace por los iconos).
            Text(
                text = when (val huecoActual = state.huecoDeHoy) {
                    null -> {
                        if (!state.tieneHorario) {
                            s.addClassesToSeeSlots
                        } else {
                            s.noFreeSlotsToday
                        }
                    }

                    else -> s.youHaveFreeSlot(
                        "${s.freeDuration(huecoActual.duracionMinutos)} " +
                            s.formatTimeRange(huecoActual.inicioHora, huecoActual.finHora),
                    )
                },
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
            )
            if (accionable) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(20.dp),
                )
            }
        }
    }
}

/** Título de sección en negrita (Descubrir). */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        ),
        color = Color.Black,
    )
}

/**
 * Sección "Matches" del Descubrir: los chats directos (cada uno es una
 * persona con la que ya hay match). Sin matches, tarjeta que invita a
 * descubrir personas en la pestaña Matches.
 */
@Composable
private fun MisMatchesSection(
    matches: MatchesBarUiState,
    onOpenChat: (String) -> Unit,
    onVerPersonas: () -> Unit,
    s: AppStrings,
) {
    SectionTitle(text = s.tabMatches)
    Spacer(modifier = Modifier.height(12.dp))

    if (matches.directos.isEmpty()) {
        MatchesCtaCard(s = s, onClick = onVerPersonas)
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(matches.directos, key = { it.id }) { chat ->
                MatchAvatarItem(chat = chat, onClick = { onOpenChat(chat.id) })
            }
        }
    }
}

/** Tarjeta de invitación cuando todavía no hay matches. */
@Composable
private fun MatchesCtaCard(s: AppStrings, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BrandGreen, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(BrandGreen, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = s.tabMatches,
                    tint = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.tabMatches,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BrandGreen,
                )
                Text(
                    text = s.discoverMatchesCta,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Avatar con el nombre debajo y punto rojo cuando hay algo sin ver. */
@Composable
private fun MatchAvatarItem(chat: Chat, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
    ) {
        Box {
            InitialAvatar(
                initial = chat.title.take(1),
                photoUrl = chat.photoUrl,
                sizeDp = 60,
            )
            if (chat.unreadCount > 0 || chat.esMatchNuevo) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                        .background(ErrorRed, CircleShape)
                        .border(2.dp, SurfaceWhite, CircleShape),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = chat.title,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Tarjeta horizontal de una actividad que cabe en el hueco de hoy. */
@Composable
private fun SugeridaCard(activity: ActivityItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(240.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActivityImage(
                imageUri = activity.imageUri,
                defaultColorHex = activity.defaultColorHex,
                contentDescription = activity.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.timeRange,
                    fontSize = 12.sp,
                    color = BrandGreen,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activity.location,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Fila compacta: publicar actividad | ver las mías. Las dos tarjetas miden
 * exactamente lo mismo (`IntrinsicSize.Min` en la fila), mismo radio y mismo
 * estilo — solo cambia el icono.
 */
@Composable
private fun CompactCreateRow(
    onCreateActivity: () -> Unit,
    onOpenMyActivities: () -> Unit,
    s: AppStrings,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AccionCard(
            icon = Icons.Filled.Add,
            iconCd = s.addCd,
            title = s.addYourOwnActivity,
            subtitle = s.inviteOthers,
            onClick = onCreateActivity,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        AccionCard(
            icon = Icons.Filled.Favorite,
            iconCd = s.myActivitiesCardTitle,
            title = s.myActivitiesCardTitle,
            subtitle = s.myActivitiesCardSubtitle,
            onClick = onOpenMyActivities,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

/** Tarjeta de acción del Descubrir (contenido centrado, altura igual a la de su pareja). */
@Composable
private fun AccionCard(
    icon: ImageVector,
    iconCd: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Mint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconCd,
                    tint = BrandGreen,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BrandGreen,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Tarjeta "foto" del Descubrir: MISMO tamaño y mismo radio para los lugares
 * del campus y para la agenda cultural (150 x 150, degradado inferior y
 * título). Así todas las filas horizontales quedan alineadas entre sí.
 */
@Composable
private fun FotoCard(
    painter: Painter,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .size(width = 150.dp, height = 150.dp)
            .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painter,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = BrandGreen,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Lugar del campus: abre el listado filtrado por su categoría. */
@Composable
private fun PlaceCard(place: PlaceItem, title: String, onClick: () -> Unit) {
    FotoCard(
        painter = painterResource(id = place.imageRes),
        title = title,
        icon = place.icon,
        onClick = onClick,
    )
}

/** Póster de un evento cultural: abre la noticia en el navegador. */
@Composable
private fun EventCard(event: CulturalEventItem, onOpenUrl: (String) -> Unit) {
    FotoCard(
        painter = painterResource(id = event.imageRes),
        title = event.title,
        onClick = { onOpenUrl(event.url) },
    )
}
