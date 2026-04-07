package com.safezone.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.safezone.data.db.VaultFile
import com.safezone.ui.theme.SafeZoneColors

// ─── SOS Screen ───────────────────────────────────────────────────────────────
@Composable
fun SosScreen(
    isServiceRunning: Boolean,
    onSosTrigger: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.12f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "sos_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue  = 0.6f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BackgroundDark)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(SafeZoneColors.OrangeCore, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("◈", color = Color.Black, fontSize = 12.sp) }
            Spacer(Modifier.width(8.dp))
            Text(
                "SAFEZONE",
                color       = SafeZoneColors.OrangeCore,
                fontWeight  = FontWeight.Black,
                letterSpacing = 3.sp,
                fontSize    = 16.sp
            )
        }

        Spacer(Modifier.height(48.dp))

        Text(
            "EMERGENCY",
            color         = Color.White,
            fontWeight    = FontWeight.Black,
            fontSize      = 14.sp,
            letterSpacing = 4.sp
        )
        Text(
            "PROTOCOL",
            color         = SafeZoneColors.OrangeCore,
            fontWeight    = FontWeight.Black,
            fontSize      = 32.sp,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(40.dp))

        // SOS Button
        Box(contentAlignment = Alignment.Center) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(
                        SafeZoneColors.StatusRed.copy(alpha = glowAlpha),
                        CircleShape
                    )
            )
            // Mid ring
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        SafeZoneColors.StatusRed.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
            // SOS button
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFF4444), Color(0xFFCC0000))
                        ),
                        CircleShape
                    )
                    .clickable(onClick = onSosTrigger),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "SOS",
                        tint     = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "SOS",
                        color      = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize   = 24.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Press and hold to dispatch emergency alert\nto all 5 contacts with GPS coordinates",
            color     = SafeZoneColors.TextSecondary,
            fontSize  = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))
        HorizontalDivider(color = SafeZoneColors.Divider)
        Spacer(Modifier.height(24.dp))

        // Service toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            colors   = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (isServiceRunning) "MONITORING ACTIVE" else "MONITORING INACTIVE",
                        color         = if (isServiceRunning) SafeZoneColors.StatusGreen else SafeZoneColors.StatusRed,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        if (isServiceRunning) "Background service running" else "Tap to arm system",
                        color    = SafeZoneColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked         = isServiceRunning,
                    onCheckedChange = { if (it) onStartService() else onStopService() },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor  = Color.White,
                        checkedTrackColor  = SafeZoneColors.OrangeCore,
                        uncheckedThumbColor = SafeZoneColors.TextDim,
                        uncheckedTrackColor = SafeZoneColors.SurfaceElevated
                    )
                )
            }
        }
    }
}

// ─── Delete Authorization Overlay ─────────────────────────────────────────────
@Composable
fun DeleteAuthOverlay(
    file: VaultFile?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (file == null) return

    var pin by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SafeZoneColors.OverlayDark),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Red delete icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(SafeZoneColors.DangerRed, RoundedCornerShape(16.dp))
                            .border(4.dp, SafeZoneColors.DangerRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "DELETE AUTHORIZATION",
                        color         = SafeZoneColors.OrangeLight,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 20.sp,
                        letterSpacing = 2.sp,
                        textAlign     = TextAlign.Center
                    )
                    Text(
                        "LEVEL 4 SECURITY CLEARANCE REQUIRED",
                        color         = SafeZoneColors.TextDim,
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "You are attempting to permanently purge ",
                        color     = SafeZoneColors.TextSecondary,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        file.fileName,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                    Text(
                        ". This action is irreversible and will result in total data loss.",
                        color     = SafeZoneColors.TextSecondary,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // Fingerprint hold button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SafeZoneColors.SurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(SafeZoneColors.OrangeGlow, Color.Transparent)
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = "Biometric",
                                    tint     = SafeZoneColors.OrangeCore,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "HOLD TO AUTHENTICATE",
                                color         = SafeZoneColors.OrangeCore,
                                fontSize      = 11.sp,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "OR ENTER SECURE PIN",
                        color         = SafeZoneColors.TextDim,
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    // PIN input row
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(4) { idx ->
                            val filled = idx < pin.length
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        if (filled) SafeZoneColors.OrangeDim else SafeZoneColors.BackgroundDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (filled) SafeZoneColors.OrangeCore else SafeZoneColors.Divider,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        if (pin.length < 4) pin += "•"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (filled) "•" else "•",
                                    color    = if (filled) Color.White else SafeZoneColors.TextDim,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick  = onConfirm,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = SafeZoneColors.OrangeCore)
                    ) {
                        Text(
                            "CONFIRM DELETION",
                            color         = Color.Black,
                            fontWeight    = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize      = 13.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = onDismiss) {
                        Text(
                            "CANCEL PROTOCOL",
                            color         = SafeZoneColors.TextDim,
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            fontSize      = 12.sp
                        )
                    }
                }
            }
        }
    }
}
