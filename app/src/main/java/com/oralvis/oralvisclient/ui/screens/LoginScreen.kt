package com.oralvis.oralvisclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.ui.components.PrimaryGradientButton
import com.oralvis.oralvisclient.ui.theme.OralVisDimensions
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurface
import com.oralvis.oralvisclient.ui.theme.OralVisOnSurfaceVariant
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary
import com.oralvis.oralvisclient.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hasAttempted by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is UiState.Success -> if (state.data != null) onLoginSuccess()
            else -> { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(OralVisDimensions.Four)
    ) {
        Spacer(modifier = Modifier.height(OralVisDimensions.Eight))
        Text(
            text = "Sign in",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OralVisOnSurface
        )
        Text(
            text = "Use email or phone and password to sign in",
            fontSize = 14.sp,
            color = OralVisOnSurfaceVariant,
            modifier = Modifier.padding(top = OralVisDimensions.One)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Six))

        OutlinedTextField(
            value = emailOrPhone,
            onValueChange = { emailOrPhone = it },
            label = { Text("Email or phone") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(OralVisDimensions.Two))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(OralVisDimensions.CardCornerRadius),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        val isEmail = emailOrPhone.contains("@")
        val errorMessage = (loginState as? UiState.Error)?.message
        val isLoading = hasAttempted && loginState is UiState.Loading

        if (errorMessage != null && hasAttempted) {
            Spacer(modifier = Modifier.height(OralVisDimensions.Two))
            Text(
                text = errorMessage,
                color = Color(0xFFB00020),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(OralVisDimensions.Four))
        PrimaryGradientButton(
            text = "Sign in",
            onClick = {
                if (emailOrPhone.isBlank() || password.isBlank()) return@PrimaryGradientButton
                hasAttempted = true
                viewModel.login(
                    phoneNo = if (isEmail) null else emailOrPhone.trim(),
                    email = if (isEmail) emailOrPhone.trim() else null,
                    password = password
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading
        )
        if (isLoading) {
            Spacer(modifier = Modifier.height(OralVisDimensions.Two))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(OralVisDimensions.Two),
                    color = OralVisPrimary
                )
            }
        }
    }
}
