package com.safezone.app.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safezone.app.domain.models.PhraseAction
import com.safezone.app.domain.models.SecurityPhrase
import com.safezone.app.ui.components.BottomTab
import com.safezone.app.ui.components.SafeZoneBottomBar
import com.safezone.app.ui.components.SafeZoneTopBar
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    onPhraseSetup: () -> Unit,
    onSignOut: () -> Unit,
    onTabHome: () -> Unit,
    onTabMap: () -> Unit,
    onTabHistory: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Column(Modifier.fillMaxSize().background(SafeZoneColors.BgBase)) {
        SafeZoneTopBar(onSettingsClick = { })

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text("Configuration",
                color = SafeZoneColors.TextPrimary,
                fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(4.dp))
            Text("Manage your safety protocols and system access.",
                color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))

            // Security Phrases
            SectionCard {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Security Phrases",
                            color = SafeZoneColors.BrandRedSoft,
                            fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Voice commands to trigger silent alarms.",
                            color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp).clip(CircleShape)
                            .background(SafeZoneColors.BgCardElevated)
                            .clickable { onPhraseSetup() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, null, tint = SafeZoneColors.BrandRedSoft)
                    }
                }
                Spacer(Modifier.height(14.dp))
                s.phrases.forEach { p ->
                    PhraseRow(p, onDelete = { vm.deletePhrase(p.id) })
                    Spacer(Modifier.height(10.dp))
                }
                if (s.phrases.isEmpty()) {
                    Text("No phrases yet. Tap + to add one.",
                        color = SafeZoneColors.TextMuted, fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 10.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            ToggleRow(
                icon = Icons.Filled.Fingerprint,
                title = "Biometrics",
                description = "Require Face ID or Fingerprint to disable active alerts or modify settings.",
                checked = s.biometricEnabled,
                onChange = vm::toggleBiometric
            )
            Spacer(Modifier.height(16.dp))
            ToggleRow(
                icon = Icons.Filled.Mic,
                title = "Listening Mode",
                description = "Continuously monitor background audio for your defined security phrases.",
                checked = s.listeningEnabled,
                onChange = { vm.toggleListening(ctx, it) }
            )
            Spacer(Modifier.height(16.dp))
            ToggleRow(
                icon = Icons.Filled.Bluetooth,
                title = "Offline BLE Mesh",
                description = "Relay SOS to nearby SafeZone users via Bluetooth when offline.",
                checked = s.bleFallbackEnabled,
                onChange = { vm.toggleBle(ctx, it) }
            )

            Spacer(Modifier.height(20.dp))
            SectionCard {
                Text("Device Permissions",
                    color = SafeZoneColors.TextPrimary,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                PermissionChip(Icons.Filled.PinDrop, "LOCATION", "ALWAYS")
                Spacer(Modifier.height(10.dp))
                PermissionChip(Icons.Filled.Notifications, "ALERTS", "ENABLED")
                Spacer(Modifier.height(10.dp))
                PermissionChip(Icons.Filled.ContactMail, "CONTACTS", "LIMITED")
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SafeZoneColors.BgCard)
                    .clickable { vm.signOut(onSignOut) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = SafeZoneColors.BrandRedSoft)
                Spacer(Modifier.size(12.dp))
                Text("Sign Out",
                    color = SafeZoneColors.BrandRedSoft,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }

        SafeZoneBottomBar(
            selected = BottomTab.Settings,
            onHome = onTabHome, onMap = onTabMap,
            onHistory = onTabHistory, onSettings = { }
        )
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SafeZoneColors.BgCard)
            .padding(18.dp)
    ) { content() }
}

@Composable
private fun PhraseRow(p: SecurityPhrase, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SafeZoneColors.BgCardElevated)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(8.dp).clip(CircleShape)
                .background(when (p.action) {
                    PhraseAction.SILENT_POLICE -> SafeZoneColors.BrandRedSoft
                    PhraseAction.NOTIFY_CONTACTS -> SafeZoneColors.AccentBlue
                    PhraseAction.FULL_SOS -> SafeZoneColors.BrandRedDeep
                })
        )
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text("\"${p.text}\"",
                color = SafeZoneColors.TextPrimary,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("Action: ${actionLabel(p.action)}",
                color = SafeZoneColors.TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Filled.Delete, null,
            tint = SafeZoneColors.TextSecondary,
            modifier = Modifier.size(20.dp).clickable { onDelete() })
    }
}

private fun actionLabel(a: PhraseAction) = when (a) {
    PhraseAction.SILENT_POLICE -> "Silent Police Dispatch"
    PhraseAction.NOTIFY_CONTACTS -> "Notify Emergency Contacts"
    PhraseAction.FULL_SOS -> "Full SOS"
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = SafeZoneColors.BrandRedSoft, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(10.dp))
            Text(title, color = SafeZoneColors.TextPrimary,
                fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SafeZoneColors.TextPrimary,
                    checkedTrackColor = SafeZoneColors.BrandRed,
                    uncheckedThumbColor = SafeZoneColors.TextSecondary,
                    uncheckedTrackColor = SafeZoneColors.BgCardElevated
                )
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(description, color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun PermissionChip(icon: ImageVector, label: String, state: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SafeZoneColors.BgCardElevated)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = SafeZoneColors.AccentBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(10.dp))
        Text(label, color = SafeZoneColors.TextPrimary,
            fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.sp,
            modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(50))
                .background(SafeZoneColors.BgCard).padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(state, color = SafeZoneColors.TextSecondary,
                fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        }
    }
}
