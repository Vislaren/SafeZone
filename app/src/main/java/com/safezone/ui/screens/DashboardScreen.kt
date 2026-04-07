package com.safezone.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safezone.ui.theme.SafeZoneColors
import com.safezone.viewmodel.DashboardState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    dashboardState: DashboardState,
    isServiceRunning: Boolean,
    onToggleService: (Boolean) -> Unit,
    onToggleMovement: (Boolean) -> Unit,
    onSosTrigger: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Live clock
    var timeNow by remember { mutableStateOf("14:02:44") }
    LaunchedEffect(Unit) {
        while (true) {
            timeNow = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    // Pulse animation for active indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_alpha"
    )
    val arcAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ), label = "arc"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BackgroundDark)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        // ── Top Bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(SafeZoneColors.OrangeCore, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◈", color = Color.Black, fontSize = 12.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "SAFEZONE",
                    color       = SafeZoneColors.OrangeCore,
                    fontWeight  = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontSize    = 16.sp
                )
            }
            Icon(
                Icons.Default.Tune,
                contentDescription = "Controls",
                tint     = SafeZoneColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        // ── Core Status Circle ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val orange = SafeZoneColors.OrangeCore
            val orangeDim = SafeZoneColors.OrangeDim

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .drawBehind {
                        val strokeW = 8.dp.toPx()
                        val radius  = (size.minDimension - strokeW) / 2f
                        val center  = Offset(size.width / 2f, size.height / 2f)
                        // Background arc
                        drawArc(
                            color       = orangeDim,
                            startAngle  = -90f,
                            sweepAngle  = 360f,
                            useCenter   = false,
                            topLeft     = Offset(center.x - radius, center.y - radius),
                            size        = Size(radius * 2, radius * 2),
                            style       = Stroke(strokeW, cap = StrokeCap.Round)
                        )
                        // Animated progress arc (75% filled)
                        drawArc(
                            brush       = Brush.sweepGradient(
                                listOf(orange.copy(alpha = 0.3f), orange),
                                center = Offset(size.width / 2f, size.height / 2f)
                            ),
                            startAngle  = -90f,
                            sweepAngle  = 270f,
                            useCenter   = false,
                            topLeft     = Offset(center.x - radius, center.y - radius),
                            size        = Size(radius * 2, radius * 2),
                            style       = Stroke(strokeW, cap = StrokeCap.Round)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint     = SafeZoneColors.OrangeCore,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "CORE STATUS",
                        color         = SafeZoneColors.TextDim,
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "System Active",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp
                    )
                }
            }
        }

        // ── Movement Sensors Card ─────────────────────────────────────────────
        SafeZoneCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Movement Sensors",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp
                        )
                        Text(
                            "High-fidelity haptic detection",
                            color    = SafeZoneColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(SafeZoneColors.SurfaceElevated, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = SafeZoneColors.TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(SafeZoneColors.OrangeDim, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "RELIABILITY 99.8%",
                            color     = SafeZoneColors.OrangeLight,
                            fontSize  = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Switch(
                        checked  = dashboardState.movementSensors,
                        onCheckedChange = onToggleMovement,
                        colors   = SwitchDefaults.colors(
                            checkedThumbColor  = Color.White,
                            checkedTrackColor  = SafeZoneColors.OrangeCore,
                            uncheckedThumbColor = SafeZoneColors.TextDim,
                            uncheckedTrackColor = SafeZoneColors.SurfaceElevated
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Hardware Matrix Card ───────────────────────────────────────────────
        SafeZoneCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "HARDWARE MATRIX",
                    color         = SafeZoneColors.TextDim,
                    fontSize      = 10.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(10.dp))
                HardwareRow("Microphone", Icons.Default.Mic, SafeZoneColors.StatusBlue)
                HorizontalDivider(color = SafeZoneColors.Divider, modifier = Modifier.padding(vertical = 8.dp))
                HardwareRow("GPS Engine", Icons.Default.LocationOn, SafeZoneColors.OrangeCore)
                HorizontalDivider(color = SafeZoneColors.Divider, modifier = Modifier.padding(vertical = 8.dp))
                HardwareRow("Vision Stream", Icons.Default.Videocam, SafeZoneColors.StatusBlue)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Environment Risk ──────────────────────────────────────────────────
        SafeZoneCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "ENVIRONMENT",
                        color         = SafeZoneColors.TextDim,
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        dashboardState.environmentRisk,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 22.sp
                    )
                }
                Icon(
                    Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    tint     = SafeZoneColors.TextDim.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Real-Time Stream Card ─────────────────────────────────────────────
        SafeZoneCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(SafeZoneColors.OrangeCore.copy(alpha = pulseAlpha), CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "REAL-TIME STREAM",
                        color         = SafeZoneColors.TextDim,
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                StreamLogRow(timeNow, "PING_RESP_SUCCESS", "LATENCY  ${dashboardState.latencyMs}ms")
                Spacer(Modifier.height(4.dp))
                StreamLogRow(
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - 1000)),
                    "SENSOR_CALIBRATED_HEALED",
                    "SECURE"
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Vault Auto-Lock Banner ─────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard),
            border   = BorderStroke(1.dp, SafeZoneColors.OrangeCore.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SafeZoneColors.SurfaceElevated, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SafeZoneColors.OrangeCore)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Vault Auto-Lock Active",
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp
                    )
                    Text(
                        "Your secure storage will engage in 4 minutes of inactivity. Access remains open for currently validated sessions.",
                        color    = SafeZoneColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ─── Reusable Components ──────────────────────────────────────────────────────
@Composable
fun SafeZoneCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard)
    ) { content() }
}

@Composable
private fun HardwareRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, dotColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = dotColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
    }
}

@Composable
private fun StreamLogRow(time: String, event: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "$time // $event",
            color      = SafeZoneColors.TextSecondary,
            fontSize   = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            status,
            color    = SafeZoneColors.OrangeCore,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
