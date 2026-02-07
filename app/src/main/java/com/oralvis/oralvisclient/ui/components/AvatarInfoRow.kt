package com.oralvis.oralvisclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant

/**
 * Row with circular avatar, primary text (e.g. name), optional secondary text (e.g. Male, specialty).
 * Optional trailing ellipsis menu.
 * When avatarDrawableResId is set, shows that drawable as avatar; otherwise shows initial letter.
 */
@Composable
fun AvatarInfoRow(
    primaryText: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    avatarDrawableResId: Int? = null,
    secondaryText: String? = null,
    showMoreMenu: Boolean = true,
    onMoreClick: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(OralVisOnSurfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (avatarDrawableResId != null) {
                androidx.compose.foundation.Image(
                    painter = painterResource(avatarDrawableResId),
                    contentDescription = primaryText,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = primaryText.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(OralVisDimensions.Three))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primaryText,
                color = OralVisOnSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (secondaryText != null) {
                Text(
                    text = secondaryText,
                    color = OralVisOnSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
        if (showMoreMenu) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onMoreClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u22EE",
                    color = OralVisOnSurfaceVariant,
                    fontSize = 20.sp
                )
            }
        }
    }
}
