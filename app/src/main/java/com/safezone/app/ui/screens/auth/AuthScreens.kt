package com.safezone.app.ui.screens.auth

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safezone.app.ui.components.LabeledField
import com.safezone.app.ui.components.PasswordField
import com.safezone.app.ui.components.PrimaryGradientButton
import com.safezone.app.ui.components.SecondaryButton
import com.safezone.app.ui.components.ShieldLogo
import com.safezone.app.ui.theme.SafeZoneColors
import com.safezone.app.ui.viewmodels.AuthViewModel
import com.safezone.app.utils.BiometricGate
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGoSignUp: () -> Unit,
    onBiometric: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(s.success) { if (s.success) { vm.clearSuccess(); onLoggedIn() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BgBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        ShieldLogo(size = 88)
        Spacer(Modifier.height(24.dp))
        Text("SafeZone",
            color = SafeZoneColors.TextPrimary,
            fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        Text("SECURE ACCESS PORTAL",
            color = SafeZoneColors.TextSecondary,
            fontSize = 12.sp, letterSpacing = 3.sp)
        Spacer(Modifier.height(48.dp))

        LabeledField(
            label = "Operator ID / Email",
            value = s.email,
            onValueChange = vm::onEmail,
            placeholder = "Enter credentials",
            leading = { Icon(Icons.Filled.Person, null, tint = SafeZoneColors.BrandRedSoft) }
        )
        Spacer(Modifier.height(20.dp))
        PasswordField(
            label = "Passcode",
            value = s.password,
            onValueChange = vm::onPassword,
            placeholder = "••••••••"
        )
        if (s.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(s.error!!, color = SafeZoneColors.BrandRedDeep, fontSize = 13.sp)
        }
        Spacer(Modifier.height(24.dp))
        PrimaryGradientButton(
            text = "ENGAGE",
            onClick = vm::login,
            loading = s.loading
        )
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(1.dp).background(SafeZoneColors.Divider))
            Text("  OR  ", color = SafeZoneColors.TextSecondary, fontSize = 12.sp, letterSpacing = 2.sp)
            Box(Modifier.weight(1f).height(1.dp).background(SafeZoneColors.Divider))
        }
        Spacer(Modifier.height(20.dp))
        SecondaryButton(
            text = "Biometric Access",
            onClick = onBiometric,
            leading = { Icon(Icons.Filled.Fingerprint, null, tint = SafeZoneColors.BrandRedSoft) }
        )
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("No account? ", color = SafeZoneColors.TextSecondary, fontSize = 14.sp)
            Text("Create one",
                color = SafeZoneColors.BrandRedSoft,
                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.clickable { onGoSignUp() })
        }
        Spacer(Modifier.height(24.dp))
        Text(buildAnnotatedString {
            append("Unauthorized access is strictly prohibited.\n")
            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append("Terms of Service") }
        }, color = SafeZoneColors.TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onGoLogin: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(s.success) { if (s.success) { vm.clearSuccess(); onSignedUp() } }
    BackHandler { onGoLogin() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BgBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            ShieldLogo(size = 44)
            Spacer(Modifier.size(10.dp))
            Text("SafeZone", color = SafeZoneColors.BrandRedSoft,
                fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(24.dp))
        Text("Create Account", color = SafeZoneColors.TextPrimary,
            fontSize = 28.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text("Join the tactical safety network.",
            color = SafeZoneColors.TextSecondary, fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        LabeledField(
            label = "Email Address", value = s.email, onValueChange = vm::onEmail,
            placeholder = "Enter your email",
            leading = { Icon(Icons.Filled.Email, null, tint = SafeZoneColors.TextSecondary) }
        )
        Spacer(Modifier.height(20.dp))
        PasswordField(
            label = "Password", value = s.password, onValueChange = vm::onPassword,
            placeholder = "Create a strong password"
        )
        Spacer(Modifier.height(20.dp))
        PasswordField(
            label = "Confirm Password", value = s.confirm, onValueChange = vm::onConfirm,
            placeholder = "Repeat your password"
        )
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.clickable { vm.onAgreedToggle() }) {
            Box(
                modifier = Modifier.size(22.dp)
                    .clip(CircleShape)
                    .background(if (s.agreed) SafeZoneColors.BrandRed else SafeZoneColors.BgCard)
            )
            Spacer(Modifier.size(12.dp))
            Text(buildAnnotatedString {
                append("I agree to the ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = SafeZoneColors.BrandRedSoft)) {
                    append("Terms of Service")
                }
                append(" and ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = SafeZoneColors.BrandRedSoft)) {
                    append("Privacy Policy.")
                }
            }, color = SafeZoneColors.TextSecondary, fontSize = 13.sp)
        }
        if (s.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(s.error!!, color = SafeZoneColors.BrandRedDeep, fontSize = 13.sp)
        }
        Spacer(Modifier.height(28.dp))
        PrimaryGradientButton(
            text = "Initiate Account",
            onClick = vm::register,
            loading = s.loading,
            trailing = { Icon(Icons.Filled.ArrowForward, null, tint = SafeZoneColors.BgBase) }
        )
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Already operational?  ", color = SafeZoneColors.TextSecondary, fontSize = 14.sp)
            Text("Log In",
                color = SafeZoneColors.BrandRedSoft,
                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.clickable { onGoLogin() })
        }
    }
}

@Composable
fun BiometricLoginScreen(
    onAuthenticated: () -> Unit,
    onFallbackPassword: () -> Unit
) {
    val ctx = LocalContext.current
    val activity = ctx as? FragmentActivity
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(activity) {
        if (activity == null) { onFallbackPassword(); return@LaunchedEffect }
        scope.launch {
            when (BiometricGate.authenticate(activity,
                title = "Authentication",
                subtitle = "Scan fingerprint to continue")
            ) {
                BiometricGate.Result.Success -> onAuthenticated()
                else -> onFallbackPassword()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(SafeZoneColors.BgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(SafeZoneColors.BgCard)
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(
                        listOf(SafeZoneColors.BrandRedGlow, SafeZoneColors.BgCard))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Fingerprint, null,
                    tint = SafeZoneColors.BrandRed,
                    modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Authentication",
                color = SafeZoneColors.TextPrimary,
                fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Scan fingerprint to continue",
                color = SafeZoneColors.BrandRedSoft, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            Text("CANCEL",
                color = SafeZoneColors.TextSecondary, letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                modifier = Modifier.clickable { onFallbackPassword() })
        }
    }
}
