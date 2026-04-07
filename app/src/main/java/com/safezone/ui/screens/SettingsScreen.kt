package com.safezone.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safezone.data.db.Contact
import com.safezone.data.db.TriggerPhrase
import com.safezone.ui.theme.SafeZoneColors
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    contacts: List<Contact>,
    phrases: List<TriggerPhrase>,
    activePhrase: String,
    impactThreshold: Float,
    accelSensitivity: Float,
    onAddContact: (name: String, phone: String, slot: Int) -> Unit,
    onRemoveContact: (Contact) -> Unit,
    onSavePhrase: (phrase: String, slot: Int) -> Unit,
    onRemovePhrase: (TriggerPhrase) -> Unit,
    onImpactThresholdChange: (Float) -> Unit,
    onAccelSensitivityChange: (Float) -> Unit,
    onSyncAll: () -> Unit
) {
    var showAddContactDialog by remember { mutableStateOf(false) }
    var addContactSlot       by remember { mutableIntStateOf(1) }
    var showAddPhraseDialog  by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
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
            Icon(Icons.Default.Tune, contentDescription = null, tint = SafeZoneColors.TextSecondary)
        }

        // ── Title ─────────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("SYSTEM CONFIGURATION", color = SafeZoneColors.TextDim, fontSize = 10.sp, letterSpacing = 2.sp)
            Text(
                "SETTINGS",
                color      = Color.White,
                fontWeight = FontWeight.Black,
                fontSize   = 32.sp,
                letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Emergency Contacts Section ─────────────────────────────────────────
        SectionHeader(
            title    = "EMERGENCY CONTACTS",
            subtitle = "PRIORITY LEVEL 1 NOTIFICATION SQUAD",
            action   = "SYNC ALL",
            onAction = onSyncAll
        )

        Spacer(Modifier.height(12.dp))

        val contactSlots = (1..5).toList()
        contactSlots.forEach { slot ->
            val contact = contacts.find { it.slot == slot }
            ContactSlotRow(
                slot    = slot,
                contact = contact,
                onAdd   = {
                    addContactSlot = slot
                    showAddContactDialog = true
                },
                onRemove = { contact?.let(onRemoveContact) }
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(24.dp))

        // ── Voice Triggers Section ─────────────────────────────────────────────
        SectionHeader(
            title    = "VOICE TRIGGERS",
            subtitle = null,
            action   = null,
            onAction = null
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("CURRENT ACTIVE PHRASE", color = SafeZoneColors.TextDim, fontSize = 10.sp, letterSpacing = 1.sp)
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0A2A0A), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("VERIFIED", color = SafeZoneColors.StatusGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SafeZoneColors.BackgroundDark, RoundedCornerShape(8.dp))
                        .padding(vertical = 14.dp, horizontal = 12.dp)
                ) {
                    Text(
                        "\"$activePhrase\"",
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle  = FontStyle.Italic,
                        fontSize   = 18.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick  = { showAddPhraseDialog = true },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape    = RoundedCornerShape(8.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = SafeZoneColors.OrangeCore)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RECORD VOICE PRINT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                    IconButton(
                        onClick  = { showAddPhraseDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(SafeZoneColors.SurfaceElevated, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit phrase", tint = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Kinetic Sensors Section ────────────────────────────────────────────
        SectionHeader(
            title    = "KINETIC SENSORS",
            subtitle = "FAST MOVEMENT DETECTION CALIBRATION",
            action   = null,
            onAction = null
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Impact Threshold
                SensorSlider(
                    label       = "IMPACT THRESHOLD",
                    value       = impactThreshold,
                    displayPct  = "${(impactThreshold * 100).roundToInt()}%",
                    description = "Minimum G-force required to initiate emergency sequence. Higher values prevent false alarms during active sports.",
                    onValueChange = onImpactThresholdChange
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = SafeZoneColors.Divider)
                Spacer(Modifier.height(16.dp))

                // Rapid Acceleration
                SensorSlider(
                    label       = "RAPID ACCELERATION",
                    value       = accelSensitivity,
                    displayPct  = "${(accelSensitivity * 100).roundToInt()}%",
                    description = "Sensitivity to sudden velocity shifts. Balanced for urban mobility environments.",
                    onValueChange = onAccelSensitivityChange
                )
            }
        }
    }

    // ── Add Contact Dialog ─────────────────────────────────────────────────────
    if (showAddContactDialog) {
        AddContactDialog(
            slot    = addContactSlot,
            onSave  = { name, phone ->
                onAddContact(name, phone, addContactSlot)
                showAddContactDialog = false
            },
            onDismiss = { showAddContactDialog = false }
        )
    }

    // ── Add Phrase Dialog ──────────────────────────────────────────────────────
    if (showAddPhraseDialog) {
        AddPhraseDialog(
            existingSlots = phrases.map { it.slot },
            onSave = { phrase, slot ->
                onSavePhrase(phrase, slot)
                showAddPhraseDialog = false
            },
            onDismiss = { showAddPhraseDialog = false }
        )
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String?,
    action: String?,
    onAction: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Bottom
    ) {
        Column {
            Text(title, color = SafeZoneColors.OrangeCore, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
            subtitle?.let { Text(it, color = SafeZoneColors.TextDim, fontSize = 10.sp, letterSpacing = 1.sp) }
        }
        action?.let {
            Button(
                onClick  = onAction ?: {},
                shape    = RoundedCornerShape(8.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = SafeZoneColors.SurfaceCard),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(it, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        }
    }
}

// ─── Contact Slot Row ─────────────────────────────────────────────────────────
@Composable
private fun ContactSlotRow(
    slot: Int,
    contact: Contact?,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SafeZoneColors.SurfaceElevated, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint     = if (contact != null) SafeZoneColors.OrangeCore else SafeZoneColors.TextDim,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (contact != null) {
                    Text(contact.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(contact.phone, color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
                } else {
                    Text("Contact Name", color = SafeZoneColors.TextDim, fontSize = 15.sp)
                    Text("+1 (555) 000-0000", color = SafeZoneColors.TextDim, fontSize = 13.sp)
                }
            }
            if (contact != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.PersonRemove, contentDescription = "Remove", tint = SafeZoneColors.StatusRed)
                }
            } else {
                IconButton(onClick = onAdd) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add", tint = SafeZoneColors.OrangeCore, modifier = Modifier.size(20.dp))
                        Text("ADD", color = SafeZoneColors.OrangeCore, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

// ─── Sensor Slider ────────────────────────────────────────────────────────────
@Composable
private fun SensorSlider(
    label: String,
    value: Float,
    displayPct: String,
    description: String,
    onValueChange: (Float) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
        Text(displayPct, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
    Spacer(Modifier.height(8.dp))
    Slider(
        value        = value,
        onValueChange = onValueChange,
        modifier     = Modifier.fillMaxWidth(),
        colors       = SliderDefaults.colors(
            thumbColor          = SafeZoneColors.OrangeCore,
            activeTrackColor    = SafeZoneColors.OrangeCore,
            inactiveTrackColor  = SafeZoneColors.SurfaceElevated
        )
    )
    Text(description, color = SafeZoneColors.TextDim, fontSize = 11.sp)
}

// ─── Dialogs ──────────────────────────────────────────────────────────────────
@Composable
private fun AddContactDialog(
    slot: Int,
    onSave: (name: String, phone: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name  by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SafeZoneColors.SurfaceCard,
        title = { Text("Add Contact — Slot $slot", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Name", color = SafeZoneColors.TextDim) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = safeZoneTextFieldColors()
                )
                OutlinedTextField(
                    value         = phone,
                    onValueChange = { phone = it },
                    label         = { Text("Phone", color = SafeZoneColors.TextDim) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = safeZoneTextFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone) }) {
                Text("SAVE", color = SafeZoneColors.OrangeCore, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = SafeZoneColors.TextDim)
            }
        }
    )
}

@Composable
private fun AddPhraseDialog(
    existingSlots: List<Int>,
    onSave: (phrase: String, slot: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var phrase by remember { mutableStateOf("") }
    val slot   = ((1..3).firstOrNull { it !in existingSlots }) ?: 1

    AlertDialog(
        onDismissRequest = onDismissRequest@{ onDismiss() },
        containerColor   = SafeZoneColors.SurfaceCard,
        title = { Text("Record Voice Trigger", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter a trigger phrase (slot $slot / 3). Speak clearly for best recognition.",
                    color = SafeZoneColors.TextSecondary,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value         = phrase,
                    onValueChange = { phrase = it },
                    label         = { Text("Trigger Phrase", color = SafeZoneColors.TextDim) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = safeZoneTextFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (phrase.isNotBlank()) onSave(phrase, slot) }) {
                Text("SAVE PHRASE", color = SafeZoneColors.OrangeCore, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = SafeZoneColors.TextDim) }
        }
    )
}

@Composable
fun safeZoneTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = SafeZoneColors.OrangeCore,
    unfocusedBorderColor    = SafeZoneColors.Divider,
    cursorColor             = SafeZoneColors.OrangeCore,
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White,
    unfocusedContainerColor = SafeZoneColors.SurfaceElevated,
    focusedContainerColor   = SafeZoneColors.SurfaceElevated
)
