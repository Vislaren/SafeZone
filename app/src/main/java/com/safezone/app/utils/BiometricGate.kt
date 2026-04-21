package com.safezone.app.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object BiometricGate {

    enum class Availability { AVAILABLE, NO_HARDWARE, NOT_ENROLLED, UNAVAILABLE }

    fun availability(context: Context): Availability {
        val bm = BiometricManager.from(context)
        return when (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Availability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Availability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Availability.NOT_ENROLLED
            else -> Availability.UNAVAILABLE
        }
    }

    sealed class Result {
        data object Success : Result()
        data class Error(val code: Int, val message: String) : Result()
        data object Failed : Result()
        data object UserCancel : Result()
    }

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "Authentication",
        subtitle: String = "Scan fingerprint to continue",
        negative: String = "Cancel"
    ): Result = suspendCancellableCoroutine { cont ->
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) {
                    if (cont.isActive) cont.resume(Result.Success)
                }
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    if (cont.isActive) {
                        if (code == BiometricPrompt.ERROR_USER_CANCELED ||
                            code == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        ) cont.resume(Result.UserCancel)
                        else cont.resume(Result.Error(code, msg.toString()))
                    }
                }
                override fun onAuthenticationFailed() {
                    // Fingerprint didn't match — user can retry; don't resume yet.
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negative)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        prompt.authenticate(info)
    }
}
