package com.oralvis.oralvisclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.theme.OralVisCompleted
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnGoing
import com.oralvis.oralvisclient.ui.theme.OralVisOnPrimary

/**
 * Pill-shaped status chip (e.g. "Completed", "On going").
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false,
    isOnGoing: Boolean = false,
    backgroundColor: Color? = null
) {
    val bg = backgroundColor ?: when {
        isCompleted -> OralVisCompleted
        isOnGoing -> OralVisOnGoing
        else -> OralVisOnGoing.copy(alpha = 0.9f)
    }
    Text(
        text = text,
        modifier = modifier
            .background(bg, RoundedCornerShape(OralVisDimensions.ChipCornerRadius))
            .padding(horizontal = OralVisDimensions.Two, vertical = OralVisDimensions.Half),
        color = OralVisOnPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )
}
