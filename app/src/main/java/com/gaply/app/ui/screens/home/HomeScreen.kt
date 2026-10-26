package com.gaply.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.screens.chats.ChatsScreen
import com.gaply.app.ui.screens.matches.MatchesScreen
import com.gaply.app.ui.screens.profile.ProfileScreen
import com.gaply.app.ui.theme.BackgroundApp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenSchedule: () -> Unit = {},
    onOpenActivities: () -> Unit = {},
    onCreateActivity: () -> Unit = {},
    onOpenMyActivities: () -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onOpenActivity: (String) -> Unit = {},
    onOpenHuecos: (dia: String, inicio: String, fin: String) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HomeViewModel.factory(context.appContainer()),
    )
    val state by viewModel.uiState.collectAsState()
    val sugeridas by viewModel.sugeridas.collectAsState()
    val s = LocalStrings.current

    // Recarga el hueco de hoy (y las actividades sugeridas) cada vez que se
    // entra/vuelve a la pantalla (por ejemplo, tras editar el horario).
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
                    sugeridas = sugeridas,
                    s = s,
                    onOpenActivities = {
                        // Botón "Ver mis actividades actuales": listado de lo
                        // que ya uní (sin filtros de lugar ni de hueco).
                        val contenedor = context.appContainer()
                        contenedor.filtroActividades.value = null
                        contenedor.filtroFranja.value = null
                        contenedor.filtroSoloInscritas.value = true
                        onOpenActivities()
                    },
                    onOpenActivitiesByCategory = { categoria ->
                        // Tocar un lugar: el listado se abre filtrado por su
                        // categoría (y salen el modo "solo inscritas" y el
                        // hueco elegido: las entradas se excluyen entre sí).
                        val contenedor = context.appContainer()
                        contenedor.filtroActividades.value = categoria
                        contenedor.filtroFranja.value = null
                        contenedor.filtroSoloInscritas.value = false
                        onOpenActivities()
                    },
                    onCreateActivity = onCreateActivity,
                    onOpenMyActivities = onOpenMyActivities,
                    onOpenChat = onOpenChat,
                    onOpenActivity = onOpenActivity,
                    onOpenHuecos = onOpenHuecos,
                    onOpenSchedule = onOpenSchedule,
                )
                // Módulo 5 · Matches: mazo de swipe embebido en la Home.
                1 -> MatchesScreen(onOpenChat = onOpenChat)

                // Módulo 5 · Chats: lista en tiempo real de conversaciones.
                2 -> ChatsScreen(onOpenChat = onOpenChat)

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
        tonalElevation = 8.dp,
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
                            fontSize = 11.sp,
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(onLogout = {})
    }
}
