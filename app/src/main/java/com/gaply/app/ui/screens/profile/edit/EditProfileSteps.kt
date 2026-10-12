package com.gaply.app.ui.screens.profile.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.gaply.app.ui.components.GaplyOutlineButton
import com.gaply.app.ui.components.GaplyPrimaryButton
import com.gaply.app.ui.components.GaplySelectField
import com.gaply.app.ui.components.GaplyTextField
import com.gaply.app.ui.components.InterestPill
import com.gaply.app.ui.screens.register.RegisterData
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary
import java.io.File

private val numericMonths = (1..12).map { it.toString().padStart(2, '0') }

// ===================================================================
// PASO 1: FOTO DE PERFIL Y BÁSICOS
// ===================================================================

@Composable
internal fun EditStepBasicsContent(
    state: EditProfileUiState,
    viewModel: EditProfileViewModel,
) {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            tempCameraUri?.let { viewModel.onPhotoSelected(it) }
        }
    }

    val launchCamera = {
        val tempFile = File.createTempFile("gaply_edit_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile,
        )
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    EditStepScaffold(onNext = viewModel::onNextStep) {
        EditStepCard(
            title = "Foto de Perfil y Básicos",
            subtitle = "Esta es la foto que verán otros usuarios en el swipe.",
        ) {
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 150.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Mint, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, Sage, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                val localUri = state.localPhotoUri
                when {
                    localUri != null -> AsyncImage(
                        model = localUri,
                        contentDescription = "Vista previa de foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )

                    state.currentPhotoUrl.isNotBlank() -> AsyncImage(
                        model = state.currentPhotoUrl,
                        contentDescription = "Foto de perfil actual",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )

                    else -> Text(
                        text = state.firstName.trim().firstOrNull()?.uppercase() ?: "G",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconOutlineButton(
                    text = "Cambiar foto",
                    icon = Icons.Filled.Image,
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                )
                IconOutlineButton(
                    text = "Cámara",
                    icon = Icons.Filled.PhotoCamera,
                    onClick = launchCamera,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            GaplyTextField(
                value = state.firstName,
                onValueChange = viewModel::onFirstNameChange,
                label = "Nombres",
                errorText = state.firstNameError,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyTextField(
                value = state.lastName,
                onValueChange = viewModel::onLastNameChange,
                label = "Apellidos",
            )
        }
    }
}

// ===================================================================
// PASO 2: DETALLES PERSONALES
// ===================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditStepDetailsContent(
    state: EditProfileUiState,
    viewModel: EditProfileViewModel,
) {
    EditStepScaffold(
        onNext = viewModel::onNextStep,
        onBack = viewModel::onPreviousStep,
    ) {
        EditStepCard(title = "Detalles Personales") {
            StepLabel(text = "Universidad")
            GaplySelectField(
                selected = state.university,
                placeholder = "Seleccione una opción",
                options = RegisterData.universities,
                onSelect = viewModel::onUniversityChange,
                isError = state.universityError != null,
            )
            if (state.universityError != null) {
                StepError(text = state.universityError)
            }

            Spacer(modifier = Modifier.height(16.dp))

            StepLabel(text = "Fecha de nacimiento *")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GaplySelectField(
                    selected = state.birthDay,
                    placeholder = "dd",
                    options = RegisterData.days,
                    onSelect = viewModel::onBirthDayChange,
                    isError = state.birthDateError != null,
                    modifier = Modifier.weight(1f),
                )
                GaplySelectField(
                    selected = state.birthMonth,
                    placeholder = "mm",
                    options = numericMonths,
                    onSelect = viewModel::onBirthMonthChange,
                    isError = state.birthDateError != null,
                    modifier = Modifier.weight(1f),
                )
                GaplySelectField(
                    selected = state.birthYear,
                    placeholder = "aaaa",
                    options = RegisterData.years,
                    onSelect = viewModel::onBirthYearChange,
                    isError = state.birthDateError != null,
                    modifier = Modifier.weight(1.2f),
                )
            }
            if (state.birthDateError != null) {
                StepError(text = state.birthDateError)
            }

            Spacer(modifier = Modifier.height(16.dp))

            StepLabel(text = "Género")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RegisterData.genders.forEach { option ->
                    FilterChip(
                        selected = state.gender == option,
                        onClick = { viewModel.onGenderChange(option) },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandGreen,
                            selectedLabelColor = SurfaceWhite,
                        ),
                        border = BorderStroke(
                            width = if (state.gender == option) 0.dp else 1.dp,
                            color = if (state.gender == option) Color.Transparent else Sage,
                        ),
                    )
                }
            }
        }
    }
}

// ===================================================================
// PASO 3: INTERESES Y DESCRIPCIÓN
// ===================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditStepInterestsContent(
    state: EditProfileUiState,
    viewModel: EditProfileViewModel,
) {
    EditStepScaffold {
        EditStepCard(
            title = "Intereses y descripción",
            subtitle = "Selecciona hasta 3 intereses (aparecen en tu perfil)",
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Los intereses personalizados del registro también se
                // muestran aquí para poder quitarlos o volver a elegirlos.
                val extras = state.interests.filter { it !in RegisterData.interests }
                (RegisterData.interests + extras).forEach { interest ->
                    InterestPill(
                        label = interest,
                        selected = interest in state.interests,
                        onClick = { viewModel.onInterestToggle(interest) },
                    )
                }
            }

            Text(
                text = "Puedes elegir hasta ${EditProfileViewModel.MAX_INTERESTS} intereses.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            StepLabel(text = "Descripción")

            GaplyTextField(
                value = state.bio,
                onValueChange = viewModel::onBioChange,
                label = "Descripción",
                placeholder = "Cuéntanos un poco sobre ti...",
                minLines = 4,
                maxLines = 4,
                singleLine = false,
                imeAction = ImeAction.Default,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${state.bio.length}/${EditProfileViewModel.MAX_BIO_LENGTH}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            GaplyPrimaryButton(
                text = "Guardar cambios",
                onClick = viewModel::onSave,
                loading = state.isLoading,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyOutlineButton(
                text = "Atrás",
                onClick = viewModel::onPreviousStep,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaplyOutlineButton(
                text = "Vista previa del perfil",
                onClick = viewModel::onTogglePreview,
            )
        }
    }
}

// ===================================================================
// VISTA PREVIA DEL PERFIL
// ===================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditProfilePreview(
    state: EditProfileUiState,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(text = "Vista previa del perfil", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(color = Mint, shape = CircleShape)
                        .border(2.dp, BrandGreen, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val localUri = state.localPhotoUri
                    when {
                        localUri != null -> AsyncImage(
                            model = localUri,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )

                        state.currentPhotoUrl.isNotBlank() -> AsyncImage(
                            model = state.currentPhotoUrl,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )

                        else -> Text(
                            text = state.firstName.trim().firstOrNull()?.uppercase() ?: "G",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandGreen,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.firstName.trim().ifBlank { "—" },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = state.university.ifBlank { "Sin universidad" },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (state.bio.isNotBlank()) {
                    Text(
                        text = state.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (state.interests.isEmpty()) {
                    Text(
                        text = "Aún no has seleccionado intereses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        state.interests.forEach { interest ->
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
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cerrar", color = BrandGreen)
            }
        },
    )
}

// ===================================================================
// AUXILIARES
// ===================================================================

/** Estructura común de los pasos: contenido con scroll + botones de abajo. */
@Composable
private fun EditStepScaffold(
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    nextText: String = "Siguiente",
    onBack: (() -> Unit)? = null,
    backText: String = "Atrás",
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            content = content,
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            when {
                onNext != null && onBack != null -> Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GaplyOutlineButton(
                        text = backText,
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                    )
                    GaplyPrimaryButton(
                        text = nextText,
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                    )
                }

                onNext != null -> GaplyPrimaryButton(text = nextText, onClick = onNext)
                onBack != null -> GaplyOutlineButton(text = backText, onClick = onBack)
            }
        }
    }
}

/** Tarjeta blanca de cada paso, con título verde en serif (estilo Módulo 1). */
@Composable
private fun EditStepCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BrandGreen,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            content()
        }
    }
}

/** Etiqueta de campo (Universidad, Fecha de nacimiento, Género, Descripción). */
@Composable
private fun StepLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = BrandGreen,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun StepError(text: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

/** Botón outline con icono (Cambiar foto / Cámara). */
@Composable
private fun IconOutlineButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Sage),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
