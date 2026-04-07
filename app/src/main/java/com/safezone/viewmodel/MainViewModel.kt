package com.safezone.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.safezone.SecurityService
import com.safezone.data.db.*
import com.safezone.data.preferences.SessionManager
import com.safezone.data.repository.ContactRepository
import com.safezone.data.repository.SecurityRepository
import com.safezone.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ─── UI State Models ──────────────────────────────────────────────────────────
data class DashboardState(
    val systemActive: Boolean    = true,
    val movementSensors: Boolean = true,
    val environmentRisk: String  = "Low Risk",
    val microphone: Boolean      = true,
    val gps: Boolean             = true,
    val visionStream: Boolean    = true,
    val latencyMs: Int           = 12,
    val pingStatus: String       = "PING_RESP_SUCCESS",
    val vaultAutoLock: Boolean   = true
)

data class AuthState(
    val phoneNumber: String       = "",
    val otpCode: String           = "",
    val isVerified: Boolean       = false,
    val isLoading: Boolean        = false,
    val errorMessage: String?     = null,
    val currentStep: AuthStep     = AuthStep.PHONE
)

enum class AuthStep { PHONE, OTP, BIOMETRIC, DONE }

data class VaultStats(
    val totalAssets: Int    = 0,
    val storageGb: Double   = 0.0
)

// ─── ViewModel ────────────────────────────────────────────────────────────────
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val sessionManager: SessionManager,
    private val contactRepository: ContactRepository,
    private val securityRepository: SecurityRepository,
    private val vaultRepository: VaultRepository
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    // ── Session / Auth ────────────────────────────────────────────────────────
    val isAuthenticated: StateFlow<Boolean> = sessionManager.isAuthenticated
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val onboardingDone: StateFlow<Boolean> = sessionManager.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isServiceRunning: StateFlow<Boolean> = sessionManager.isServiceRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _authState = MutableStateFlow(AuthState())
val authState: StateFlow<AuthState> = _authState.asStateFlow()

// Holds the generated OTP for verification — never exposed to UI
private var _pendingOtp: String = ""

    // ── Contacts ──────────────────────────────────────────────────────────────
    val contacts: StateFlow<List<Contact>> = contactRepository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Trigger Phrases ───────────────────────────────────────────────────────
    val phrases: StateFlow<List<TriggerPhrase>> = securityRepository.observePhrases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePhrase: StateFlow<String> = sessionManager.activePhrase
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Protocol Omega Delta")

    // ── Vault ─────────────────────────────────────────────────────────────────
    val allFiles: StateFlow<List<VaultFile>> = vaultRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val audioFiles: StateFlow<List<VaultFile>> = vaultRepository.observeByType(FileType.AUDIO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoFiles: StateFlow<List<VaultFile>> = vaultRepository.observeByType(FileType.VIDEO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultStats: StateFlow<VaultStats> = allFiles
        .map { files ->
            VaultStats(
                totalAssets = files.size,
                storageGb   = files.sumOf { it.sizeMb } / 1024.0
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VaultStats())

    // ── Dashboard ─────────────────────────────────────────────────────────────
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    // ── Settings ──────────────────────────────────────────────────────────────
    val impactThreshold: StateFlow<Float> = sessionManager.impactThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.82f)

    val accelSensitivity: StateFlow<Float> = sessionManager.accelSensitivity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.45f)

    // ─── Auth Actions ─────────────────────────────────────────────────────────
    fun onPhoneNumberChange(raw: String) {
    // Strip everything except digits
    val digits = raw.filter { it.isDigit() }.take(9)
    _authState.update { it.copy(phoneNumber = digits, errorMessage = null) }
}

    fun onOtpChange(otp: String) {
        _authState.update { it.copy(otpCode = otp, errorMessage = null) }
    }

    fun initiateLink() {
    val phone = _authState.value.phoneNumber.trim()

    if (phone.isBlank()) {
        _authState.update { it.copy(errorMessage = "Please enter a phone number") }
        return
    }

    // Generate a 6-digit OTP and send it via SMS
    _pendingOtp = (100000..999999).random().toString()

    _authState.update { it.copy(isLoading = true) }

    viewModelScope.launch {
        sessionManager.setPhoneNumber(phone)

        val sent = sendOtpSms(phone, _pendingOtp)
        if (sent) {
            _authState.update { it.copy(isLoading = false, currentStep = AuthStep.OTP) }
        } else {
            _authState.update {
                it.copy(
                    isLoading    = false,
                    errorMessage = "Failed to send SMS. Check the number and try again."
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun sendOtpSms(phone: String, otp: String): Boolean {
    return try {
        val message = "Your SafeZone verification code is: $otp"
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ctx.getSystemService(android.telephony.SmsManager::class.java)
        } else {
            android.telephony.SmsManager.getDefault()
        }
        smsManager.sendTextMessage(phone, null, message, null, null)
        true
    } catch (e: Exception) {
        android.util.Log.e("SafeZone::OTP", "SMS send failed: ${e.message}")
        false
    }
}

    fun verifyOtp() {
    val code = _authState.value.otpCode.trim()

    if (code.length < 6) {
        _authState.update { it.copy(errorMessage = "Enter the full 6-digit code") }
        return
    }

    if (code != _pendingOtp) {
        _authState.update { it.copy(errorMessage = "Incorrect code. Please try again.") }
        return
    }

    _authState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
        val token = UUID.randomUUID().toString()
        sessionManager.setSessionToken(token)
        _pendingOtp = "" // Clear OTP after successful use
        _authState.update { it.copy(isLoading = false, currentStep = AuthStep.BIOMETRIC) }
    }
}

    fun completeBiometric() {
        viewModelScope.launch {
            sessionManager.setAuthenticated(true)
            sessionManager.setBiometricEnabled(true)
            sessionManager.setOnboardingDone(true)
            _authState.update { it.copy(isVerified = true, currentStep = AuthStep.DONE) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            stopService()
            sessionManager.clearSession()
        }
    }

    // ─── Service Control ──────────────────────────────────────────────────────
    fun startService() {
        val intent = Intent(ctx, SecurityService::class.java).apply {
            action = SecurityService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent)
        } else {
            ctx.startService(intent)
        }
    }

    fun stopService() {
        ctx.startService(Intent(ctx, SecurityService::class.java).apply {
            action = SecurityService.ACTION_STOP
        })
    }

    fun triggerSos() {
        ctx.startService(Intent(ctx, SecurityService::class.java).apply {
            action = SecurityService.ACTION_SOS
        })
    }

    // ─── Contact Management ───────────────────────────────────────────────────
    fun saveContact(name: String, phone: String, slot: Int) {
        viewModelScope.launch {
            contactRepository.saveContact(name, phone, slot)
        }
    }

    fun removeContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
        }
    }

    // ─── Phrase Management ────────────────────────────────────────────────────
    fun savePhrase(phrase: String, slot: Int) {
        viewModelScope.launch {
            securityRepository.savePhrase(phrase, slot)
            sessionManager.setActivePhrase(phrase)
        }
    }

    fun removePhrase(phrase: TriggerPhrase) {
        viewModelScope.launch {
            securityRepository.deletePhrase(phrase)
        }
    }

    // ─── Vault / File Management ──────────────────────────────────────────────
    fun deleteVaultFile(file: VaultFile) {
        viewModelScope.launch {
            try {
                java.io.File(file.filePath).delete()
            } catch (_: Exception) {}
            vaultRepository.delete(file)
        }
    }

    // ─── Settings ─────────────────────────────────────────────────────────────
    fun setImpactThreshold(value: Float) {
        viewModelScope.launch { sessionManager.setImpactThreshold(value) }
    }

    fun setAccelSensitivity(value: Float) {
        viewModelScope.launch { sessionManager.setAccelSensitivity(value) }
    }

    fun toggleMovementSensors(enabled: Boolean) {
        _dashboardState.update { it.copy(movementSensors = enabled) }
    }
}
