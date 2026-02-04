package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPrescriptionScreen(
    bookingId: String,
    clinicalViewModel: com.oralvis.oralvisclient.ui.viewmodel.ClinicalViewModel = viewModel(
        factory = OralVisViewModelFactory(AppGraph)
    ),
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var prescriptionsText by remember { mutableStateOf("") }
    var hasSaved by remember { mutableStateOf(false) }
    val saveState by clinicalViewModel.saveRecordState.collectAsState()

    LaunchedEffect(saveState) {
        if (hasSaved && saveState is UiState.Success) onSaved()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Add Prescription", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(OralVisDimensions.Four)
        ) {
            OutlinedTextField(
                value = prescriptionsText,
                onValueChange = { prescriptionsText = it },
                label = { Text("Prescription details") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(OralVisDimensions.Two))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(OralVisDimensions.Four))
            if (hasSaved) {
                when (saveState) {
                    is UiState.Loading -> Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = OralVisPrimary)
                    }
                    is UiState.Error -> Text(
                        text = (saveState as UiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(OralVisDimensions.Two)
                    )
                    else -> { }
                }
            }
            PrimaryGradientButton(
                text = "Save",
                onClick = {
                    hasSaved = true
                    val prescriptionList = prescriptionsText.split("\n").filter { it.isNotBlank() }
                        .map { line -> mapOf("text" to line) }
                    clinicalViewModel.saveClinicalRecord(
                        bookingId = bookingId,
                        notes = notes.ifBlank { null },
                        prescriptions = if (prescriptionList.isEmpty()) null else prescriptionList
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !hasSaved || saveState !is UiState.Loading
            )
        }
    }
}
