package com.safezone.app.ui.screens.map

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.safezone.app.ui.components.BottomTab
import com.safezone.app.ui.components.OutlinedPill
import com.safezone.app.ui.components.PrimaryGradientButton
import com.safezone.app.ui.components.SafeZoneBottomBar
import com.safezone.app.ui.components.SafeZoneTopBar
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.MapViewModel
import com.safezone.app.utils.Formatters

@Composable
fun MapScreen(
    onOpenAlert: (String) -> Unit,
    onBack: () -> Unit,
    onTabHome: () -> Unit,
    onTabHistory: () -> Unit,
    onTabSettings: () -> Unit,
    vm: MapViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()

    val camera: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }
    val me = s.myLocation
    androidx.compose.runtime.LaunchedEffect(me?.lat, me?.lng) {
        if (me != null) {
            camera.position = CameraPosition.fromLatLngZoom(LatLng(me.lat, me.lng), 15f)
        }
    }

    Column(Modifier.fillMaxSize().background(SafeZoneColors.BgBase)) {
        SafeZoneTopBar(onSettingsClick = onTabSettings)

        Box(Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                properties = MapProperties(isMyLocationEnabled = me != null),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true,
                    mapToolbarEnabled = false
                )
            ) {
                s.nearby.forEach { u ->
                    Marker(
                        state = MarkerState(LatLng(u.lat, u.lng)),
                        title = u.name,
                        snippet = Formatters.formatDistance(u.distanceMeters)
                    )
                }
                s.focusedAlert?.let { a ->
                    Marker(
                        state = MarkerState(LatLng(a.lat, a.lng)),
                        title = a.userName,
                        snippet = "SOS: ${Formatters.formatDistance(a.distanceMeters)}"
                    )
                }
            }

            // Incoming-alert bottom sheet
            s.focusedAlert?.let { alert ->
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(SafeZoneColors.BgCard)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(SafeZoneColors.BrandRedGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            if (alert.userPhotoUrl != null) {
                                AsyncImage(model = alert.userPhotoUrl, contentDescription = null,
                                    modifier = Modifier.size(56.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Filled.Person, null, tint = SafeZoneColors.BrandRedSoft)
                            }
                        }
                        Spacer(Modifier.size(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(alert.userName,
                                color = SafeZoneColors.TextPrimary,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.height(4.dp))
                            OutlinedPill(text = "EMERGENCY SOS")
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(Formatters.formatMiles(alert.distanceMeters),
                                color = SafeZoneColors.AccentBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("MILES", color = SafeZoneColors.TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(SafeZoneColors.BgCardElevated).padding(14.dp)
                    ) {
                        Text("User triggered panic sequence. Last known location shared.",
                            color = SafeZoneColors.TextPrimary, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    PrimaryGradientButton(
                        text = "Respond Now",
                        onClick = { onOpenAlert(alert.eventId); vm.clearFocus() },
                        trailing = {
                            Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, tint = SafeZoneColors.BgBase)
                        }
                    )
                }
            }
        }
        SafeZoneBottomBar(
            selected = BottomTab.Map,
            onHome = onTabHome, onMap = { },
            onHistory = onTabHistory, onSettings = onTabSettings
        )
    }
}
