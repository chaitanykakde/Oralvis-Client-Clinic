package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.R
import com.oralvis.oralvisclient.ui.components.PatientPillRow
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnPrimary
import com.oralvis.oralvisclient.ui.theme.OralVisPatientCardBg
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

/**
 * Home / Apps UI body. Renders content from ViewModel state (appointments as patient preview).
 * No dummy data; loaders stop on Success or Error.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: com.oralvis.oralvisclient.ui.viewmodel.DashboardViewModel? = null,
    appointmentViewModel: com.oralvis.oralvisclient.ui.viewmodel.AppointmentViewModel? = null,
    clinicId: String? = null,
    userName: String = "",
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToPatients: () -> Unit = {},
    onNavigateToAppointmentDetails: (String) -> Unit = {},
    onNavigateToAddPatient: () -> Unit = {}
) {
    val appVm = appointmentViewModel ?: return
    val appointmentsState by appVm.appointmentsState.collectAsState()
    var menuBookingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(clinicId) {
        clinicId?.let { id -> appVm.loadAppointments(id) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAF8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = userName.ifEmpty { "Ray" },
                color = OralVisOnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            QuickActionRow(
                onCalendarClick = onNavigateToCalendar,
                onPatientsClick = onNavigateToPatients
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Patients",
                color = OralVisOnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (val state = appointmentsState) {
                is UiState.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OralVisPrimary)
                }
                is UiState.Error -> Text(
                    text = state.message,
                    color = Color.Red,
                    modifier = Modifier.padding(OralVisDimensions.Two)
                )
                is UiState.Success -> if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No patients yet",
                            color = OralVisOnSurface,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(OralVisDimensions.PatientListSpacing),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(state.data) { booking ->
                            PatientPreviewCard(
                                name = booking.patientName,
                                onClick = { onNavigateToAppointmentDetails(booking.id) },
                                onMoreClick = { menuBookingId = booking.id }
                            )
                        }
                    }
                }
            }
        }
        AddPatientFAB(
            onClick = onNavigateToAddPatient,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
        )
        if (menuBookingId != null) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { menuBookingId = null },
                modifier = Modifier.padding(OralVisDimensions.Two)
            ) {
                val id = menuBookingId!!
                DropdownMenuItem(
                    text = { Text("View details") },
                    onClick = {
                        menuBookingId = null
                        onNavigateToAppointmentDetails(id)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cancel appointment") },
                    onClick = {
                        menuBookingId = null
                        appVm.cancelBooking(id)
                        clinicId?.let { appVm.loadAppointments(it) }
                    }
                )
            }
        }
    }
}

@Composable
private fun PatientPreviewCard(
    name: String,
    onClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = OralVisDimensions.PatientPillShadow,
                shape = RoundedCornerShape(OralVisDimensions.PatientPillCornerRadius),
                clip = false
            )
            .clip(RoundedCornerShape(OralVisDimensions.PatientPillCornerRadius))
            .background(OralVisPatientCardBg)
            .clickable(onClick = onClick)
    ) {
        PatientPillRow(
            name = name,
            onMoreClick = onMoreClick
        )
    }
}

@Composable
private fun QuickActionRow(
    onCalendarClick: () -> Unit,
    onPatientsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            contentDescription = "Calender",
            iconResId = R.drawable.calender,
            modifier = Modifier.weight(1f),
            onClick = onCalendarClick
        )
        QuickActionCard(
            contentDescription = "Patients",
            iconResId = R.drawable.user,
            modifier = Modifier.weight(1f),
            onClick = onPatientsClick
        )
    }
}

@Composable
private fun QuickActionCard(
    contentDescription: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(OralVisDimensions.ActionCardHeight)
            .shadow(
                elevation = OralVisDimensions.ActionCardShadow,
                shape = RoundedCornerShape(OralVisDimensions.ActionCardCornerRadius),
                clip = false
            )
            .clip(RoundedCornerShape(OralVisDimensions.ActionCardCornerRadius))
            .background(Color.White)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(OralVisDimensions.ActionCardIconSize),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun AddPatientFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = OralVisDimensions.FabShadow,
                shape = RoundedCornerShape(OralVisDimensions.FabCornerRadius),
                clip = false
            )
            .clip(RoundedCornerShape(OralVisDimensions.FabCornerRadius))
            .background(OralVisPrimary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = OralVisDimensions.FabPaddingH,
                vertical = OralVisDimensions.FabPaddingV
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.addpatient),
                contentDescription = "Add Patient",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "Add Patient",
                color = OralVisOnPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
