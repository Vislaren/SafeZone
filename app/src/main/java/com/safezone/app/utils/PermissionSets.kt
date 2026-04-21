package com.safezone.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object PermissionSets {

    val LOCATION_FOREGROUND = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val LOCATION_BACKGROUND = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val MICROPHONE = arrayOf(Manifest.permission.RECORD_AUDIO)

    val NOTIFICATIONS: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        else emptyArray()

    val BLUETOOTH = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    val CAMERA = arrayOf(Manifest.permission.CAMERA)

    /** Minimal set to launch SOS reliably. */
    val SOS_CRITICAL = LOCATION_FOREGROUND + MICROPHONE + NOTIFICATIONS

    fun allGranted(context: Context, perms: Array<String>): Boolean =
        perms.all {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
}
