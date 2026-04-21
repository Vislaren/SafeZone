package com.safezone.app.ui.screens.sos

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safezone.app.ui.components.OutlinedPill
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.SosActiveViewModel
import com.safezone.app.utils.Formatters

@Composable
fun SosActiveScreen(
    onEnded: () -> Unit,
    vm: SosActiveViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BgBase)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        OutlinedPill(text = "SOS ACTIVE", borderColor = SafeZoneColors.BrandRed)
        Spacer(Modifier.height(40.dp))

        val pulse = rememberInfiniteTransition(label = "timer-glow")
        val glow by pulse.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
            label = "glow"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(80.dp))
                .background(SafeZoneColors.BrandRedDeep.copy(alpha = glow * 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Formatters.formatTimer(s.elapsedSeconds),
                color = SafeZoneColors.BrandRedSoft,
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "TIME ELAPSED",
            color = SafeZoneColors.BrandRedSoft,
            fontSize = 12.sp,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(32.dp))

        StatusCard(
            icon = Icons.Filled.MyLocation,
            iconTint = SafeZoneColors.AccentBlue,
            title = "Live Tracking",
            subtitle = "Sharing precise location with\nEmergency Contacts",
            active = s.locationSharing
        )
        Spacer(Modifier.height(12.dp))
        StatusCard(
            icon = Icons.Filled.Mic,
            iconTint = SafeZoneColors.BrandRed,
            title = "Audio Recording",
            subtitle = "Capturing environment",
            active = s.audioRecording,
            waveform = true
        )
        Spacer(Modifier.height(12.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SafeZoneColors.BgCard)
                .padding(14.dp)
        ) {
            Text(
                text = "Local authorities have been notified of your situation.\nEstimated arrival: ${s.etaMinutes} mins",
                color = SafeZoneColors.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.weight(1f))
        SlideToEnd(onTriggered = { vm.endSos(ctx, onEnded) })
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatusCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    active: Boolean,
    waveform: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SafeZoneColors.BgCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint)
        }

        Spacer(Modifier.size(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = SafeZoneColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = subtitle, color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
        }

        if (waveform) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(10, 20, 14, 28, 16, 22).forEach { heightDp ->
                    Box(
                        Modifier
                            .size(3.dp, heightDp.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SafeZoneColors.BrandRed)
                    )
                }
            }
        } else if (active) {
            Icon(
                Icons.Filled.CheckCircle,
                null,
                tint = SafeZoneColors.AccentBlue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SlideToEnd(onTriggered: () -> Unit) {
    val density = LocalDensity.current
    val trackWidth = 320.dp
    val knobSize = 56.dp
    val maxX = with(density) { (trackWidth - knobSize).toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animated by animateFloatAsState(offsetX, label = "slide")

    Box(
        modifier = Modifier
            .size(width = trackWidth, height = 64.dp)
            .clip(RoundedCornerShape(50))
            .background(SafeZoneColors.BgCardElevated),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "<  Slide to End SOS",
            color = SafeZoneColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(animated.toInt(), 0) }
                .size(knobSize)
                .clip(CircleShape)
                .background(SafeZoneColors.BgBase)
                .align(Alignment.CenterStart)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX >= maxX * 0.85f) onTriggered()
                            offsetX = 0f
                        }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(0f, maxX)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ChevronRight, null, tint = SafeZoneColors.BrandRedSoft)
        }
    }
}
