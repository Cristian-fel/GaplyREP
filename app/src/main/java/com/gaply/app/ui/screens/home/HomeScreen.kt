package com.gaply.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.R
import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.screens.profile.ProfileScreen
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

private enum class PlaceKey { BIBLIOTECA, CANCHA, SALON, ZONA_VERDE, AUDITORIO }

private data class PlaceItem(
    val key: PlaceKey,
    val imageRes: Int,
    val icon: ImageVector,
)

private fun placeTitle(key: PlaceKey, s: AppStrings): String = when (key) {
    PlaceKey.BIBLIOTECA -> s.placeLibrary
    PlaceKey.CANCHA -> s.placeCourt
    PlaceKey.SALON -> s.placeMultiRoom
    PlaceKey.ZONA_VERDE -> s.placeGreenZone
    PlaceKey.AUDITORIO -> s.placeAuditorium
}

private data class CulturalEventItem(
    val title: String,
    val imageRes: Int,
    val url: String
)

private val suggestedPlaces = listOf(
    PlaceItem(PlaceKey.BIBLIOTECA, R.drawable.img_biblioteca, Icons.Default.AccountBalance),
    PlaceItem(PlaceKey.CANCHA, R.drawable.img_cancha, Icons.Default.SportsSoccer),
    PlaceItem(PlaceKey.SALON, R.drawable.img_gimnasio, Icons.Default.FitnessCenter),
    PlaceItem(PlaceKey.ZONA_VERDE, R.drawable.img_zona_verde, Icons.Default.Park),
    PlaceItem(PlaceKey.AUDITORIO, R.drawable.img_auditorio, Icons.Default.TheaterComedy)
)

private val culturalEvents = listOf(
    CulturalEventItem(
        title = "Expo Tadeo",
        imageRes = R.drawable.img_expotadeo,
        url = "https://www.utadeo.edu.co/es/eventos/expotadeo-fest"
    ),
    CulturalEventItem(
        title = "Cátedra",
        imageRes = R.drawable.img_catedra,
        url = "https://www.utadeo.edu.co/es/eventos/presupuesto-deuda-y-crecimiento-los-desafios-que-enfrenta-colombia-para-sostener-su-estado"
    ),
    CulturalEventItem(
        title = "Cabito Fest",
        imageRes = R.drawable.img_cabitofest,
        url = "https://www.utadeo.edu.co/es/noticia/especiales/sistema-de-bibliotecas/104046/participa-con-tu-emprendimiento-en-cabito-fest-2026"
    ),
    CulturalEventItem(
        title = "Torneo de Ajedrez",
        imageRes = R.drawable.img_ajedrez,
        url = "https://www.utadeo.edu.co/es/noticia/novedades/sistema-de-bibliotecas/104046/inscripciones-abiertas-torneo-de-ajedrez"
    )
)

private const val URL_AGENDA_GENERAL =
    "https://www.utadeo.edu.co/es/eventos?viewsreference%5Bcompressed%5D=eJxdkNEKgzAMRf8lzz44x5jzZ0pGYw20VWp0iPjva-lm2R5Kc3PP5UJ20CgI3Q4YzOLIC3R-sbaCgVBTgK6uwLLjcz_2_UynmtAkKAthsRQTRwXk8WlJq4gKezP_NpTxDH3-0vsdjlQSIqviY9mUbFPi4xJNwGmAf4B1tC_3tmmL1TNZrTy6FM1iZXoVINDKM48-p29t87hGM5Ms5JQmmw5VH2_zqWo_&page=1"

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenSchedule: () -> Unit = {},
    onOpenActivities: () -> Unit = {},
    onCreateActivity: () -> Unit = {},
    onOpenMyActivities: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HomeViewModel.factory(context.appContainer()),
    )
    val state by viewModel.uiState.collectAsState()
    val s = LocalStrings.current

    // Recarga el hueco de hoy cada vez que se entra/vuelve a la pantalla
    // (por ejemplo, después de editar el horario en "Mi horario").
    LaunchedEffect(Unit) {
        viewModel.cargarHorario()
    }

    Scaffold(
        containerColor = BackgroundApp,
        bottomBar = {
            GaplyBottomBar(
                selected = state.selectedTab,
                onSelect = viewModel::onSelectTab,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state.selectedTab) {
                0 -> DiscoverTab(
                    state = state,
                    viewModel = viewModel,
                    s = s,
                    onOpenActivities = onOpenActivities,
                    onCreateActivity = onCreateActivity,
                    onOpenMyActivities = onOpenMyActivities,
                )
                1 -> PlaceholderTab(
                    emoji = "👥",
                    title = s.tabMatches,
                    message = s.matchesEmptyMsg,
                )

                2 -> PlaceholderTab(
                    emoji = "💬",
                    title = s.tabChats,
                    message = s.chatsEmptyMsg,
                )

                else -> ProfileScreen(
                    user = state.user,
                    onEditProfile = onEditProfile,
                    onOpenSettings = onOpenSettings,
                    onOpenSchedule = onOpenSchedule,
                    onLogout = {
                        viewModel.signOut()
                        onLogout()
                    },
                )
            }
        }
    }

    when (state.dialog) {
        HomeDialog.CREATE_ACTIVITY -> AppDialog(
            style = DialogStyle.ERROR,
            title = s.connectionErrorTitle,
            message = s.connectionErrorMessage,
            onDismiss = viewModel::consumeDialog,
        )

        HomeDialog.EVENT_SITE -> AppDialog(
            style = DialogStyle.INFO,
            title = s.culturalAgenda,
            message = s.eventSiteInfoMsg,
            onDismiss = viewModel::consumeDialog,
        )

        null -> Unit
    }
}

@Composable
private fun GaplyBottomBar(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val s = LocalStrings.current
    val tabs = listOf(
        s.tabDiscover to Icons.Filled.Search,
        s.tabMatches to Icons.Filled.Groups,
        s.tabChats to Icons.Filled.ChatBubble,
        s.tabProfile to Icons.Filled.Person,
    )

    NavigationBar(
        containerColor = Color.White,
        contentColor = BrandGreen,
        tonalElevation = 8.dp
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelect(index) },
                icon = {
                    Icon(
                        imageVector = tab.second,
                        contentDescription = tab.first,
                    )
                },
                label = {
                    Text(
                        text = tab.first,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandGreen,
                    selectedTextColor = BrandGreen,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Mint,
                ),
            )
        }
    }
}

@Composable
private fun DiscoverTab(
    state: HomeUiState,
    viewModel: HomeViewModel,
    s: AppStrings = LocalStrings.current,
    onOpenActivities: () -> Unit = {},
    onCreateActivity: () -> Unit = {},
    onOpenMyActivities: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Cabecera de bienvenida
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
                    fontSize = 22.sp
                ),
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = s.notificationsCd,
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner del Horario Libre: el hueco más grande de hoy según el
        // horario real del usuario (el mismo dato que procesa el agente).
        Card(
            shape = RoundedCornerShape(50.dp),
            colors = CardDefaults.cardColors(containerColor = BrandGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when (val hueco = state.huecoDeHoy) {
                        null -> {
                            if (!state.tieneHorario) {
                                s.addClassesToSeeSlots
                            } else {
                                s.noFreeSlotsToday
                            }
                        }

                        else -> s.youHaveFreeSlot(
                            "${s.freeDuration(hueco.duracionMinutos)} " +
                                s.formatTimeRange(hueco.inicioHora, hueco.finHora),
                        )
                    },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(text = s.suggestionsToday)

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(suggestedPlaces) { place ->
                PlaceCard(place = place, title = placeTitle(place.key, s))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Módulo Actividades: atajo al listado "Actividades para tu hueco".
        Button(
            onClick = onOpenActivities,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGreen,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Event,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = s.activitiesForYourSlot,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(text = s.createActivitySection)

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandGreen, RoundedCornerShape(16.dp))
                .clickable(onClick = onCreateActivity)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = s.addCd,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = s.addYourOwnActivity,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BrandGreen
                    )
                    Text(
                        text = s.inviteOthers,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Módulo Actividades: acceso a las actividades publicadas por el usuario.
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandGreen),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenMyActivities)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = s.myActivitiesCardTitle,
                        tint = BrandGreen
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = s.myActivitiesCardTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = s.myActivitiesCardSubtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Encabezado de Agenda Cultural con link al sitio general
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(text = s.culturalAgenda)
            Row(
                modifier = Modifier.clickable { uriHandler.openUri(URL_AGENDA_GENERAL) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = s.visitWebsite,
                    color = BrandGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = s.visitWebsite,
                    tint = BrandGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid 2x2 de eventos culturales
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EventCard(
                    event = culturalEvents[0],
                    modifier = Modifier.weight(1f),
                    onOpenUrl = { url -> uriHandler.openUri(url) }
                )
                EventCard(
                    event = culturalEvents[1],
                    modifier = Modifier.weight(1f),
                    onOpenUrl = { url -> uriHandler.openUri(url) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EventCard(
                    event = culturalEvents[2],
                    modifier = Modifier.weight(1f),
                    onOpenUrl = { url -> uriHandler.openUri(url) }
                )
                EventCard(
                    event = culturalEvents[3],
                    modifier = Modifier.weight(1f),
                    onOpenUrl = { url -> uriHandler.openUri(url) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        color = Color.Black,
    )
}

@Composable
private fun PlaceCard(place: PlaceItem, title: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .width(140.dp)
            .height(150.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = place.imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = place.icon,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EventCard(
    event: CulturalEventItem,
    modifier: Modifier = Modifier,
    onOpenUrl: (String) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(100.dp)
            .clickable { onOpenUrl(event.url) }
    ) {
        Image(
            painter = painterResource(id = event.imageRes),
            contentDescription = event.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PlaceholderTab(
    emoji: String,
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = emoji, fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(onLogout = {})
    }
}
