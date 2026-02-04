package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import com.oralvis.oralvisclient.ui.components.AvatarInfoRow
import com.oralvis.oralvisclient.ui.components.OralVisCard
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBookingScreen(
    clinicId: String?,
    actionTitle: String = "Select appointment",
    viewModel: com.oralvis.oralvisclient.ui.viewmodel.AppointmentViewModel = viewModel(
        factory = OralVisViewModelFactory(AppGraph)
    ),
    onBookingSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.appointmentsState.collectAsState()

    LaunchedEffect(clinicId) {
        clinicId?.let { viewModel.loadAppointments(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        androidx.compose.material3.TopAppBar(
            title = { Text("Select appointment", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OralVisPrimary,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
        when (val s = state) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = OralVisPrimary) }
            is UiState.Error -> Text(
                text = s.message,
                color = Color.Red,
                modifier = Modifier.padding(OralVisDimensions.Four)
            )
            is UiState.Success -> if (s.data.isEmpty()) {
                Text(
                    text = "No appointments. Add an appointment first.",
                    color = Color.Gray,
                    modifier = Modifier.padding(OralVisDimensions.Four)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(OralVisDimensions.Four),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OralVisDimensions.Two)
                ) {
                    items(s.data) { booking ->
                        OralVisCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookingSelected(booking.id) }
                        ) {
                            AvatarInfoRow(
                                primaryText = booking.patientName,
                                secondaryText = "${booking.appointmentDate} · ${booking.slotTime}",
                                onMoreClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
