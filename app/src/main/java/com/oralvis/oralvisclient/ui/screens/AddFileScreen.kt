package com.oralvis.oralvisclient.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFileScreen(
    bookingId: String,
    clinicalViewModel: com.oralvis.oralvisclient.ui.viewmodel.ClinicalViewModel = viewModel(
        factory = OralVisViewModelFactory(AppGraph)
    ),
    onFileUploaded: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uploadState by clinicalViewModel.uploadFileState.collectAsState()
    var hasUploaded by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            hasUploaded = true
            context.contentResolver.openInputStream(uri)?.use { input ->
                val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
                tempFile.outputStream().use { output -> input.copyTo(output) }
                clinicalViewModel.uploadFile(bookingId, tempFile)
            }
        }
    }

    LaunchedEffect(uploadState) {
        if (hasUploaded && uploadState is UiState.Success) onFileUploaded()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Add File", fontWeight = FontWeight.Bold) },
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
                text = "Select a file to attach to this appointment.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = OralVisDimensions.Two)
            )
            Spacer(modifier = Modifier.height(OralVisDimensions.Four))
            when (uploadState) {
                is UiState.Loading -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = OralVisPrimary)
                }
                is UiState.Error -> Text(
                    text = (uploadState as UiState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(OralVisDimensions.Two)
                )
                else -> { }
            }
            PrimaryGradientButton(
                text = "Select file",
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uploadState !is UiState.Loading
            )
        }
    }
}
