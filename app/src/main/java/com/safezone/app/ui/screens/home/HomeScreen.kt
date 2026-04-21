package com.safezone.app.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safezone.app.ui.components.BottomTab
import com.safezone.app.ui.components.SafeZoneBottomBar
import com.safezone.app.ui.components.SafeZoneTopBar
import com.safezone.app.ui.components.StatusPill
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenHistory: () -> Unit,
    onSosActive: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    LaunchedEffect(s.sosActive) { if (s.sosActive) onSosActive() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BgBase)
    ) {
        SafeZoneTopBar(
            avatarUrl = s.avatarUrl,
            onSettingsClick = onOpenSettings
        )
        Spacer(Modifier.height(24.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StatusPill(
                text = "Status: ${s.statusLabel}",
                dotColor = if (s.sosActive) SafeZoneColors.BrandRed else SafeZoneColors.AccentBlue
            )
        }
        Spacer(Modifier.height(40.dp))

        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            SosHoldButton(
                holdMs = 3000,
                onTriggered = { vm.triggerSos(ctx) }
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAction(
                label = "Start Listening",
                icon = Icons.Filled.Mic,
                modifier = Modifier.weight(1f),
                onClick = { vm.toggleListening(ctx) }
            )
            QuickAction(
                label = "View Map",
                icon = Icons.Filled.Map,
                modifier = Modifier.weight(1f),
                onClick = onOpenMap
            )
        }
        Spacer(Modifier.height(16.dp))
        SafeZoneBottomBar(
            selected = BottomTab.Home,
            onHome = { }, onMap = onOpenMap,
            onHistory = onOpenHistory, onSettings = onOpenSettings
        )
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SafeZoneColors.BgCard)
            .clickable { onClick() }
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(SafeZoneColors.BgCardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = SafeZoneColors.TextSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = SafeZoneColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** Big SOS button. Hold for [holdMs] to trigger, releasing early cancels. */
@Composable
private fun SosHoldButton(
    holdMs: Long,
    onTriggered: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "sos-pulse")
    val pulse by transition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "pulse"
    )
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(pulse)
                .border(2.dp, SafeZoneColors.BrandRedDeep.copy(alpha = 0.35f + progress.value * 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .border(2.dp, SafeZoneColors.BrandRedDeep.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(SafeZoneColors.SosButtonGradient)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    val job = scope.launch {
                                        progress.animateTo(1f, tween(holdMs.toInt()))
                                        onTriggered()
                                    }
                                    val released = tryAwaitRelease()
                                    if (!released || progress.value < 1f) {
                                        job.cancel()
                                        progress.animateTo(0f, tween(180))
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SOS",
                        color = SafeZoneColors.BrandRedDeep,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 42.sp, letterSpacing = 2.sp)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Hold for 3 seconds to activate\nemergency protocol",
            color = SafeZoneColors.TextSecondary,
            fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
