package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phone by remember { mutableStateOf("9876512345") }
    var name by remember { mutableStateOf("Rahul Sharma") }
    var email by remember { mutableStateOf("rahul@example.com") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = SudhaniTheme.colors.background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo Badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SudhaniNavyDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "SudhaniHub",
                    tint = SudhaniGoldPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SudhaniHub",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = SudhaniTheme.colors.textPrimary
            )

            Text(
                text = "Sab Kuch, Jaldi Se",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = SudhaniGoldDark
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SudhaniTheme.colors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = if (!isOtpSent) "LOGIN / SIGN UP" else "VERIFY MOBILE OTP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )

                    Text(
                        text = if (!isOtpSent) 
                            "Enter your phone number to proceed" 
                        else 
                            "Enter 4-digit code sent to +91 $phone",
                        fontSize = 11.sp,
                        color = SudhaniTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    val authTextFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SudhaniTheme.colors.textPrimary,
                        unfocusedTextColor = SudhaniTheme.colors.textPrimary,
                        cursorColor = SudhaniGoldDark,
                        focusedContainerColor = SudhaniTheme.colors.surface,
                        unfocusedContainerColor = SudhaniTheme.colors.surface,
                        focusedPrefixColor = SudhaniTheme.colors.textPrimary,
                        unfocusedPrefixColor = SudhaniTheme.colors.textPrimary,
                        focusedBorderColor = SudhaniGoldPrimary,
                        unfocusedBorderColor = SudhaniTheme.colors.cardBorder
                    )

                    if (!isOtpSent) {
                        // Phone input
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number", color = SudhaniTheme.colors.textSecondary) },
                            prefix = { Text("+91 ") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = SudhaniGoldDark
                                )
                            },
                            colors = authTextFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Name input
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Full Name", color = SudhaniTheme.colors.textSecondary) },
                            colors = authTextFieldColors,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email input (optional)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email (Optional for e-receipts)", color = SudhaniTheme.colors.textSecondary) },
                            colors = authTextFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // OTP Input
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 4) otpCode = it },
                            label = { Text("4-Digit OTP", color = SudhaniTheme.colors.textSecondary) },
                            placeholder = { Text("Try 4819") },
                            colors = authTextFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input")
                        )

                        Surface(
                            color = SudhaniTheme.colors.chipBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniGoldPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "💡 Quick Demo OTP: Tap 'Auto-fill 4819' or type 4819",
                                fontSize = 11.sp,
                                color = SudhaniGoldDark,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable { otpCode = "4819" }
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!isOtpSent) {
                                if (phone.length < 10) {
                                    errorMessage = "Please enter a valid 10-digit mobile number"
                                } else {
                                    errorMessage = null
                                    isOtpSent = true
                                    otpCode = "4819" // auto-fill demo
                                }
                            } else {
                                if (otpCode.isNotEmpty()) {
                                    SudhaniRepository.loginOrRegisterCustomer(
                                        name = name.ifEmpty { "Rahul Sharma" },
                                        phone = "+91 $phone",
                                        email = email.ifEmpty { "rahul@example.com" }
                                    )
                                    onAuthSuccess()
                                } else {
                                    errorMessage = "Please enter the OTP"
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (!isOtpSent) "SEND OTP" else "VERIFY & CONTINUE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = SudhaniNavyDark
                        )
                    }

                    if (isOtpSent) {
                        TextButton(
                            onClick = { isOtpSent = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "CHANGE MOBILE NUMBER",
                                color = SudhaniGoldDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onAuthSuccess) {
                Text(
                    text = "CONTINUE AS GUEST",
                    color = SudhaniTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy",
                fontSize = 10.sp,
                color = SudhaniTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
