package com.safezone.app.ui.screens.profile

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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.safezone.app.ui.components.LabeledField
import com.safezone.app.ui.components.PrimaryGradientButton
import com.safezone.app.ui.components.SafeZoneCard
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.ProfileViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun ProfileSetupScreen(
    onSaved: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BgBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text("Profile Setup",
            color = SafeZoneColors.TextPrimary,
            fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(6.dp))
        Text("Secure your identity for emergency response.",
            color = SafeZoneColors.TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))

        // Avatar picker (TODO: wire to file picker + vm.uploadAvatar)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(
                            listOf(SafeZoneColors.BrandRedGlow, SafeZoneColors.BgCard)))
                        .clickable { /* TODO open image picker */ },
                    contentAlignment = Alignment.Center
                ) {
                    if (s.photoUrl != null) {
                        AsyncImage(
                            model = s.photoUrl, contentDescription = null,
                            modifier = Modifier.size(110.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Filled.AddAPhoto, null,
                            tint = SafeZoneColors.BrandRedSoft,
                            modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("UPLOAD PHOTO", color = SafeZoneColors.TextSecondary,
                    fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(28.dp))

        SafeZoneCard(Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PersonOutline, null, tint = SafeZoneColors.BrandRedSoft,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Primary Details",
                        color = SafeZoneColors.BrandRedSoft, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
                LabeledField("Full Name", s.fullName, vm::onName, placeholder = "Jane Doe")
                Spacer(Modifier.height(14.dp))
                LabeledField("Phone Number", s.phone, vm::onPhone,
                    placeholder = "+1 (555) 000-0000", keyboardType = KeyboardType.Phone)
                Spacer(Modifier.height(14.dp))
                LabeledField("Home Address", s.address, vm::onAddress,
                    placeholder = "123 Safety Lane, Sector 7")
            }
        }
        Spacer(Modifier.height(20.dp))

        SafeZoneCard(Modifier.fillMaxWidth()) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("* Emergency Contacts",
                        color = SafeZoneColors.BrandRedSoft,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Box(Modifier.clip(RoundedCornerShape(12.dp))
                        .background(SafeZoneColors.BgCardElevated)
                        .padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("2 Required", color = SafeZoneColors.TextSecondary, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                ContactFields("Contact 01", s.contactName1, s.contactPhone1,
                    vm::onContact1Name, vm::onContact1Phone)
                Spacer(Modifier.height(16.dp))
                ContactFields("Contact 02", s.contactName2, s.contactPhone2,
                    vm::onContact2Name, vm::onContact2Phone)
            }
        }

        if (s.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(s.error!!, color = SafeZoneColors.BrandRedDeep, fontSize = 13.sp)
        }

        Spacer(Modifier.height(32.dp))
        PrimaryGradientButton(
            text = "Save & Continue",
            onClick = { vm.save(onSaved) },
            loading = s.saving,
            trailing = { Icon(Icons.Filled.ArrowForward, null, tint = SafeZoneColors.BgBase) }
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ContactFields(
    title: String,
    name: String,
    phone: String,
    onName: (String) -> Unit,
    onPhone: (String) -> Unit
) {
    Column(modifier = Modifier.clip(RoundedCornerShape(14.dp))
        .background(SafeZoneColors.BgCardElevated).padding(14.dp)) {
        Text(title, color = SafeZoneColors.TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        LabeledField("Name", name, onName, placeholder = "Name")
        Spacer(Modifier.height(10.dp))
        LabeledField("Phone Number", phone, onPhone,
            placeholder = "Phone Number", keyboardType = KeyboardType.Phone)
    }
}
