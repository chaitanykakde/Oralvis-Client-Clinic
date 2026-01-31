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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant

@Composable
fun AppointmentFormScreen(
    modifier: Modifier = Modifier,
    onSaveNext: () -> Unit = {}
) {
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
            value = "",
            onValueChange = {},
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = "",
            onValueChange = {},
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
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OralVisDimensions.Two)
        ) {
            listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach { type ->
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(OralVisDimensions.ChipCornerRadius),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                ) {
                    Text(
                        text = type,
                        modifier = Modifier.padding(horizontal = OralVisDimensions.Two, vertical = OralVisDimensions.One),
                        fontSize = 12.sp,
                        color = OralVisOnSurface
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Language") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = "",
            onValueChange = {},
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
