package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary

private val BLOOD_TYPES = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

@Composable
fun AppointmentFormScreen(
    modifier: Modifier = Modifier,
    onSaveNext: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var selectedBloodType by remember { mutableStateOf<String?>(null) }
    var language by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(OralVisDimensions.Four)
    ) {
        Text(
            text = "Appointment Details",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(vertical = OralVisDimensions.Three)
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { dateOfBirth = it },
            label = { Text("Date of birth") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        Text(
            text = "Blood Type",
            fontSize = 14.sp,
            color = OralVisOnSurfaceVariant,
            modifier = Modifier.padding(vertical = OralVisDimensions.One)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OralVisDimensions.Two)
        ) {
            BLOOD_TYPES.forEach { type ->
                val selected = selectedBloodType == type
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(OralVisDimensions.ChipCornerRadius))
                        .border(
                            width = 1.dp,
                            color = if (selected) OralVisPrimary else Color.Gray,
                            shape = RoundedCornerShape(OralVisDimensions.ChipCornerRadius)
                        )
                        .clickable { selectedBloodType = type }
                        .padding(horizontal = OralVisDimensions.Two, vertical = OralVisDimensions.One),
                    color = if (selected) OralVisPrimary.copy(alpha = 0.15f) else Color.Transparent,
                    shape = RoundedCornerShape(OralVisDimensions.ChipCornerRadius)
                ) {
                    Text(
                        text = type,
                        fontSize = 12.sp,
                        color = if (selected) OralVisPrimary else OralVisOnSurface
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = language,
            onValueChange = { language = it },
            label = { Text("Language") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Four))
        PrimaryGradientButton(
            text = "Save & Next",
            onClick = onSaveNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
