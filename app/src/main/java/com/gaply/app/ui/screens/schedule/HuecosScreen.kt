package com.gaply.app.ui.screens.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.di.appContainer
import com.gaply.app.domain.agent.DiaSemana
import com.gaply.app.domain.agent.FranjaHoraria
import com.gaply.app.domain.agent.Recomendacion
import com.gaply.app.domain.agent.ResultadoAgente
import com.gaply.app.ui.components.GaplySelectField
import com.gaply.app.ui.components.InterestPill
import com.gaply.app.ui.screens.register.RegisterData
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary

private val horasDisponibles = (7..21).map { "%02d:00".format(it) }

/**
 * Módulo 2 · "Mis huecos".
 *
 * Pantalla de entrada de datos para el agente: al cambiar los intereses o la
 * franja libre, las recomendaciones se recalculan en vivo (entrada distinta →
 * salida distinta).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HuecosScreen(
    onBack: () -> Unit,
    diaInicial: String = "",
    inicioInicial: String = "",
    finInicial: String = "",
) {
    val context = LocalContext.current
    val viewModel: HuecosViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HuecosViewModel.factory(
            context.appContainer(),
            diaInicial = diaInicial,
            inicioInicial = inicioInicial,
            finInicial = finInicial,
        ),
    )
    val s = LocalStrings.current
    viewModel.strings = s
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back,
                )
            }
            Text(text = s.myFreeSlots, style = MaterialTheme.typography.titleLarge)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = s.tellUsLikesFreeTime,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            InteresesCard(
                seleccionados = state.interesesSeleccionados,
                aviso = state.aviso,
                onToggle = viewModel::alternarInteres,
            )

            Spacer(modifier = Modifier.height(16.dp))

            HuecoCard(state = state, viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            ResultadosCard(resultado = state.resultado)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = s.utilityExplanation,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ===================================================================
// TARJETAS DE ENTRADA
// ===================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InteresesCard(
    seleccionados: List<String>,
    aviso: HuecosAviso?,
    onToggle: (String) -> Unit,
) {
    val s = LocalStrings.current

    SectionCard(title = s.yourInterests) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RegisterData.interests.forEach { interes ->
                InterestPill(
                    label = s.interestLabel(interes),
                    selected = interes in seleccionados,
                    onClick = { onToggle(interes) },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (aviso) {
                HuecosAviso.MAX_INTERESTS -> s.minInterestsWarning(HuecosViewModel.MAX_INTERESES)
                HuecosAviso.END_BEFORE_START -> s.endMustBeAfterStart
                null -> s.selectedCount(seleccionados.size, HuecosViewModel.MAX_INTERESES)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (aviso != null) MaterialTheme.colorScheme.error else TextSecondary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HuecoCard(
    state: HuecosUiState,
    viewModel: HuecosViewModel,
) {
    val s = LocalStrings.current

    SectionCard(title = s.yourFreeSlotTitle) {
        Text(
            text = s.dayLabel,
            style = MaterialTheme.typography.labelLarge,
            color = BrandGreen,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DiaSemana.values().forEach { dia ->
                DayChip(
                    label = s.dayName(dia.titulo),
                    selected = state.dia == dia,
                    onClick = { viewModel.seleccionarDia(dia) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.fromLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = BrandGreen,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                GaplySelectField(
                    selected = state.horaInicio,
                    placeholder = s.startShort,
                    options = horasDisponibles,
                    onSelect = viewModel::seleccionarInicio,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.toLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = BrandGreen,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                GaplySelectField(
                    selected = state.horaFin,
                    placeholder = s.endShort,
                    options = horasDisponibles,
                    onSelect = viewModel::seleccionarFin,
                )
            }
        }
    }
}

// ===================================================================
// SALIDA DEL AGENTE
// ===================================================================

@Composable
private fun ResultadosCard(resultado: ResultadoAgente?) {
    val s = LocalStrings.current

    SectionCard(title = s.suggestionsTitle) {
        when {
            resultado == null -> Text(
                text = s.fixTimeToSeeSuggestions,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            resultado.recomendaciones.isEmpty() -> Text(
                text = s.noneFitsSlot,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            else -> resultado.recomendaciones.forEachIndexed { index, recomendacion ->
                if (index > 0) Spacer(modifier = Modifier.height(14.dp))
                RecomendacionItem(recomendacion = recomendacion)
            }
        }

        if (resultado != null && resultado.descartadas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = s.doesNotFit(resultado.descartadas.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandGreen,
            )
            Spacer(modifier = Modifier.height(8.dp))
            resultado.descartadas.forEach { descarte ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.activityName(descarte.actividad.id, descarte.actividad.nombre),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = descarte.motivo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun RecomendacionItem(recomendacion: Recomendacion) {
    val s = LocalStrings.current
    val hueco = recomendacion.huecoAprovechado

    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(color = BrandGreen, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = recomendacion.puntuacion.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SurfaceWhite,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = s.activityName(recomendacion.actividad.id, recomendacion.actividad.nombre),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            recomendacion.razones.forEach { razon ->
                Text(
                    text = "• $razon",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = s.yourSlotIs(
                    "${s.dayName(hueco.dia.titulo)} " +
                        "${FranjaHoraria.aHora(hueco.inicioMinutos)}-${FranjaHoraria.aHora(hueco.finMinutos)}",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = BrandGreen,
            )
        }
    }
}

// ===================================================================
// AUXILIARES
// ===================================================================

/** Tarjeta blanca con título verde en serif (estilo Módulo 1). */
@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, Sage),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrandGreen,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun DayChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Mint.copy(alpha = 0.35f),
            labelColor = BrandGreen,
            selectedContainerColor = BrandGreen,
            selectedLabelColor = SurfaceWhite,
        ),
        border = BorderStroke(
            width = if (selected) 0.dp else 1.dp,
            color = if (selected) androidx.compose.ui.graphics.Color.Transparent else Sage,
        ),
    )
}
