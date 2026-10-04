package com.gaply.app.ui.screens.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaply.app.R
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.components.GaplyTextField
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.TextSecondary

@Composable
internal fun StepAccountContent(
    state: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(text = "Crea tu cuenta", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Empieza con tus datos de acceso.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            GaplyTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                label = "Usuario (@username)",
                errorText = state.usernameError,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Correo",
                errorText = state.emailError,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                isPassword = true,
                errorText = state.passwordError,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirmar Contraseña",
                isPassword = true,
                errorText = state.confirmPasswordError,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::onNextStep,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        GaplyPrimaryButton(
            text = "Siguiente",
            onClick = viewModel::onNextStep,
            loading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
internal fun StepPersonalContent(
    state: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Información personal",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cuéntanos un poco sobre ti.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            GaplyTextField(
                value = state.firstName,
                onValueChange = viewModel::onFirstNameChange,
                label = "Nombre *",
                errorText = state.firstNameError,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyTextField(
                value = state.lastName,
                onValueChange = viewModel::onLastNameChange,
                label = "Apellidos",
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fecha de nacimiento",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GaplyDropdown(
                    label = "Día",
                    value = state.birthDay,
                    options = RegisterData.days,
                    onSelected = viewModel::onBirthDayChange,
                    errorText = state.birthDateError,
                    modifier = Modifier.weight(1f),
                )
                GaplyDropdown(
                    label = "Mes",
                    value = state.birthMonth,
                    options = RegisterData.months,
                    onSelected = viewModel::onBirthMonthChange,
                    modifier = Modifier.weight(1f),
                )
                GaplyDropdown(
                    label = "Año",
                    value = state.birthYear,
                    options = RegisterData.years,
                    onSelected = viewModel::onBirthYearChange,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Dónde estudias actualmente?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            GaplyDropdown(
                label = "Universidad *",
                value = state.university,
                options = RegisterData.universities,
                onSelected = viewModel::onUniversityChange,
                errorText = state.universityError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Identidad de género",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))

            GaplyDropdown(
                label = "Elige tu género",
                value = state.gender,
                options = RegisterData.genders,
                onSelected = viewModel::onGenderChange,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        GaplyPrimaryButton(
            text = "Siguiente",
            onClick = viewModel::onNextStep,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StepGoalContent(
    state: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(text = "\u00bfQu\u00e9 te trae a Gaply?", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Elige el objetivo que mejor te describa. Podr\u00e1s cambiarlo despu\u00e9s.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RegisterData.goals.chunked(2).forEach { rowGoals ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowGoals.forEach { goal ->
                            GoalCard(
                                goal = goal,
                                selected = state.goal == goal,
                                onClick = { viewModel.onGoalChange(goal) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowGoals.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        GaplyPrimaryButton(
            text = "Siguiente",
            onClick = viewModel::onNextStep,
            loading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GoalCard(
    goal: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emoji = goal.substringBefore(' ')
    val label = goal.substringAfter(' ')

    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Mint else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) BrandGreen else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = emoji, fontSize = 30.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            if (selected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StepInterestsContent(
    state: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    var showExtras by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Selecciona algunos de tus intereses",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${state.selectedInterests.size}/${RegisterData.MAX_INTERESTS}",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandGreen,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Puedes elegir hasta ${RegisterData.MAX_INTERESTS} intereses.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (RegisterData.interests + state.extraInterests).forEach { interest ->
                    InterestChip(
                        label = interest,
                        selected = state.selectedInterests.contains(interest),
                        onClick = { viewModel.onToggleInterest(interest) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        GaplyOutlineButton(
            text = "Agregar intereses",
            onClick = { showExtras = true },
        )

        Spacer(modifier = Modifier.height(12.dp))

        GaplyPrimaryButton(
            text = "Siguiente",
            onClick = viewModel::onNextStep,
            loading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showExtras) {
        InterestsExtrasDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = {
                viewModel.onDismissExtras()
                showExtras = false
            },
        )
    }
}

@Composable
private fun InterestChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val imageRes = interestImageFor(label)
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(label)
            }
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Mint,
            selectedLabelColor = BrandGreen,
            selectedLeadingIconColor = BrandGreen,
        ),
    )
}

private fun interestImageFor(label: String): Int? = when (label) {
    "Deportes" -> R.drawable.ic_deportes
    "Música" -> R.drawable.ic_musica
    "Videojuegos" -> R.drawable.ic_videojuegos
    "Artes" -> R.drawable.ic_arte
    "Juegos de mesa" -> R.drawable.ic_juegosmesa
    "Películas" -> R.drawable.ic_peliculas
    "Estudio" -> R.drawable.ic_estudio
    "Eventos" -> R.drawable.ic_eventos
    "Baile" -> R.drawable.ic_baile
    else -> null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsExtrasDialog(
    state: RegisterUiState,
    viewModel: RegisterViewModel,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Agregar intereses") },
        text = {
            Column {
                Text(
                    text = "M\u00e1ximo ${RegisterData.MAX_INTERESTS} intereses en total " +
                            "(${state.selectedInterests.size}/${RegisterData.MAX_INTERESTS}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RegisterData.extraOptions.forEach { option ->
                        InterestChip(
                            label = option,
                            selected = state.selectedInterests.contains(option),
                            onClick = { viewModel.onToggleInterest(option) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Listo", color = BrandGreen)
            }
        },
    )
}

@Composable
internal fun StepPhotoContent(
    state: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    val displayName = state.firstName.trim().ifBlank {
        state.username.trim().ifBlank { "Gaply" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(color = Mint, shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = displayName.first().uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = BrandGreen,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    GaplyOutlineButton(
                        text = "Cargar foto",
                        onClick = viewModel::onShowGalleryInfo,
                        modifier = Modifier.weight(1f),
                    )
                    GaplyPrimaryButton(
                        text = "Tomar Foto",
                        onClick = viewModel::onShowCameraInfo,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hola $displayName",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Danos una breve descripci\u00f3n:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyTextField(
                value = state.bio,
                onValueChange = viewModel::onBioChange,
                label = "Biograf\u00eda",
                singleLine = false,
                minLines = 4,
                maxLines = 6,
                imeAction = ImeAction.Default,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        GaplyPrimaryButton(
            text = "Guardar perfil",
            onClick = viewModel::onSaveProfile,
            loading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(12.dp))

        GaplyOutlineButton(
            text = "Siguiente",
            onClick = viewModel::onNextStep
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun GaplyDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = errorText != null,
            supportingText = if (errorText != null) {
                { Text(text = errorText) }
            } else {
                null
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorSupportingTextColor = MaterialTheme.colorScheme.error,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}