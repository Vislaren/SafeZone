package com.safezone.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safezone.app.ui.components.ShieldLogo
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.SplashDestination
import com.safezone.app.ui.viewmodels.SplashViewModel

@Composable
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onNeedsLogin: () -> Unit,
    vm: SplashViewModel = hiltViewModel()
) {
    val dest by vm.dest.collectAsStateWithLifecycle()

    LaunchedEffect(dest) {
        when (dest) {
            SplashDestination.HOME -> onAuthenticated()
            SplashDestination.LOGIN -> onNeedsLogin()
            SplashDestination.LOADING -> Unit
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1100)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShieldLogo(size = 120)
            Spacer(Modifier.height(32.dp))
            Text("SafeZone", color = SafeZoneColors.TextPrimary,
                fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(8.dp))
            Text("TACTICAL SECURITY",
                color = SafeZoneColors.TextSecondary,
                fontSize = 13.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(80.dp))
            Box(
                modifier = Modifier
                    .width(180.dp).height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SafeZoneColors.BgCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(SafeZoneColors.BrandRedSoft, SafeZoneColors.BrandRed)
                            )
                        )
                )
            }
            Spacer(Modifier.height(14.dp))
            Text("INITIALIZING PROTOCOLS",
                color = SafeZoneColors.TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        }
    }
}
