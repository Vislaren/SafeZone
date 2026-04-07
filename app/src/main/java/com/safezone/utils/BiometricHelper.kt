package com.safezone.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun canAuthenticate(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Authentication failed — try again")
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        prompt.authenticate(promptInfo)
    }

    // Convenience wrappers for specific app flows
    fun promptForDeletion(
        activity: FragmentActivity,
        fileName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) = showPrompt(
        activity   = activity,
        title      = "DELETE AUTHORIZATION",
        subtitle   = "Level 4 Security Clearance Required",
        description = "Permanently purge $fileName — this action is irreversible.",
        onSuccess  = onSuccess,
        onError    = onError
    )

    fun promptForSettings(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) = showPrompt(
        activity    = activity,
        title       = "SECURE ENCLAVE",
        subtitle    = "Identity Verification Required",
        description = "Authenticate to access system configuration.",
        onSuccess   = onSuccess,
        onError     = onError
    )
}
