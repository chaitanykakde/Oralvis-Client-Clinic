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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.components.AvatarInfoRow
import com.oralvis.oralvisclient.ui.components.OralVisCard
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisGradientEnd
import com.oralvis.oralvisclient.ui.theme.OralVisGradientStart
import com.oralvis.oralvisclient.ui.theme.OralVisOnPrimary
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
            .background(Color(0xFFFDF4E5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = OralVisDimensions.Four,
                    vertical = OralVisDimensions.Four
                )
        ) {
            Text(
                text = userName.ifEmpty { " " },
                color = OralVisOnSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(OralVisDimensions.Four))

            QuickActionRow(
                onCalendarClick = onNavigateToCalendar,
                onPatientsClick = onNavigateToPatients
            )

            Spacer(modifier = Modifier.height(OralVisDimensions.Five))

            Text(
                text = "Patients",
                color = OralVisOnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(OralVisDimensions.Two))

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
                        verticalArrangement = Arrangement.spacedBy(OralVisDimensions.Two),
                        contentPadding = PaddingValues(bottom = 80.dp)
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
        FloatingActionButton(
            onClick = onNavigateToAddPatient,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(OralVisDimensions.Four),
            containerColor = OralVisPrimary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Patient")
        }
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
    OralVisCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = Color(0xFFF2F4F8)
    ) {
        AvatarInfoRow(
            primaryText = name,
            secondaryText = null,
            showMoreMenu = true,
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
        horizontalArrangement = Arrangement.spacedBy(OralVisDimensions.Three)
    ) {
        QuickActionCard(
            title = "Calender",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Calendar",
                    tint = OralVisPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            modifier = Modifier
                .weight(1f),
            onClick = onCalendarClick
        )
        QuickActionCard(
            title = "Patients",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = "Patients",
                    tint = OralVisPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            modifier = Modifier
                .weight(1f),
            onClick = onPatientsClick
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    OralVisCard(
        modifier = modifier
            .height(136.dp)
            .clip(RoundedCornerShape(OralVisDimensions.CardCornerRadius))
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(OralVisDimensions.Two))
            Text(
                text = title,
                color = OralVisOnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AddPatientButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(OralVisGradientStart, OralVisGradientEnd)
    )

    Box(
        modifier = modifier
            .wrapContentWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                clip = false
            )
            .clip(RoundedCornerShape(28.dp))
            .background(gradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = OralVisDimensions.Six,
                vertical = OralVisDimensions.Two
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ Add Patient",
            color = OralVisOnPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
