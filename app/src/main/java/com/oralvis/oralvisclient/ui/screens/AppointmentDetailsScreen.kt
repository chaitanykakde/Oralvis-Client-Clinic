package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.oralvis.oralvisclient.ui.components.AvatarInfoRow
import com.oralvis.oralvisclient.ui.components.OralVisCard
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant

@Composable
fun AppointmentDetailsScreen(
    modifier: Modifier = Modifier,
    patientName: String,
    gender: String?,
    dateRange: String,
    doctorName: String?,
    timeSlot: String?,
    onCheckIn: () -> Unit,
    onMoreClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(OralVisDimensions.Four)
    ) {
        Text(
            text = "Appointment Details",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(vertical = OralVisDimensions.Three)
        )
        OralVisCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                AvatarInfoRow(
                    primaryText = patientName,
                    secondaryText = gender,
                    onMoreClick = onMoreClick
                )
                Spacer(modifier = Modifier.height(OralVisDimensions.Three))
                RowWithIcon(text = dateRange, iconLabel = "📅")
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                RowWithIcon(text = doctorName ?: "", iconLabel = "👤")
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                RowWithIcon(text = timeSlot ?: "", iconLabel = "🕐")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryGradientButton(
            text = "Check - In",
            onClick = onCheckIn,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RowWithIcon(text: String, iconLabel: String) {
    Row(
        modifier = Modifier.padding(vertical = OralVisDimensions.Half),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(text = iconLabel, fontSize = 16.sp, modifier = Modifier.padding(end = OralVisDimensions.Two))
        Text(
            text = text,
            color = OralVisOnSurface,
            fontSize = 14.sp
        )
    }
}
