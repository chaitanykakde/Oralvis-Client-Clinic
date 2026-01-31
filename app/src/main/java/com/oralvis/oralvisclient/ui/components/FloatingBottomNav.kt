package com.oralvis.oralvisclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oralvis.oralvisclient.ui.theme.OralVisBottomNavActive
import com.oralvis.oralvisclient.ui.theme.OralVisBottomNavBackground
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant

/**
 * Floating pill-shaped bottom navigation with 4 icons.
 * Active item can be highlighted with background circle.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null
)

@Composable
fun FloatingBottomNav(
    modifier: Modifier = Modifier,
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OralVisDimensions.Four, vertical = OralVisDimensions.Two)
            .clip(RoundedCornerShape(OralVisDimensions.BottomNavCornerRadius))
            .background(OralVisBottomNavBackground.copy(alpha = 0.95f))
            .padding(horizontal = OralVisDimensions.Three, vertical = OralVisDimensions.Two),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onItemClick(item.route) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(OralVisDimensions.Two)
                        .then(
                            if (selected) Modifier
                                .background(OralVisBottomNavActive.copy(alpha = 0.2f), CircleShape)
                                .padding(OralVisDimensions.Two)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.selectedIcon ?: item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        tint = if (selected) OralVisBottomNavActive else OralVisOnSurfaceVariant
                    )
                }
            }
        }
    }
}
