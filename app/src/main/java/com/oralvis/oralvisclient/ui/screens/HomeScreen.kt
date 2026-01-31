package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.ui.components.AvatarInfoRow
import com.oralvis.oralvisclient.ui.components.OralVisCard
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: com.oralvis.oralvisclient.ui.viewmodel.DashboardViewModel = viewModel(
        factory = OralVisViewModelFactory(AppGraph)
    ),
    appointmentViewModel: com.oralvis.oralvisclient.ui.viewmodel.AppointmentViewModel = viewModel(
        factory = OralVisViewModelFactory(AppGraph)
    ),
    clinicId: String?,
    onNavigateToCalendar: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToAppointmentDetails: (String) -> Unit
) {
    val dashboardState by viewModel.dashboardStatsState.collectAsState()
    val appointmentsState by appointmentViewModel.appointmentsState.collectAsState()
    val sessionManager = AppGraph.sessionManager()
    val userName = sessionManager.getCurrentUser()?.name ?: ""

    LaunchedEffect(clinicId) {
        clinicId?.let { id ->
            viewModel.loadDashboardStats(id)
            appointmentViewModel.loadAppointments(id)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(OralVisDimensions.Four)
    ) {
        Text(
            text = userName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OralVisOnSurface,
            modifier = Modifier.padding(vertical = OralVisDimensions.Two)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(OralVisDimensions.Four),
            contentPadding = PaddingValues(vertical = OralVisDimensions.Two)
        ) {
            item {
                OralVisCard(
                    modifier = Modifier
                        .size(160.dp)
                        .clickable(onClick = onNavigateToCalendar)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📅",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = OralVisDimensions.Two)
                        )
                        Text(
                            text = "Calendar",
                            color = OralVisOnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            item {
                OralVisCard(
                    modifier = Modifier
                        .size(160.dp)
                        .clickable(onClick = onNavigateToPatients)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👥",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = OralVisDimensions.Two)
                        )
                        Text(
                            text = "Patients",
                            color = OralVisOnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(OralVisDimensions.Four))
        Text(
            text = "Patients",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = OralVisOnSurface,
            modifier = Modifier.padding(vertical = OralVisDimensions.Two)
        )
        when (val state = appointmentsState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(OralVisDimensions.Six),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = OralVisPrimary) }
            is UiState.Error -> Text(
                text = state.message,
                color = Color.Red,
                modifier = Modifier.padding(OralVisDimensions.Four)
            )
            is UiState.Success -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(OralVisDimensions.Two),
                contentPadding = PaddingValues(bottom = OralVisDimensions.Eight)
            ) {
                items(state.data) { booking ->
                    OralVisCard(modifier = Modifier.fillMaxWidth().clickable {
                        onNavigateToAppointmentDetails(booking.id)
                    }) {
                        AvatarInfoRow(
                            primaryText = booking.patientName,
                            secondaryText = booking.status.name.lowercase().replaceFirstChar { it.uppercase() },
                            onMoreClick = {}
                        )
                    }
                }
            }
        }
    }
}
