package com.gaply.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.BrandGreenLight
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

private data class Place(val emoji: String, val name: String)

private data class Event(val title: String, val date: String, val color: Color)

private val suggestedPlaces = listOf(
    Place("\uD83C\uDFDB", "Biblioteca"),
    Place("\uD83C\uDFC0", "Cancha M7A"),
    Place("\uD83C\uDFCB", "Sal\u00f3n M4"),
)

private val culturalEvents = listOf(
    Event("Expo Tadeo", "Octubre", BrandGreenLight),
    Event("Dale Rumbo", "Octubre", Color(0xFF2E4A7D)),
    Event("Torneo de Ajedrez", "Noviembre", Color(0xFF7D4A2E)),
    Event("El Paro", "Noviembre", Color(0xFF5E2E7D)),
)

@Composable
fun HomeScreen(onLogout: () -> Unit) {
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
                    emoji = "\uD83D\uDC65",
                    title = "Matches",
                    message = "Tus matches aparecer\u00e1n aqu\u00ed.",
                )

                2 -> PlaceholderTab(
                    emoji = "\uD83D\uDCAC",
                    title = "Chats",
                    message = "Tus conversaciones aparecer\u00e1n aqu\u00ed.",
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
            message = "En la versi\u00f3n final, este bot\u00f3n abrir\u00e1 el formulario para crear tu propia actividad.",
            onDismiss = viewModel::consumeDialog,
        )

        HomeDialog.EVENT_SITE -> AppDialog(
            style = DialogStyle.INFO,
            title = "Agenda cultural",
            message = "En la versi\u00f3n final, este enlace abrir\u00e1 el sitio del evento.",
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

    NavigationBar(containerColor = SurfaceWhite) {
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
                        style = MaterialTheme.typography.labelSmall,
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
    val displayName = state.user?.firstName?.trim()?.takeIf { it.isNotEmpty() }
        ?: state.user?.let { "@${it.username}" }
        ?: "Usuario"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(color = Mint, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Perfil",
                    tint = BrandGreen,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificaciones",
                    tint = BrandGreen,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Mint),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(text = "\uD83D\uDD52", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tienes 2 horas libres",
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandGreen,
                    )
                    Text(
                        text = "12:00 - 2:00 p.m.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandGreenLight,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(text = "Sugerencias para hoy")

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(suggestedPlaces) { place ->
                PlaceCard(place = place)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandGreen),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Agregar tu propia actividad",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = "Invita a otros a unirse en tu hueco",
                        style = MaterialTheme.typography.bodySmall,
                        color = Mint,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                FilledIconButton(
                    onClick = viewModel::onShowCreateActivityInfo,
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = BrandGreen,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Crear actividad",
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(text = "Agenda cultural")

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(culturalEvents) { event ->
                EventCard(event = event)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ir al sitio \u2197",
            style = MaterialTheme.typography.labelLarge,
            color = BrandGreen,
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 4.dp)
                .clickable(onClick = viewModel::onShowEventSiteInfo),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun PlaceCard(place: Place) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Sage),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = place.emoji, fontSize = 30.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Disponible ahora",
                style = MaterialTheme.typography.labelSmall,
                color = BrandGreenLight,
            )
        }
    }
}

@Composable
private fun EventCard(event: Event) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(190.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = event.color),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.date,
                style = MaterialTheme.typography.labelSmall,
                color = Mint,
            )
        }
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
                .background(color = Mint, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayName.first().uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = BrandGreen,
            )
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
            border = androidx.compose.foundation.BorderStroke(1.dp, Sage),
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
                InfoRow(label = "G\u00e9nero", value = user?.gender.orEmpty().ifBlank { "-" })
                InfoRow(
                    label = "Prop\u00f3sito",
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
                text = "A\u00fan no has seleccionado intereses.",
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
                text = "Biograf\u00eda",
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
            text = "Cerrar sesi\u00f3n",
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
