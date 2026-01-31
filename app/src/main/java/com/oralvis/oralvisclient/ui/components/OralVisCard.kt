package com.oralvis.oralvisclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oralvis.oralvisclient.ui.theme.OralVisCardBackground
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions

/**
 * Rounded card (20dp) with light grey background and subtle shadow.
 */
@Composable
fun OralVisCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = OralVisCardBackground,
    elevation: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier.shadow(elevation, RoundedCornerShape(OralVisDimensions.CardCornerRadius)),
        shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = {
            Box(
                modifier = Modifier.padding(OralVisDimensions.Four),
                content = content
            )
        }
    )
}
