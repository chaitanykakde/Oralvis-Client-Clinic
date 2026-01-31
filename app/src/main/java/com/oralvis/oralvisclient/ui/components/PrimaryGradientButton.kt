package com.oralvis.oralvisclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisGradientEnd
import com.oralvis.oralvisclient.ui.theme.OralVisGradientStart
import com.oralvis.oralvisclient.ui.theme.OralVisOnPrimary

/**
 * Primary action button with blue gradient and white text.
 */
@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(OralVisDimensions.ButtonCornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (enabled) listOf(OralVisGradientStart, OralVisGradientEnd)
                    else listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.7f))
                )
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = OralVisDimensions.Three, horizontal = OralVisDimensions.Six),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = OralVisOnPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
