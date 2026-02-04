package com.oralvis.oralvisclient.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oralvis.oralvisclient.ui.theme.OralVisBottomNavActive
import com.oralvis.oralvisclient.ui.theme.OralVisBottomNavBackground
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null
)

@Composable
fun FloatingBottomNav(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = OralVisBottomNavBackground,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        imageVector = item.selectedIcon ?: item.icon,
                        contentDescription = item.label,
                        tint = if (selected) OralVisBottomNavActive else OralVisOnSurfaceVariant
                    )
                }
            )
        }
    }
}
