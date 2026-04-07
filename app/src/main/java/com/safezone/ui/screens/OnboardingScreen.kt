package com.safezone.ui.screens

import androidx.biometric.BiometricManager
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.safezone.ui.theme.SafeZoneColors
import com.safezone.utils.BiometricHelper
import com.safezone.viewmodel.AuthState
import com.safezone.viewmodel.AuthStep

@Composable
fun OnboardingScreen(
    authState: AuthState,
    onPhoneChange: (String) -> Unit,
    onInitiateLink: () -> Unit,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onBiometricDone: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Brand Header ──────────────────────────────────────────────────
            SafeZoneBrandHeader()
            Spacer(Modifier.height(40.dp))

            // ── Step 1: Phone ─────────────────────────────────────────────────
            PhoneInputSection(
                authState       = authState,
                onPhoneChange   = onPhoneChange,
                onInitiateLink  = onInitiateLink
            )

            // ── Divider ───────────────────────────────────────────────────────
            AnimatedVisibility(visible = authState.currentStep.ordinal >= AuthStep.OTP.ordinal) {
                Column {
                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(color = SafeZoneColors.Divider)
                    Spacer(Modifier.height(32.dp))

                    // ── Step 2: OTP ───────────────────────────────────────────
                    OtpSection(
                        authState  = authState,
                        onOtpChange = onOtpChange,
                        onVerify   = onVerifyOtp
                    )
                }
            }

            // ── Step 3: Biometric ─────────────────────────────────────────────
            AnimatedVisibility(visible = authState.currentStep == AuthStep.BIOMETRIC) {
                Column {
                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(color = SafeZoneColors.Divider)
                    Spacer(Modifier.height(32.dp))
                    BiometricSection(
                        onBiometricDone = {
                            if (BiometricHelper.canAuthenticate(context)) {
                                BiometricHelper.showPrompt(
                                    activity    = context as FragmentActivity,
                                    title       = "SECURE ENCLAVE",
                                    subtitle    = "Biometric Enrollment",
                                    description = "Register your biometric for secure access",
                                    onSuccess   = onBiometricDone
                                )
                            } else {
                                onBiometricDone()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Footer ────────────────────────────────────────────────────────
            Text(
                "• SYSTEM ARMED    V2.4.0-STABLE",
                color     = SafeZoneColors.OrangeCore,
                fontSize  = 10.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "© 2024 SAFEZONE HEAVY INDUSTRIES",
                color    = SafeZoneColors.TextDim,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─── Brand Header ─────────────────────────────────────────────────────────────
@Composable
private fun SafeZoneBrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(SafeZoneColors.OrangeCore, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("◈", color = Color.Black, fontSize = 14.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "SAFEZONE",
            color       = SafeZoneColors.OrangeCore,
            fontWeight  = FontWeight.Black,
            letterSpacing = 3.sp,
            fontSize    = 18.sp
        )
    }
}

// ─── Phone Input Section ──────────────────────────────────────────────────────
@Composable
private fun PhoneInputSection(
    authState: AuthState,
    onPhoneChange: (String) -> Unit,
    onInitiateLink: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "AUTHENTICATION LAYER 01",
            color         = SafeZoneColors.TextDim,
            fontSize      = 11.sp,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        Row {
            Text("Enter ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
            Text("Mobile ", color = SafeZoneColors.OrangeCore, fontWeight = FontWeight.Black, fontSize = 28.sp)
            Text("ID", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
    "Enter your 9-digit Cameroon mobile number (MTN, Orange or Camtel).",
    color    = SafeZoneColors.TextSecondary,
    fontSize = 14.sp
)
        Spacer(Modifier.height(24.dp))

        // Phone Input Field
        OutlinedTextField(
            value         = authState.phoneNumber,
            onValueChange = onPhoneChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text("|", color = SafeZoneColors.TextDim) },
            prefix = {
    Text("+237 ", color = SafeZoneColors.OrangeCore, fontWeight = FontWeight.SemiBold)
},
keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = SafeZoneColors.OrangeCore,
                unfocusedBorderColor = SafeZoneColors.Divider,
                cursorColor          = SafeZoneColors.OrangeCore,
                focusedTextColor     = Color.White,
                unfocusedTextColor   = Color.White,
                unfocusedContainerColor = SafeZoneColors.SurfaceCard,
                focusedContainerColor   = SafeZoneColors.SurfaceCard
            ),
            shape = RoundedCornerShape(12.dp)
        )

        authState.errorMessage?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(err, color = SafeZoneColors.StatusRed, fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick  = onInitiateLink,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = SafeZoneColors.OrangeCore),
            enabled  = !authState.isLoading
        ) {
            if (authState.isLoading && authState.currentStep == AuthStep.PHONE) {
                CircularProgressIndicator(
                    color    = Color.Black,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "INITIATE LINK",
                    color         = Color.Black,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontSize      = 13.sp
                )
            }
        }
    }
}

// ─── OTP Section ─────────────────────────────────────────────────────────────
@Composable
private fun OtpSection(
    authState: AuthState,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(44) }
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            kotlinx.coroutines.delay(1000)
            timeLeft--
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "SEQUENCE VALIDATION",
            color = SafeZoneColors.TextDim,
            fontSize = 11.sp,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Transmission Sent",
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 26.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Input the 6-digit cryptographic key sent to your device.",
            color    = SafeZoneColors.TextSecondary,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(24.dp))

        // OTP digit boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(6) { idx ->
                val char = otp.getOrNull(idx)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(
                            if (char != null) SafeZoneColors.OrangeDim else SafeZoneColors.SurfaceCard,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (char != null) SafeZoneColors.OrangeCore else SafeZoneColors.Divider,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        char?.toString() ?: "•",
                        color      = if (char != null) Color.White else SafeZoneColors.TextDim,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                }
            }
        }

        // Hidden text field driving OTP
        OutlinedTextField(
            value         = otp,
            onValueChange = { new ->
                if (new.length <= 6 && new.all { it.isDigit() }) {
                    otp = new
                    onOtpChange(new)
                    if (new.length == 6) onVerify()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor   = Color.Transparent
            )
        )

        Spacer(Modifier.height(12.dp))
        Text(
            "RESEND CODE (0:${timeLeft.toString().padStart(2, '0')})",
            color         = SafeZoneColors.TextDim,
            fontSize      = 11.sp,
            letterSpacing = 1.sp,
            modifier      = Modifier.fillMaxWidth(),
            textAlign     = TextAlign.Center
        )
    }
}

// ─── Biometric Section ────────────────────────────────────────────────────────
@Composable
private fun BiometricSection(onBiometricDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fingerprint icon with glow
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(SafeZoneColors.OrangeGlow, Color.Transparent)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(SafeZoneColors.SurfaceElevated, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Biometric",
                            tint     = SafeZoneColors.OrangeCore,
                            modifier = Modifier.size(36.dp)
                        )
                        // Checkmark overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .background(SafeZoneColors.OrangeCore, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Identity Verified",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 20.sp
                )
                Text(
                    "Biometric Enrollment Successful",
                    color    = SafeZoneColors.TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(20.dp))

                // Secure Enclave status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SafeZoneColors.SurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔒", fontSize = 16.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "SECURE\nENCLAVE",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        "ENCRYPTED",
                        color         = SafeZoneColors.OrangeCore,
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick  = onBiometricDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafeZoneColors.OrangeCore)
        ) {
            Text(
                "ACCESS DASHBOARD",
                color         = Color.Black,
                fontWeight    = FontWeight.Black,
                letterSpacing = 3.sp,
                fontSize      = 13.sp
            )
        }
    }
}
