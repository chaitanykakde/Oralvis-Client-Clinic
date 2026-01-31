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
import com.oralvis.oralvisclient.ui.components.AvatarInfoRow
import com.oralvis.oralvisclient.ui.components.OralVisCard
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface

@Composable
fun PatientDetailsScreen(
    modifier: Modifier = Modifier,
    patientName: String,
    dateRange: String,
    doctorName: String?,
    appointmentDate: String?,
    timeSlot: String?,
    onBook: () -> Unit,
    onMoreClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(OralVisDimensions.Four)
    ) {
        Text(
            text = "Patient Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = OralVisOnSurface,
            modifier = Modifier.padding(vertical = OralVisDimensions.Two)
        )
        OralVisCard(modifier = Modifier.fillMaxWidth()) {
            AvatarInfoRow(
                primaryText = patientName,
                secondaryText = dateRange,
                onMoreClick = onMoreClick
            )
        }
        Spacer(modifier = Modifier.height(OralVisDimensions.Four))
        Text(
            text = "Doctor Selection",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = OralVisOnSurface,
            modifier = Modifier.padding(vertical = OralVisDimensions.Two)
        )
        OralVisCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                AvatarInfoRow(
                    primaryText = doctorName ?: "Dr.",
                    secondaryText = "Dentalogist",
                    onMoreClick = onMoreClick
                )
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                Text(text = "📅 $appointmentDate", color = OralVisOnSurface, fontSize = 13.sp)
                Text(text = "🕐 $timeSlot", color = OralVisOnSurface, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                PrimaryGradientButton(text = "Book", onClick = onBook)
            }
        }
    }
}
