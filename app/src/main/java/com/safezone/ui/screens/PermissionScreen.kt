package com.safezone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*
import com.safezone.ui.theme.SafeZoneColors
import com.safezone.utils.PermissionUtils

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onAllGranted: () -> Unit
) {
    val permissions = rememberMultiplePermissionsState(PermissionUtils.ALL_REQUIRED.toList())

    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted) onAllGranted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BackgroundDark)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SafeZoneColors.OrangeDim, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = SafeZoneColors.OrangeCore, modifier = Modifier.size(40.dp))
        }

        Spacer(Modifier.height(20.dp))
        Text("SYSTEM ACCESS", color = SafeZoneColors.OrangeCore, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 3.sp)
        Text("REQUIRED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 3.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "SafeZone requires the following permissions to provide emergency monitoring and response.",
            color     = SafeZoneColors.TextSecondary,
            fontSize  = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Permission List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(PermissionUtils.ALL_REQUIRED) { perm ->
                val state  = permissions.permissions.find { it.permission == perm }
                val granted = state?.status?.isGranted ?: false

                Card(
                    shape  = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (granted) SafeZoneColors.SurfaceCard else SafeZoneColors.SurfaceCard
                    ),
                    border = if (granted)
                        androidx.compose.foundation.BorderStroke(1.dp, SafeZoneColors.StatusGreen.copy(alpha = 0.4f))
                    else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (granted) SafeZoneColors.StatusGreen.copy(alpha = 0.15f)
                                    else SafeZoneColors.SurfaceElevated,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (granted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = null,
                                tint     = if (granted) SafeZoneColors.StatusGreen else SafeZoneColors.TextDim,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(PermissionUtils.label(perm), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(PermissionUtils.rationale(perm), color = SafeZoneColors.TextDim, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (permissions.allPermissionsGranted) {
            Button(
                onClick  = onAllGranted,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = SafeZoneColors.StatusGreen)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("ALL PERMISSIONS GRANTED", color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        } else {
            Button(
                onClick  = { permissions.launchMultiplePermissionRequest() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = SafeZoneColors.OrangeCore)
            ) {
                Text("GRANT PERMISSIONS", color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
            }
        }
    }
}
