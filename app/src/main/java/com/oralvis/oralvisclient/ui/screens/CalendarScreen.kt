package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.ui.components.OralVisCard
import com.oralvis.oralvisclient.ui.components.StatusChip
import com.oralvis.oralvisclient.ui.theme.OralVisCalendarBackground
import com.oralvis.oralvisclient.ui.theme.OralVisCompleted
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnGoing
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary
import java.util.Calendar

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: com.oralvis.oralvisclient.ui.viewmodel.CalendarViewModel = viewModel(
        factory = OralVisViewModelFactory(AppGraph)
    ),
    clinicId: String?,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    onAppointmentClick: (String) -> Unit
) {
    var month by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var year by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    val dateState by viewModel.calendarDateState.collectAsState()

    LaunchedEffect(clinicId, selectedDate) {
        clinicId?.let { id ->
            selectedDate?.let { viewModel.loadCalendarDate(id, it) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(OralVisDimensions.Four)
    ) {
        OralVisCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = OralVisCalendarBackground
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${monthName(month)} $year",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OralVisOnSurface
                    )
                    Row {
                        listOf("Day", "Month").forEachIndexed { index, label ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                    .background(if (index == 0) OralVisPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    color = OralVisOnSurface
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(OralVisDimensions.Three))
                val daysOfWeek = listOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            color = OralVisOnSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(OralVisDimensions.Two))
                val daysInMonth = getDaysInMonth(year, month)
                val firstDay = firstDayOfWeek(year, month)
                val totalCells = daysInMonth + firstDay
                val rows = (totalCells + 6) / 7
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(rows) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            repeat(7) { col ->
                                val index = row * 7 + col
                                val day = index - firstDay + 1
                                val isCurrentMonth = day in 1..daysInMonth
                                val dateStr = if (isCurrentMonth) String.format("%04d-%02d-%02d", year, month, day) else null
                                val isSelected = dateStr == selectedDate
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) Modifier.background(OralVisPrimary.copy(alpha = 0.3f))
                                            else Modifier
                                        )
                                        .then(
                                            if (dateStr != null) Modifier.clickable { onDateSelected(dateStr) }
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCurrentMonth) {
                                        Text(
                                            text = "$day",
                                            fontSize = 14.sp,
                                            color = if (isSelected) OralVisPrimary else OralVisOnSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(OralVisDimensions.Four))
        when (val s = dateState) {
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
                verticalArrangement = Arrangement.spacedBy(OralVisDimensions.Two),
                contentPadding = PaddingValues(bottom = OralVisDimensions.Eight)
            ) {
                items(s.data.appointments) { appt ->
                    val isCompleted = appt.slotTime.contains("11:10")
                    OralVisCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isCompleted) OralVisCompleted else OralVisOnGoing)
                                .padding(OralVisDimensions.Three)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = appt.patientName.uppercase(),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${appt.slotTime}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${appt.durationMinutes ?: 30} mins",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                            StatusChip(
                                text = if (isCompleted) "Completed" else "On going",
                                isCompleted = isCompleted,
                                isOnGoing = !isCompleted
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun monthName(month: Int): String = when (month) {
    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"; 5 -> "May"; 6 -> "June"
    7 -> "July"; 8 -> "August"; 9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
    else -> ""
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    val c = Calendar.getInstance()
    c.set(year, month - 1, 1)
    return c.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun firstDayOfWeek(year: Int, month: Int): Int {
    val c = Calendar.getInstance()
    c.set(year, month - 1, 1)
    var day = c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
    if (day < 0) day += 7
    return day
}
