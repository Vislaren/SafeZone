package com.safezone.app.ui.screens.alert

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.safezone.app.ui.components.OutlinedPill
import com.safezone.app.ui.components.PrimaryGradientButton
import com.safezone.app.ui.components.SecondaryButton
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.AlertViewModel
import com.safezone.app.utils.Formatters

@Composable
fun AlertScreen(
    eventId: String,
    onNavigate: () -> Unit,
    onAssist: () -> Unit,
    onBack: () -> Unit,
    vm: AlertViewModel = hiltViewModel()
) {
    LaunchedEffect(eventId) { vm.load(eventId) }
    val s by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SafeZoneColors.BrandRedDeep.copy(alpha = 0.45f),
                        SafeZoneColors.BgBase,
                        SafeZoneColors.BgBase
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pulsing warning icon
        val p = rememberInfiniteTransition(label = "warn-pulse")
        val a by p.animateFloat(0.6f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
        Box(
            modifier = Modifier.size(110.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(
                    SafeZoneColors.BrandRedDeep.copy(alpha = a * 0.4f),
                    SafeZoneColors.BgBase
                ))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PriorityHigh, null,
                tint = SafeZoneColors.BrandRedSoft, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("DISTRESS SIGNAL",
            color = SafeZoneColors.BrandRedSoft,
            fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text("IMMEDIATE RESPONSE REQUIRED",
            color = SafeZoneColors.BrandRedSoft.copy(alpha = 0.7f),
            fontSize = 12.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SafeZoneColors.BgCard)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(130.dp).clip(CircleShape)
                        .border(3.dp, SafeZoneColors.BrandRed, CircleShape),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (s.avatarUrl != null) {
                        AsyncImage(model = s.avatarUrl, contentDescription = null,
                            modifier = Modifier.size(130.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop)
                    } else {
                        Box(
                            modifier = Modifier.size(130.dp).clip(CircleShape).background(SafeZoneColors.BgCardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null,
                                tint = SafeZoneColors.TextSecondary, modifier = Modifier.size(60.dp))
                        }
                    }
                    Box(
                        modifier = Modifier.size(36.dp)
                            .clip(CircleShape)
                            .background(SafeZoneColors.BgCardElevated)
                            .alpha(a),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Sensors, null, tint = SafeZoneColors.BrandRedSoft)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(s.userName.ifBlank { "Unknown" },
                    color = SafeZoneColors.TextPrimary,
                    fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                OutlinedPill(
                    text = Formatters.formatDistance(s.distanceMeters) + " away",
                    borderColor = SafeZoneColors.BrandRedDeep
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SafeZoneColors.BgCard)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("EST. INTERCEPT",
                    color = SafeZoneColors.TextSecondary,
                    fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                Text("${"%.1f".format(s.etaMinutes)} Mins",
                    color = SafeZoneColors.AccentBlue,
                    fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Assist",
                modifier = Modifier.weight(1f),
                leading = { Icon(Icons.Filled.Phone, null, tint = SafeZoneColors.BrandRedSoft) },
                onClick = onAssist
            )
            PrimaryGradientButton(
                text = "Navigate",
                onClick = onNavigate,
                modifier = Modifier.weight(1.3f),
                trailing = { Icon(Icons.Filled.Navigation, null, tint = SafeZoneColors.BgBase) }
            )
        }
    }
}
