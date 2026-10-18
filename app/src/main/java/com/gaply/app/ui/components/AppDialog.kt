package com.gaply.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gaply.app.R
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.SuccessGreen

// 1. Añadimos WARNING al enum
enum class DialogStyle { SUCCESS, ERROR, WARNING, INFO }

/**
 * Diálogo modal compartido.
 *
 * @param icon     Icono personalizado opcional; si es null usa el del [style].
 * @param iconTint Tinte del icono personalizado; si es null usa el del [style].
 * @param iconBackgroundColor Color de fondo circular tras el icono (opcional).
 * @param borderColor Color del borde del modal (los mockups usan verde en los éxito).
 */
@Composable
fun AppDialog(
    style: DialogStyle,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmText: String = LocalStrings.current.accept,
    showConfirmButton: Boolean = true,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    iconBackgroundColor: Color? = null,
    borderColor: Color = Color.Black,
) {
    val s = LocalStrings.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(3.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Botón de cerrar "X" en la esquina superior derecha
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .border(1.5.dp, Color.Black, CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = s.closeCd,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Contenido principal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Icono: personalizado si se pasó, o el propio del estilo
                    when {
                        icon != null -> {
                            if (iconBackgroundColor != null) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(iconBackgroundColor),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconTint ?: Color.White,
                                        modifier = Modifier.size(36.dp),
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint ?: Color.Unspecified,
                                    modifier = Modifier.size(56.dp),
                                )
                            }
                        }
                        // 2. Evaluamos los estilos aquí
                        style == DialogStyle.SUCCESS -> {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = s.successCd,
                                tint = SuccessGreen,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        style == DialogStyle.INFO -> {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = s.infoCd,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        style == DialogStyle.WARNING -> {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = s.warningCd,
                                tint = ErrorRed,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        else -> {
                            Icon(
                                painter = painterResource(id = R.drawable.wifi),
                                contentDescription = s.errorCd,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (showConfirmButton) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = confirmText,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}