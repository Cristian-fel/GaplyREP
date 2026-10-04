package com.gaply.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gaply.app.ui.theme.BrandGreen
import com.gaply.app.ui.theme.ErrorRed
import com.gaply.app.ui.theme.SuccessGreen

enum class DialogStyle { SUCCESS, ERROR, INFO }

@Composable
fun AppDialog(
    style: DialogStyle,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmText: String = "Aceptar",
) {
    val containerColor = when (style) {
        DialogStyle.SUCCESS -> SuccessGreen
        DialogStyle.ERROR -> ErrorRed
        DialogStyle.INFO -> BrandGreen
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        icon = {
            Icon(
                imageVector = when (style) {
                    DialogStyle.SUCCESS -> Icons.Filled.Lock
                    DialogStyle.ERROR -> Icons.Filled.Warning
                    DialogStyle.INFO -> Icons.Filled.Info
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Transparent),
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = confirmText, color = Color.White)
            }
        },
    )
}
