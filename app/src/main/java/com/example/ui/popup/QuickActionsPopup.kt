package com.example.ui.popup

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppModel
import com.example.ui.components.AppIconView
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsPopup(
    app: AppModel,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleHide: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp, top = 8.dp)
        ) {
            // App Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppIconView(
                    packageName = app.packageName,
                    drawable = app.iconDrawable,
                    size = 58.dp,
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Items
            QuickActionPill(
                icon = Icons.Rounded.Launch,
                label = "Ouvrir l'application",
                onClick = {
                    onDismiss()
                    onLaunch()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionPill(
                icon = if (app.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                label = if (app.isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                highlight = app.isFavorite,
                onClick = {
                    onDismiss()
                    onToggleFavorite()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionPill(
                icon = Icons.Rounded.Info,
                label = "Informations sur l'application",
                onClick = {
                    onDismiss()
                    onAppInfo()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionPill(
                icon = Icons.Rounded.VisibilityOff,
                label = "Masquer l'application",
                onClick = {
                    onDismiss()
                    onToggleHide()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionPill(
                icon = Icons.Rounded.Delete,
                label = "Désinstaller",
                isDestructive = true,
                onClick = {
                    onDismiss()
                    onUninstall()
                }
            )
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    highlight: Boolean = false,
    isDestructive: Boolean = false
) {
    val backgroundColor = when {
        isDestructive -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        highlight -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val contentColor = when {
        isDestructive -> MaterialTheme.colorScheme.error
        highlight -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}
