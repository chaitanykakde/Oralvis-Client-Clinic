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
import com.oralvis.oralvisclient.ui.components.AvatarInfoRow
import com.oralvis.oralvisclient.ui.components.OralVisCard
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

@Composable
fun PatientsListScreen(
    modifier: Modifier = Modifier,
    viewModel: com.oralvis.oralvisclient.ui.viewmodel.AppointmentViewModel = viewModel(
        factory = OralVisViewModelFactory(AppGraph)
    ),
    clinicId: String?,
    onPatientClick: (String) -> Unit,
    onMoreClick: (String) -> Unit = {}
) {
    val state by viewModel.appointmentsState.collectAsState()

    LaunchedEffect(clinicId) {
        clinicId?.let { viewModel.loadAppointments(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(OralVisDimensions.Four)
    ) {
        Text(
            text = "Patients",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(vertical = OralVisDimensions.Three)
        )
        when (val s = state) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(OralVisDimensions.Six),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = OralVisPrimary) }
            is UiState.Error -> Text(
                text = s.message,
                color = Color.Red,
                modifier = Modifier.padding(OralVisDimensions.Four)
            )
            is UiState.Success -> LazyColumn(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OralVisDimensions.Two),
                contentPadding = PaddingValues(bottom = OralVisDimensions.Eight)
            ) {
                items(s.data) { booking ->
                    OralVisCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = OralVisDimensions.Half)
                            .clickable { onPatientClick(booking.patientId ?: booking.id) }
                    ) {
                        AvatarInfoRow(
                            primaryText = booking.patientName,
                            secondaryText = booking.status.name.lowercase().replaceFirstChar { it.uppercase() },
                            onMoreClick = { onMoreClick(booking.id) }
                        )
                    }
                }
            }
        }
    }
}
