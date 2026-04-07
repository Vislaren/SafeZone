package com.safezone.utils

import android.Manifest
import android.os.Build

object PermissionUtils {

    /** All permissions required for full SafeZone functionality */
    val ALL_REQUIRED: Array<String> by lazy {
        buildList {
            // Always required
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.SEND_SMS)
            add(Manifest.permission.READ_CONTACTS)
            add(Manifest.permission.READ_PHONE_STATE)
            add(Manifest.permission.CALL_PHONE)

            // Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    /** Permissions needed only for initial auth/SMS features */
    val AUTH_PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SEND_SMS
    )

    /** Core monitoring permissions */
    val MONITORING_PERMISSIONS: Array<String> by lazy {
        buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }.toTypedArray()
    }

    /** Human-readable label for each permission */
    fun label(permission: String): String = when (permission) {
        Manifest.permission.CAMERA               -> "Camera"
        Manifest.permission.RECORD_AUDIO         -> "Microphone"
        Manifest.permission.ACCESS_FINE_LOCATION -> "Precise Location"
        Manifest.permission.ACCESS_COARSE_LOCATION -> "Approximate Location"
        Manifest.permission.SEND_SMS             -> "Send SMS"
        Manifest.permission.READ_CONTACTS        -> "Read Contacts"
        Manifest.permission.READ_PHONE_STATE     -> "Phone State"
        Manifest.permission.CALL_PHONE           -> "Make Calls"
        Manifest.permission.POST_NOTIFICATIONS   -> "Notifications"
        else -> permission.substringAfterLast(".")
    }

    /** Description for each permission shown in rationale UI */
    fun rationale(permission: String): String = when (permission) {
        Manifest.permission.CAMERA               -> "Required for silent video surveillance after trigger"
        Manifest.permission.RECORD_AUDIO         -> "Required for voice trigger detection and audio logging"
        Manifest.permission.ACCESS_FINE_LOCATION -> "Required for GPS coordinate dispatch to emergency contacts"
        Manifest.permission.SEND_SMS             -> "Required to send emergency alerts to your 5 contacts"
        Manifest.permission.READ_CONTACTS        -> "Required to import contacts from your phonebook"
        Manifest.permission.CALL_PHONE           -> "Required for SOS call dispatch"
        Manifest.permission.POST_NOTIFICATIONS   -> "Required to show service status notifications"
        else -> "Required for core functionality"
    }
}
