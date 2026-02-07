package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.domain.model.CalendarDayAppointment
import com.oralvis.oralvisclient.ui.theme.OralVisAgendaBackground
import com.oralvis.oralvisclient.ui.theme.OralVisCalendarHeaderBlue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.ui.theme.OralVisCompletedTeal
import com.oralvis.oralvisclient.ui.theme.OralVisOnPrimary
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary
import com.oralvis.oralvisclient.ui.theme.OralVisTimelineLine
import java.util.Calendar

private const val TIMELINE_START_HOUR = 8
private const val TIMELINE_END_HOUR = 20
private const val MINUTES_PER_HOUR = 60
private const val HOURS_DISPLAY = TIMELINE_END_HOUR - TIMELINE_START_HOUR
private const val TOTAL_DISPLAY_MINUTES = HOURS_DISPLAY * MINUTES_PER_HOUR

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
    var internalSelectedDate by remember { mutableStateOf(selectedDate) }
    val dateState by viewModel.calendarDateState.collectAsState()
    val effectiveDate = internalSelectedDate ?: run {
        String.format("%04d-%02d-%02d", year, month, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    }

    LaunchedEffect(clinicId, effectiveDate) {
        clinicId?.let { id -> viewModel.loadCalendarDate(id, effectiveDate) }
    }
    LaunchedEffect(selectedDate) {
        internalSelectedDate = selectedDate
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OralVisAgendaBackground)
    ) {
        CalendarHeader(
            month = month,
            year = year,
            selectedDate = effectiveDate,
            onDateSelected = { date ->
                internalSelectedDate = date
                onDateSelected(date)
            },
            onMonthYearChange = { m, y -> month = m; year = y }
        )

        when (val s = dateState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = OralVisPrimary) }
            is UiState.Error -> Text(
                text = s.message,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
            is UiState.Success -> AgendaSection(
                appointments = s.data.appointments,
                onAppointmentClick = onAppointmentClick
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    month: Int,
    year: Int,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onMonthYearChange: (Int, Int) -> Unit
) {
    val bottomRadius = 32.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = bottomRadius, bottomEnd = bottomRadius))
            .background(OralVisCalendarHeaderBlue)
            .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${monthName(month)} $year",
                    color = OralVisOnPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Month year",
                    tint = OralVisOnPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            YearPill(
                currentYear = year,
                onYearSelected = { onMonthYearChange(month, it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        val daysOfWeek = listOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    color = OralVisOnPrimary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        val daysInMonth = getDaysInMonth(year, month)
        val firstDay = firstDayOfWeek(year, month)
        val totalCells = daysInMonth + firstDay
        val rows = (totalCells + 6) / 7
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) Modifier.background(Color.White)
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
                                    fontSize = 15.sp,
                                    color = if (isSelected) OralVisCalendarHeaderBlue else OralVisOnPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearPill(
    currentYear: Int,
    onYearSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(currentYear - 1, currentYear, currentYear + 1).forEach { y ->
            val selected = y == currentYear
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .then(if (selected) Modifier.background(Color.White.copy(alpha = 0.35f)) else Modifier)
                    .clickable { onYearSelected(y) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$y",
                    color = OralVisOnPrimary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun AgendaSection(
    appointments: List<CalendarDayAppointment>,
    onAppointmentClick: (String) -> Unit
) {
    val timeLabelWidth = 56.dp
    val contentPaddingStart = 16.dp
    val hourHeightDp = 72.dp
    val totalHeightDp = hourHeightDp * HOURS_DISPLAY

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OralVisAgendaBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.height(totalHeightDp).fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    repeat(HOURS_DISPLAY) { i ->
                        val hour = TIMELINE_START_HOUR + i
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(hourHeightDp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = when {
                                    hour == 0 -> "12:00 am"
                                    hour < 12 -> "${hour}:00 am"
                                    hour == 12 -> "12:00 pm"
                                    else -> "${hour - 12}:00 pm"
                                },
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.width(timeLabelWidth).padding(top = 2.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .padding(end = 16.dp)
                                    .background(OralVisTimelineLine)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = timeLabelWidth + contentPaddingStart, end = 16.dp)
                ) {
                    appointments.forEachIndexed { index, appt ->
                        val (startMinutes, durationMins) = parseSlotTime(appt.slotTime, appt.durationMinutes)
                        val startOffsetMinutes = (startMinutes / 60 - TIMELINE_START_HOUR) * 60 + (startMinutes % 60)
                        if (startOffsetMinutes < 0 || startOffsetMinutes >= TOTAL_DISPLAY_MINUTES) return@forEachIndexed
                        val topDp = (startOffsetMinutes / 60f * 72f).dp
                        val heightDp = (durationMins / 60f * 72f).dp.coerceAtLeast(48.dp)
                        val isCompleted = appt.slotTime.contains("11:10") || index == 0
                        AppointmentCard(
                            appointment = appt,
                            isCompleted = isCompleted,
                            modifier = Modifier
                                .offset(y = topDp)
                                .height(heightDp)
                                .fillMaxWidth(),
                            onClick = { onAppointmentClick("") }
                        )
                    }
                }

                CurrentTimeIndicator(
                    modifier = Modifier.fillMaxSize(),
                    timeLabelWidth = timeLabelWidth
                )
            }
        }
    }
}

@Composable
private fun CurrentTimeIndicator(
    modifier: Modifier = Modifier,
    timeLabelWidth: androidx.compose.ui.unit.Dp = 56.dp
) {
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
    val offsetDp = ((hour - TIMELINE_START_HOUR) * 72f).dp
    if (hour < TIMELINE_START_HOUR || hour >= TIMELINE_END_HOUR) return
    Row(
        modifier = modifier
            .padding(top = offsetDp - 6.dp)
            .fillMaxWidth()
            .height(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = timeLabelWidth - 4.dp)
                .size(8.dp)
                .background(Color.Black, CircleShape)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(end = 16.dp)
                .background(Color.Black)
        )
    }
}

@Composable
private fun AppointmentCard(
    appointment: CalendarDayAppointment,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardColor = if (isCompleted) OralVisCompletedTeal else OralVisCalendarHeaderBlue
    Box(
        modifier = modifier
            .padding(vertical = 2.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.patientName.uppercase(),
                    color = OralVisOnPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appointment.patientName.take(1).uppercase(),
                        color = OralVisOnPrimary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appointment.slotTime,
                color = OralVisOnPrimary.copy(alpha = 0.95f),
                fontSize = 12.sp
            )
            Text(
                text = "${appointment.durationMinutes ?: 30} mins",
                color = OralVisOnPrimary.copy(alpha = 0.85f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isCompleted) "Completed" else "On going",
                color = OralVisOnPrimary,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun parseSlotTime(slotTime: String, durationMinutes: Int?): Pair<Int, Int> {
    val duration = durationMinutes ?: 30
    val parts = slotTime.split("-", "–").map { it.trim() }
    if (parts.size < 2) return Pair(8 * 60, duration)
    val startStr = parts[0]
    val startMinutes = parseTimeToMinutes(startStr)
    return Pair(startMinutes, duration)
}

private fun parseTimeToMinutes(s: String): Int {
    val lower = s.lowercase().trim()
    val cleaned = lower.replace("am", "").replace("pm", "").trim()
    val parts = cleaned.split(":", " ").filter { it.isNotBlank() }
    if (parts.isEmpty()) return 8 * 60
    val hour = parts[0].toIntOrNull() ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val hasPm = lower.contains("pm")
    val hasAm = lower.contains("am")
    val hour24 = when {
        hasPm && hour in 1..11 -> hour + 12
        hasPm && hour == 12 -> 12
        hasAm && hour == 12 -> 0
        hasAm && hour in 1..11 -> hour
        !hasAm && !hasPm && hour in 0..23 -> hour
        else -> hour.coerceIn(0, 23)
    }
    return (hour24.coerceIn(0, 23)) * 60 + minute.coerceIn(0, 59)
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
