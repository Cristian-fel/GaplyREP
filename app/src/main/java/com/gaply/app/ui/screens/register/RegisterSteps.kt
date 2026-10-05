package com.gaply.app.ui.screens.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.gaply.app.R
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.components.GaplyTextField
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.TextSecondary
import java.io.File

// ===================================================================
// PASO 1: CUENTA
// ===================================================================

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

// ===================================================================
// PASO 2: INFORMACIÓN PERSONAL
// ===================================================================

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

// ===================================================================
// PASO 3: OBJETIVO
// ===================================================================

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
            Text(text = "¿Qué te trae a Gaply?", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Elige el objetivo que mejor te describa. Podrás cambiarlo después.",
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

// ===================================================================
// PASO 4: INTERESES
// ===================================================================

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
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Puedes elegir hasta ${RegisterData.MAX_INTERESTS} intereses.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            val allInterests = RegisterData.interests + state.extraInterests
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                allInterests.chunked(3).forEach { rowInterests ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        rowInterests.forEach { interest ->
                            InterestCard(
                                label = interest,
                                selected = state.selectedInterests.contains(interest),
                                onClick = { viewModel.onToggleInterest(interest) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - rowInterests.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
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
private fun InterestCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageRes = interestImageFor(label)

    Card(
        onClick = onClick,
        modifier = modifier
            .height(110.dp)
            .then(
                if (selected) {
                    Modifier.border(2.5.dp, BrandGreen, RoundedCornerShape(16.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            ),
                            startY = 60f
                        )
                    )
            )

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}

private fun interestImageFor(label: String): Int? {
    val cleanLabel = label.lowercase().trim()
    return when {
        cleanLabel.contains("deporte") -> R.drawable.ic_deportes
        cleanLabel.contains("música") || cleanLabel.contains("musica") -> R.drawable.ic_musica
        cleanLabel.contains("videojuego") || cleanLabel.contains("video juegos") -> R.drawable.ic_videojuegos
        cleanLabel.contains("arte") -> R.drawable.ic_arte
        cleanLabel.contains("mesa") -> R.drawable.ic_juegosmesa
        cleanLabel.contains("película") || cleanLabel.contains("pelicula") -> R.drawable.ic_peliculas
        cleanLabel.contains("estudio") -> R.drawable.ic_estudio
        cleanLabel.contains("evento") -> R.drawable.ic_eventos
        cleanLabel.contains("baile") -> R.drawable.ic_baile
        else -> null
    }
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
                    text = "Máximo ${RegisterData.MAX_INTERESTS} intereses en total " +
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
                        FilterChip(
                            selected = state.selectedInterests.contains(option),
                            onClick = { viewModel.onToggleInterest(option) },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Mint,
                                selectedLabelColor = BrandGreen,
                            )
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

// ===================================================================
// PASO 5: FOTO Y BIOGRAFÍA
// ===================================================================

@Composable
internal fun StepPhotoContent(
    state: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    val context = LocalContext.current
    val displayName = state.firstName.trim().ifBlank {
        state.username.trim().ifBlank { "Gaply" }
    }

    // Launchers para Galería y Cámara
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it) }
    }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.onProfileImageSelected(tempCameraUri)
        }
    }

    val launchCamera = {
        val tempFile = File.createTempFile("profile_temp_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header superior extendido con gradiente de contraste
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                // Imagen ic_uniarriba desde el borde superior
                Image(
                    painter = painterResource(id = R.drawable.ic_uniarriba),
                    contentDescription = "Header Universidad",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                )

                // Capa de degradado semi-transparente para mayor opacidad/contraste
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                // Tarjeta flotante con vista previa del avatar y botones de acción
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Mint.copy(alpha = 0.92f)
                    ),
                    border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        // Vista Previa de la Foto de Perfil
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(color = Color.White, shape = CircleShape)
                                .border(2.5.dp, BrandGreen, CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (state.profileImageUri != null) {
                                AsyncImage(
                                    model = state.profileImageUri,
                                    contentDescription = "Vista previa de foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = displayName.firstOrNull()?.uppercase() ?: "G",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = BrandGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botones Cargar / Tomar foto
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GaplyOutlineButton(
                                text = "Cargar foto",
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            )
                            GaplyPrimaryButton(
                                text = "Tomar Foto",
                                onClick = { launchCamera() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de saludo y descripción
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hola $displayName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Danos una breve descripción:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                GaplyTextField(
                    value = state.bio,
                    onValueChange = viewModel::onBioChange,
                    label = "Biografía",
                    singleLine = false,
                    minLines = 4,
                    maxLines = 6,
                    imeAction = ImeAction.Default,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Botones inferiores de guardado y navegación
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
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
}

// ===================================================================
// COMPONENTE AUXILIAR DROPDOWN
// ===================================================================

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