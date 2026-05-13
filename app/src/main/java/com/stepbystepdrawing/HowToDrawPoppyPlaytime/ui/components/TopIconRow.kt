package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.IconGray

@Composable
fun TopIconRow(
    showBack: Boolean,
    onBack: () -> Unit,
    showFavorite: Boolean = false,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onOpenFavorites: (() -> Unit)? = null,
    favoritesShortcutHighlighted: Boolean = false,
    onShare: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onExitClick: () -> Unit = {},
    showTrailingActions: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
) {
    val topIconSize: Dp = 30.dp
    val favoriteTint =
        if (isFavorite || favoritesShortcutHighlighted) Color(0xFFFFC107) else IconGray
    val rowArrangement =
        if (showTrailingActions) horizontalArrangement else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = rowArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = IconGray,
                    modifier = Modifier.size(topIconSize)
                )
            }
        } else if (onOpenFavorites != null) {
            IconButton(onClick = onOpenFavorites) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Open favorites",
                    tint = favoriteTint,
                    modifier = Modifier.size(topIconSize)
                )
            }
        } else if (showFavorite) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = favoriteTint,
                    modifier = Modifier.size(topIconSize)
                )
            }
        } else {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = IconGray,
                    modifier = Modifier.size(topIconSize)
                )
            }
        }
        if (showBack && showFavorite) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = favoriteTint,
                    modifier = Modifier.size(topIconSize)
                )
            }
        }
        if (showTrailingActions) {
            IconButton(onClick = onShare) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = IconGray,
                    modifier = Modifier.size(topIconSize)
                )
            }
            IconButton(onClick = onPlayClick) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = IconGray,
                    modifier = Modifier.size(topIconSize)
                )
            }
            IconButton(onClick = onExitClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Exit app",
                    tint = IconGray,
                    modifier = Modifier.size(topIconSize)
                )
            }
        }
    }
}
