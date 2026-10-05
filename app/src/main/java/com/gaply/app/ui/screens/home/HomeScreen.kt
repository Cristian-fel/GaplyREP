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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TheaterComedy
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

private data class PlaceItem(
    val title: String,
    val imageRes: Int,
    val icon: ImageVector
)

private data class CulturalEventItem(
    val title: String,
    val imageRes: Int,
    val url: String
)

private val suggestedPlaces = listOf(
    PlaceItem("Biblioteca", R.drawable.img_biblioteca, Icons.Default.AccountBalance),
    PlaceItem("Cancha M7A", R.drawable.img_cancha, Icons.Default.SportsSoccer),
    PlaceItem("Salón Polifuncional", R.drawable.img_gimnasio, Icons.Default.FitnessCenter),
    PlaceItem("Zona Verde", R.drawable.img_zona_verde, Icons.Default.Park),
    PlaceItem("Auditorio", R.drawable.img_auditorio, Icons.Default.TheaterComedy)
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
fun HomeScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HomeViewModel.factory(context.appContainer()),
    )
    val state by viewModel.uiState.collectAsState()

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
                0 -> DiscoverTab(state = state, viewModel = viewModel)
                1 -> PlaceholderTab(
                    emoji = "👥",
                    title = "Matches",
                    message = "Tus matches aparecerán aquí.",
                )

                2 -> PlaceholderTab(
                    emoji = "💬",
                    title = "Chats",
                    message = "Tus conversaciones aparecerán aquí.",
                )

                else -> ProfileTab(
                    state = state,
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
            style = DialogStyle.INFO,
            title = "Crear la actividad",
            message = "En la versión final, este botón abrirá el formulario para crear tu propia actividad.",
            onDismiss = viewModel::consumeDialog,
        )

        HomeDialog.EVENT_SITE -> AppDialog(
            style = DialogStyle.INFO,
            title = "Agenda cultural",
            message = "En la versión final, este enlace abrirá el sitio del evento.",
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
    val tabs = listOf(
        "Descubrir" to Icons.Filled.Search,
        "Matches" to Icons.Filled.Groups,
        "Chats" to Icons.Filled.ChatBubble,
        "Perfil" to Icons.Filled.Person,
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
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Perfil",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = state.user?.firstName?.takeIf { it.isNotBlank() } ?: "Bienvenido",
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
                    contentDescription = "Notificaciones",
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner del Horario Libre
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
                    text = "Tienes 2 horas libres 12:00 - 2:00 p.m.",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(text = "Sugerencias para hoy")

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(suggestedPlaces) { place ->
                PlaceCard(place = place)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(text = "Crear la actividad")

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandGreen, RoundedCornerShape(16.dp))
                .clickable(onClick = viewModel::onShowCreateActivityInfo)
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
                        contentDescription = "Agregar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Agregar tu propia actividad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BrandGreen
                    )
                    Text(
                        text = "Invita a otros a unirse en tu hueco",
                        fontSize = 12.sp,
                        color = Color.Gray
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
            SectionTitle(text = "Agenda cultural")
            Row(
                modifier = Modifier.clickable { uriHandler.openUri(URL_AGENDA_GENERAL) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ir al sitio",
                    color = BrandGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Ir al sitio",
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
private fun PlaceCard(place: PlaceItem) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .width(140.dp)
            .height(150.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = place.imageRes),
                contentDescription = place.title,
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
                    text = place.title,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileTab(
    state: HomeUiState,
    onLogout: () -> Unit,
) {
    val user = state.user
    val displayName = user?.firstName?.trim()?.takeIf { it.isNotEmpty() }
        ?: user?.username.orEmpty().ifBlank { "Usuario" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(color = Mint, shape = CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val avatarUrl = user?.profilePictureUrl.orEmpty()
            if (avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = displayName.first().uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrandGreen,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "@${user?.username.orEmpty()}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, Sage),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(label = "Correo", value = user?.email.orEmpty().ifBlank { "-" })
                InfoRow(
                    label = "Universidad",
                    value = user?.university.orEmpty().ifBlank { "-" },
                )
                InfoRow(
                    label = "Fecha de nacimiento",
                    value = user?.birthDate.orEmpty().ifBlank { "-" },
                )
                InfoRow(label = "Género", value = user?.gender.orEmpty().ifBlank { "-" })
                InfoRow(
                    label = "Propósito",
                    value = user?.goal.orEmpty().ifBlank { "-" },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Intereses",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        )

        if (user == null || user.interests.isEmpty()) {
            Text(
                text = "Aún no has seleccionado intereses.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                user.interests.forEach { interest ->
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text(interest) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Mint,
                            selectedLabelColor = BrandGreen,
                        ),
                    )
                }
            }
        }

        val bio = user?.bio.orEmpty()
        if (bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Biografía",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GaplyOutlineButton(
            text = "Cerrar sesión",
            onClick = onLogout,
            borderColor = ErrorRed,
            contentColor = ErrorRed,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(150.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
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