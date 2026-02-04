package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillScreen(
    bookingId: String,
    appointmentViewModel: com.oralvis.oralvisclient.ui.viewmodel.AppointmentViewModel = viewModel(
        factory = OralVisViewModelFactory(AppGraph)
    ),
    onMarkedPaid: () -> Unit,
    onBack: () -> Unit
) {
    val actionState by appointmentViewModel.actionState.collectAsState()
    var hasMarkedPaid by remember { mutableStateOf(false) }

    LaunchedEffect(actionState) {
        if (hasMarkedPaid && actionState is UiState.Success) onMarkedPaid()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Add Bill / Mark paid", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(OralVisDimensions.Four)
        ) {
            Text(
                text = "Mark this appointment as paid to record payment.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = OralVisDimensions.Two)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (hasMarkedPaid) {
                when (actionState) {
                    is UiState.Loading -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OralVisPrimary)
                    }
                    is UiState.Error -> Text(
                        text = (actionState as UiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(OralVisDimensions.Two)
                    )
                    else -> { }
                }
            }
            PrimaryGradientButton(
                text = "Mark as paid",
                onClick = {
                    hasMarkedPaid = true
                    appointmentViewModel.markPaid(bookingId)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !hasMarkedPaid || actionState !is UiState.Loading
            )
        }
    }
}
