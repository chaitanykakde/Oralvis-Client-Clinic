package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.components.OralVisCard
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    onCompleteProfile: () -> Unit = {},
    onSaveNext: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(OralVisDimensions.Four)
    ) {
        Text(
            text = "Summary",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(vertical = OralVisDimensions.Three)
        )
        OralVisCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Consult",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OralVisOnSurface
                )
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                Text(
                    text = "To activate consult, we will require you to complete your profile",
                    fontSize = 13.sp,
                    color = OralVisOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                androidx.compose.material3.OutlinedButton(
                    onClick = onCompleteProfile,
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = OralVisPrimary
                    )
                ) {
                    Text("Complete Profile", fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(OralVisDimensions.Four))
        OralVisCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Health Feed",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OralVisOnSurface
                )
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                Text(text = "0 Article Views", fontSize = 14.sp, color = OralVisOnSurfaceVariant)
                Text(text = "0 Profile Views (via Health feed)", fontSize = 14.sp, color = OralVisOnSurfaceVariant)
                Text(text = "0 Article Likes", fontSize = 14.sp, color = OralVisOnSurfaceVariant)
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                androidx.compose.material3.TextButton(onClick = {}) {
                    Text("View More", color = OralVisPrimary, fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryGradientButton(
            text = "Save & Next",
            onClick = onSaveNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
