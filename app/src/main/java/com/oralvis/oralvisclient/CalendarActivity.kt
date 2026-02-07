package com.oralvis.oralvisclient

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.ui.screens.CalendarScreen
import com.oralvis.oralvisclient.ui.theme.OralvisClientTheme

/**
 * Full-screen Calendar. No app bar, no bottom nav, no tabs.
 * Only a back button that finishes this activity and returns to MainActivity.
 */
class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            OralvisClientTheme {
                val factory = remember { OralVisViewModelFactory(AppGraph) }
                val viewModel: com.oralvis.oralvisclient.ui.viewmodel.CalendarViewModel =
                    viewModel(factory = factory)
                val clinicId = remember { AppGraph.sessionManager().getClinicId() }
                var selectedDate by remember { mutableStateOf<String?>(null) }

                Box(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                    CalendarScreen(
                        viewModel = viewModel,
                        clinicId = clinicId,
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        onAppointmentClick = { id ->
                            if (id.isNotBlank()) {
                                startActivity(
                                    Intent(this@CalendarActivity, MainActivity::class.java).apply {
                                        putExtra("open_booking_id", id)
                                    }
                                )
                                finish()
                            }
                        }
                    )
                    IconButton(
                        onClick = { finish() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                }
                }
            }
        }
    }
}
