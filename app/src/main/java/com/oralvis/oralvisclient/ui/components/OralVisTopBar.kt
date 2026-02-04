package com.oralvis.oralvisclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary
import com.oralvis.oralvisclient.ui.theme.OralVisPrimaryDark
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnPrimary

/**
 * OralVis top bar: solid blue, logo + "OralVis" text.
 * Optionally shows tabs (e.g. "APPS" / "SUMMARY") with underline for active.
 */
@Composable
fun OralVisTopBar(
    modifier: Modifier = Modifier,
    showTabs: Boolean = false,
    activeTabIndex: Int = 0,
    tabTitles: List<String> = emptyList(),
    onTabSelected: (Int) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(OralVisPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = OralVisDimensions.Four,
                    end = OralVisDimensions.Four,
                    top = OralVisDimensions.Three,
                    bottom = if (showTabs && tabTitles.isNotEmpty()) OralVisDimensions.Two else OralVisDimensions.Three
                )
        ) {
            // Logo + title row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OralVisLogo(modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(OralVisDimensions.Two))
                Text(
                    text = "OralVis",
                    color = OralVisOnPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Centered horizontal tabs below the logo, WhatsApp-style
            if (showTabs && tabTitles.size >= 2) {
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            OralVisTab(
                                title = title,
                                selected = index == activeTabIndex,
                                onClick = { onTabSelected(index) }
                            )
                            if (index < tabTitles.lastIndex) {
                                Spacer(modifier = Modifier.width(OralVisDimensions.Three))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OralVisLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "O",
            color = OralVisOnPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OralVisTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = OralVisDimensions.Two, vertical = OralVisDimensions.One)
    ) {
        Text(
            text = title,
            color = OralVisOnPrimary.copy(alpha = if (selected) 1f else 0.7f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth()
                    .background(OralVisOnPrimary)
            )
        }
    }
}
