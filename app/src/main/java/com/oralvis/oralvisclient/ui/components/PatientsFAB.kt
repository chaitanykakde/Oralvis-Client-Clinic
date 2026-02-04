package com.oralvis.oralvisclient.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnPrimary
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

data class PatientsFABOption(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun PatientsFAB(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddAppointment: () -> Unit,
    onAddFile: () -> Unit,
    onAddBill: () -> Unit,
    onAddPrescription: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(OralVisDimensions.Two, Alignment.Bottom)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(OralVisDimensions.Two),
                horizontalAlignment = Alignment.End
            ) {
                listOf(
                    PatientsFABOption("Add Prescription", Icons.Default.Description, onAddPrescription),
                    PatientsFABOption("Add Bill", Icons.Default.Receipt, onAddBill),
                    PatientsFABOption("Add File", Icons.Default.Description, onAddFile),
                    PatientsFABOption("Add Appointment", Icons.Default.CalendarMonth, onAddAppointment)
                ).reversed().forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(OralVisDimensions.One),
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(OralVisDimensions.ButtonCornerRadius))
                            .clip(RoundedCornerShape(OralVisDimensions.ButtonCornerRadius))
                            .background(OralVisPrimary)
                            .clickable {
                                option.onClick()
                                onToggle()
                            }
                            .padding(horizontal = OralVisDimensions.Three, vertical = OralVisDimensions.Two)
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.label,
                            tint = OralVisOnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = option.label,
                            color = OralVisOnPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF9E9E9E))
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Open menu",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
