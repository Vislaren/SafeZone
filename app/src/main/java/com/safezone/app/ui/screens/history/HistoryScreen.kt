package com.safezone.app.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safezone.app.domain.models.SosEvent
import com.safezone.app.domain.models.SosStatus
import com.safezone.app.ui.components.BottomTab
import com.safezone.app.ui.components.OutlinedPill
import com.safezone.app.ui.components.SafeZoneBottomBar
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.HistoryViewModel
import com.safezone.app.utils.Formatters

@Composable
fun HistoryScreen(
    onTabHome: () -> Unit,
    onTabMap: () -> Unit,
    onTabSettings: () -> Unit,
    vm: HistoryViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(SafeZoneColors.BgBase)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SOS History",
                color = SafeZoneColors.TextPrimary,
                fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Icon(Icons.Filled.FilterList, null,
                tint = SafeZoneColors.BrandRedSoft, modifier = Modifier.size(26.dp))
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(Modifier.weight(1f), "Total Alerts", s.totalAlerts.toString(), null)
            StatCard(Modifier.weight(1f), "Avg Response", s.avgResponseMinutes.toString(), "m")
        }
        Spacer(Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp, vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(s.events, key = { it.id }) { event -> EventCard(event) }
            if (s.events.isEmpty()) {
                item {
                    Text("No history yet.",
                        color = SafeZoneColors.TextSecondary, fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }

        SafeZoneBottomBar(
            selected = BottomTab.History,
            onHome = onTabHome, onMap = onTabMap,
            onHistory = { }, onSettings = onTabSettings
        )
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, suffix: String?) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SafeZoneColors.BgCard)
            .padding(18.dp)
    ) {
        Text(label, color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = SafeZoneColors.TextPrimary,
                fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
            if (suffix != null) {
                Text(suffix, color = SafeZoneColors.TextSecondary,
                    fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp, start = 3.dp))
            }
        }
    }
}

@Composable
private fun EventCard(e: SosEvent) {
    val pillBorder = when (e.status) {
        SosStatus.HIGH_ALERT -> SafeZoneColors.BrandRed
        SosStatus.RESOLVED -> SafeZoneColors.AccentBlue
        SosStatus.CANCELED -> SafeZoneColors.TextMuted
        SosStatus.ACTIVE -> SafeZoneColors.BrandRedDeep
    }
    val pillText = when (e.status) {
        SosStatus.HIGH_ALERT -> "HIGH ALERT"
        SosStatus.RESOLVED -> "RESOLVED"
        SosStatus.CANCELED -> "CANCELED"
        SosStatus.ACTIVE -> "ACTIVE"
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(SafeZoneColors.BgCard)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp)
                .background(Color(0xFF1A2A1A)) // static map placeholder tint
        ) {
            Row(
                modifier = Modifier.padding(12.dp).align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(50))
                    .background(SafeZoneColors.BgBase.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, null,
                    tint = SafeZoneColors.BrandRedSoft, modifier = Modifier.size(14.dp))
                Spacer(Modifier.size(4.dp))
                Text("Sector 4", color = SafeZoneColors.TextPrimary, fontSize = 12.sp)
            }
        }
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(e.note ?: titleForEvent(e),
                    color = SafeZoneColors.TextPrimary,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedPill(text = pillText, borderColor = pillBorder, textColor = pillBorder)
            }
            Spacer(Modifier.height(6.dp))
            Text(descriptionFor(e),
                color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarToday, null,
                    tint = SafeZoneColors.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.size(6.dp))
                Text(Formatters.formatDate(e.startedAt),
                    color = SafeZoneColors.TextMuted, fontSize = 12.sp)
                Spacer(Modifier.size(14.dp))
                Icon(Icons.Filled.Schedule, null,
                    tint = SafeZoneColors.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.size(6.dp))
                Text("${Formatters.formatTime(e.startedAt)} (${Formatters.formatDurationShort(e.durationSeconds)})",
                    color = SafeZoneColors.TextMuted, fontSize = 12.sp)
            }
        }
    }
}

private fun titleForEvent(e: SosEvent): String = when (e.triggerSource.name) {
    "PHRASE" -> "Voice Trigger"
    "GEOFENCE" -> "Geofence Alert"
    "BLE" -> "Offline Mesh Alert"
    else -> "Manual SOS"
}

private fun descriptionFor(e: SosEvent): String = when (e.status) {
    SosStatus.CANCELED -> "Accidental trigger. Dismissed by user."
    SosStatus.RESOLVED -> "Resolved and marked safe."
    else -> "Dispatched security unit to location."
}
