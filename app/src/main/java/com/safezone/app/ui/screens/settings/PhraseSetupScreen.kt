package com.safezone.app.ui.screens.settings

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safezone.app.ui.components.LabeledField
import com.safezone.app.ui.components.PrimaryGradientButton
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.PhraseSetupViewModel

@Composable
fun PhraseSetupScreen(
    onBack: () -> Unit,
    vm: PhraseSetupViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BgBase)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SafeZoneColors.BgElevated)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                tint = SafeZoneColors.TextPrimary,
                modifier = Modifier.size(28.dp).clickable { onBack() })
            Spacer(Modifier.weight(1f))
            Text("SafeZone", color = SafeZoneColors.BrandRed,
                fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(28.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text("Security Phrase",
                color = SafeZoneColors.TextPrimary,
                fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text("Set a secret phrase. In an emergency, speaking this phrase will silently trigger an SOS alert.",
                color = SafeZoneColors.TextSecondary, fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SafeZoneColors.BgCard)
                    .padding(20.dp)
            ) {
                LabeledField(
                    label = "Phrase Text",
                    value = s.text,
                    onValueChange = vm::onTextChange,
                    placeholder = "Red Protocol"
                )
            }
            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SafeZoneColors.BgCard)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("STATUS",
                    color = SafeZoneColors.AccentBlue,
                    fontSize = 12.sp, letterSpacing = 3.sp, fontWeight = FontWeight.SemiBold)
                Text(if (s.recording) "Recording…" else "Ready to Record",
                    color = SafeZoneColors.TextPrimary,
                    fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                val t = rememberInfiniteTransition(label = "rec-pulse")
                val scale by t.animateFloat(
                    initialValue = 1f, targetValue = if (s.recording) 1.15f else 1f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "scale"
                )
                val ctx = LocalContext.current
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(
                                SafeZoneColors.BrandRedSoft, SafeZoneColors.BrandRed, SafeZoneColors.BrandRedDeep
                            ))
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                vm.startRecording(ctx)
                                try {
                                    awaitRelease()
                                } finally {
                                    vm.stopRecording()
                                }
                            })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Mic, null,
                        tint = SafeZoneColors.BgBase, modifier = Modifier.size(44.dp))
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom) {
                    listOf(6, 14, 10, 20, 14, 22, 12, 18, 8, 14).forEach { h ->
                        Box(Modifier.size(4.dp, h.dp).clip(RoundedCornerShape(2.dp))
                            .background(SafeZoneColors.TextSecondary))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Hold to record your phrase. Release to save.",
                    color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
            }

            if (s.recordedFilePath != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(SafeZoneColors.BgCardElevated)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val ctx = LocalContext.current
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(SafeZoneColors.BgCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, tint = SafeZoneColors.TextPrimary,
                            modifier = Modifier.clickable { vm.playRecording(ctx) })
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${s.text.ifBlank { "Phrase" }}.wav",
                            color = SafeZoneColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("0:${s.recordedSeconds.toString().padStart(2, '0')}s",
                            color = SafeZoneColors.TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.Delete, null,
                        tint = SafeZoneColors.TextSecondary,
                        modifier = Modifier.size(20.dp).clickable { vm.clearRecording() })
                }
            }

            if (s.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(s.error!!, color = SafeZoneColors.BrandRedDeep, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            PrimaryGradientButton(
                text = "Save Phrase",
                onClick = vm::save,
                loading = s.saving,
                trailing = { Icon(Icons.Filled.Save, null, tint = SafeZoneColors.BgBase) }
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
