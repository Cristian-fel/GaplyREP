package com.gaply.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.gaply.app.di.appContainer
import com.gaply.app.domain.model.User
import com.gaply.app.ui.components.AppDialog
import com.gaply.app.ui.components.DialogStyle
import com.gaply.app.ui.components.InterestPill
import com.gaply.app.ui.screens.register.RegisterData
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.Mint
import com.gaply.app.ui.theme.Sage
import com.gaply.app.ui.theme.SurfaceWhite
import com.gaply.app.ui.theme.TextSecondary
import java.io.File

/**
 * Módulo 2 · Pestaña "Perfil" del Home.
 * Reproduce el Figma: cabecera verde con onda, engranaje de Configuración,
 * cerrar sesión, foto con badge de cámara, botón Editar, descripción,
 * intereses y acceso a "Mi horario".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSchedule: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ProfileViewModel.factory(context.appContainer()),
    )
    val uploading by viewModel.uploading.collectAsState()
    val photoError by viewModel.error.collectAsState()

    // ---------------------------------------------------------- foto
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
        val tempFile = File.createTempFile("gaply_profile_", ".jpg", context.cacheDir).apply {
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

    // ------------------------------------------------------- datos
    val displayName = user?.firstName?.trim()?.takeIf { it.isNotEmpty() }
        ?: user?.username.orEmpty().ifBlank { "Usuario" }
    val subtitle = listOfNotNull(
        user?.university?.trim()?.takeIf { it.isNotEmpty() },
        user?.email?.trim()?.takeIf { it.isNotEmpty() },
    ).joinToString(" - ").ifBlank { "Completa tu perfil" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ProfileHeader(
            photoUrl = user?.profilePictureUrl.orEmpty(),
            fallbackLetter = displayName.firstOrNull()?.uppercase() ?: "U",
            uploading = uploading,
            onCameraClick = { galleryLauncher.launch("image/*") },
            onGalleryClick = launchCamera,
            onOpenSettings = onOpenSettings,
            onLogout = onLogout,
            onEditProfile = onEditProfile,
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            ProfileSectionCard(
                icon = Icons.Filled.Description,
                title = "Descripción",
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                val bio = user?.bio.orEmpty()
                Text(
                    text = bio.ifBlank { "Aún no has agregado una descripción." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (bio.isBlank()) TextSecondary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(
                icon = Icons.Filled.Favorite,
                title = "Intereses",
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                val selected = user?.interests.orEmpty()
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RegisterData.interests.forEach { interest ->
                        InterestPill(
                            label = interest,
                            selected = interest in selected,
                            onClick = { },
                        )
                    }
                }
                if (selected.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Elige tus intereses desde el botón Editar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(
                icon = Icons.Filled.Schedule,
                title = "Horario",
                onClick = onOpenSchedule,
                actionText = "Agregar horario",
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Agrega tus clases para descubrir tus huecos libres.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    photoError?.let { message ->
        AppDialog(
            style = DialogStyle.ERROR,
            title = "¡Ups!",
            message = message,
            onDismiss = viewModel::consumeError,
        )
    }
}

// ===================================================================
// CABECERA
// ===================================================================

/** Onda verde de la cabecera (baja a la izquierda, sube a la derecha). */
private fun DrawScope.drawHeaderWave(color: Color) {
    val wave = size.height * 0.22f
    val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height - wave)
        quadraticTo(
            x1 = size.width * 0.6f,
            y1 = size.height,
            x2 = 0f,
            y2 = size.height,
        )
        close()
    }
    drawPath(path = path, color = color)
}

@Composable
private fun ProfileHeader(
    photoUrl: String,
    fallbackLetter: String,
    uploading: Boolean,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawHeaderWave(color = BrandGreen)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                HeaderCircleButton(
                    icon = Icons.Filled.Settings,
                    contentDescription = "Configuración",
                    onClick = onOpenSettings,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = SurfaceWhite,
                    )
                }
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.labelLarge,
                    color = SurfaceWhite,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                ProfileAvatarWithBadge(
                    photoUrl = photoUrl,
                    fallbackLetter = fallbackLetter,
                    uploading = uploading,
                    onAvatarClick = onGalleryClick,
                    onBadgeClick = onCameraClick,
                )

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, SurfaceWhite),
                    colors = androidx.compose.material3.ButtonDefaults
                        .outlinedButtonColors(contentColor = SurfaceWhite),
                    modifier = Modifier.height(44.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Editar", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** Foto circular con badge de cámara abajo a la derecha. */
@Composable
private fun ProfileAvatarWithBadge(
    photoUrl: String,
    fallbackLetter: String,
    uploading: Boolean,
    onAvatarClick: () -> Unit,
    onBadgeClick: () -> Unit,
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(color = Mint, shape = CircleShape)
                .border(3.dp, SurfaceWhite, CircleShape)
                .clip(CircleShape)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center,
        ) {
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = fallbackLetter,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = SurfaceWhite, shape = CircleShape)
                .border(2.dp, BrandGreen, CircleShape)
                .clip(CircleShape)
                .clickable { onBadgeClick() },
            contentAlignment = Alignment.Center,
        ) {
            if (uploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = BrandGreen,
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Cambiar foto de perfil",
                    tint = BrandGreen,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/** Botón circular claro de la cabecera (Configuración). */
@Composable
private fun HeaderCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .background(color = SurfaceWhite, shape = CircleShape),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = BrandGreen,
        )
    }
}

// ===================================================================
// TARJETAS
// ===================================================================

/** Tarjeta de sección con icono verde en cuadrado redondeado (Descripción, Intereses, Horario). */
@Composable
private fun ProfileSectionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    actionText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, Sage),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(color = BrandGreen, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SurfaceWhite,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (actionText != null) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelLarge,
                        color = BrandGreen,
                    )
                }
            }

            content()
        }
    }
}
