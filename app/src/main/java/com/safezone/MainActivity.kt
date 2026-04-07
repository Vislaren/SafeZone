package com.safezone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safezone.data.db.VaultFile
import com.safezone.ui.screens.*
import com.safezone.ui.theme.SafeZoneTheme
import com.safezone.ui.theme.SafeZoneColors
import com.safezone.utils.BiometricHelper
import com.safezone.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeZoneTheme {
                SafeZoneApp()
            }
        }
    }
}

// ─── Root Navigation ──────────────────────────────────────────────────────────
@Composable
fun SafeZoneApp() {
    val vm: MainViewModel = hiltViewModel()
    val isAuthenticated   by vm.isAuthenticated.collectAsStateWithLifecycle()
    val onboardingDone    by vm.onboardingDone.collectAsStateWithLifecycle()
    val authState         by vm.authState.collectAsStateWithLifecycle()

    // Permission gate
    var permissionsGranted by remember { mutableStateOf(false) }

    when {
        !permissionsGranted -> PermissionScreen(onAllGranted = { permissionsGranted = true })
        !isAuthenticated    -> OnboardingScreen(
            authState       = authState,
            onPhoneChange   = vm::onPhoneNumberChange,
            onInitiateLink  = vm::initiateLink,
            onOtpChange     = vm::onOtpChange,
            onVerifyOtp     = vm::verifyOtp,
            onBiometricDone = vm::completeBiometric
        )
        else -> MainAppScaffold(vm)
    }
}

// ─── Main App with Bottom Navigation ─────────────────────────────────────────
enum class Screen(
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
) {
    DASHBOARD("DASHBOARD", Icons.Outlined.Dashboard,    Icons.Filled.Dashboard),
    VAULT    ("VAULT",     Icons.Outlined.Lock,         Icons.Filled.Lock),
    SOS      ("SOS",       Icons.Outlined.Warning,      Icons.Filled.Warning),
    SETTINGS ("SETTINGS",  Icons.Outlined.Settings,     Icons.Filled.Settings)
}

@Composable
fun MainAppScaffold(vm: MainViewModel) {
    var currentScreen    by remember { mutableStateOf(Screen.DASHBOARD) }
    var fileToDelete     by remember { mutableStateOf<VaultFile?>(null) }

    val dashboardState  by vm.dashboardState.collectAsStateWithLifecycle()
    val isServiceRunning by vm.isServiceRunning.collectAsStateWithLifecycle()
    val contacts        by vm.contacts.collectAsStateWithLifecycle()
    val phrases         by vm.phrases.collectAsStateWithLifecycle()
    val activePhrase    by vm.activePhrase.collectAsStateWithLifecycle()
    val allFiles        by vm.allFiles.collectAsStateWithLifecycle()
    val vaultStats      by vm.vaultStats.collectAsStateWithLifecycle()
    val impactThreshold by vm.impactThreshold.collectAsStateWithLifecycle()
    val accelSensitivity by vm.accelSensitivity.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Start service on first authenticated launch
    LaunchedEffect(Unit) { vm.startService() }

    Box(modifier = Modifier.fillMaxSize().background(SafeZoneColors.BackgroundDark)) {

        // ── Content Area ──────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 72.dp)) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
    fadeIn(animationSpec = tween(200)) togetherWith
        fadeOut(animationSpec = tween(200))
},
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    Screen.DASHBOARD -> DashboardScreen(
                        dashboardState   = dashboardState,
                        isServiceRunning = isServiceRunning,
                        onToggleService  = { if (it) vm.startService() else vm.stopService() },
                        onToggleMovement = vm::toggleMovementSensors,
                        onSosTrigger     = vm::triggerSos
                    )
                    Screen.VAULT -> VaultScreen(
                        files  = allFiles,
                        stats  = vaultStats,
                        onDeleteRequest = { file ->
                            // Biometric gate for deletion
                            val activity = context as? FragmentActivity
activity?.let {
    BiometricHelper.promptForDeletion(
        activity = it,
        fileName = file.fileName,
        onSuccess = { fileToDelete = file }
    )
}
                        }
                    )
                    Screen.SOS -> SosScreen(
                        isServiceRunning = isServiceRunning,
                        onSosTrigger     = vm::triggerSos,
                        onStartService   = vm::startService,
                        onStopService    = vm::stopService
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        contacts              = contacts,
                        phrases               = phrases,
                        activePhrase          = activePhrase,
                        impactThreshold       = impactThreshold,
                        accelSensitivity      = accelSensitivity,
                        onAddContact          = vm::saveContact,
                        onRemoveContact       = vm::removeContact,
                        onSavePhrase          = vm::savePhrase,
                        onRemovePhrase        = vm::removePhrase,
                        onImpactThresholdChange = vm::setImpactThreshold,
                        onAccelSensitivityChange = vm::setAccelSensitivity,
                        onSyncAll             = { /* trigger sync logic */ }
                    )
                }
            }
        }

        // ── Bottom Navigation Bar ─────────────────────────────────────────────
        SafeZoneBottomBar(
            currentScreen = currentScreen,
            onSelect      = { currentScreen = it },
            modifier      = Modifier.align(Alignment.BottomCenter)
        )
    }

    // ── Delete Auth Dialog ─────────────────────────────────────────────────────
    DeleteAuthOverlay(
        file      = fileToDelete,
        onConfirm = {
            fileToDelete?.let { vm.deleteVaultFile(it) }
            fileToDelete = null
        },
        onDismiss = { fileToDelete = null }
    )
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────
@Composable
fun SafeZoneBottomBar(
    currentScreen: Screen,
    onSelect: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SafeZoneColors.SurfaceDark)
    ) {
        // Top divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SafeZoneColors.Divider)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Screen.entries.forEach { screen ->
                val selected = currentScreen == screen
                val isSos    = screen == Screen.SOS

                if (isSos) {
                    // Special SOS center button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(SafeZoneColors.OrangeCore, CircleShape)
                            .clickable { onSelect(screen) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Warning,
                            contentDescription = "SOS",
                            tint               = Color.Black,
                            modifier           = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Column(
                        modifier            = Modifier
                            .clickable { onSelect(screen) }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector        = if (selected) screen.iconSelected else screen.icon,
                            contentDescription = screen.label,
                            tint               = if (selected) SafeZoneColors.OrangeCore else SafeZoneColors.TextDim,
                            modifier           = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text       = screen.label,
                            color      = if (selected) SafeZoneColors.OrangeCore else SafeZoneColors.TextDim,
                            fontSize   = 9.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
