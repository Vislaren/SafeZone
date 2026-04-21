package com.safezone.app.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.safezone.app.MainActivity
import com.safezone.app.R
import com.safezone.app.SafeZoneApp
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

/**
 * BLE fallback. Advertises a SafeZone service UUID when in SOS mode and scans for peers.
 * Device peripheral mode isn't universal — we fall back to scan-only on unsupported hardware.
 */
@AndroidEntryPoint
class BLEService : LifecycleService() {

    private var mode: Mode = Mode.SCAN
    private var adapter: BluetoothAdapter? = null
    private var advertiseCb: AdvertiseCallback? = null
    private var scanCb: ScanCallback? = null

    enum class Mode { SCAN, BROADCAST }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        mode = when (intent?.action) {
            ACTION_BROADCAST -> Mode.BROADCAST
            else -> Mode.SCAN
        }
        if (!hasBtPermissions()) { stopSelf(); return START_NOT_STICKY }
        startForegroundWithNotif()

        val mgr = getSystemService(BluetoothManager::class.java)
        adapter = mgr?.adapter
        if (adapter?.isEnabled != true) { stopSelf(); return START_NOT_STICKY }

        when (mode) {
            Mode.BROADCAST -> { startAdvertise(); startScan() }
            Mode.SCAN -> startScan()
        }
        return START_STICKY
    }

    private fun hasBtPermissions(): Boolean {
        val scan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) ==
            PackageManager.PERMISSION_GRANTED
        val adv = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) ==
            PackageManager.PERMISSION_GRANTED
        val conn = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
        return scan && adv && conn
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertise() {
        val advertiser = adapter?.bluetoothLeAdvertiser ?: return // peripheral mode unsupported
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SAFEZONE_SOS_UUID))
            .build()
        val cb = object : AdvertiseCallback() {}
        advertiser.startAdvertising(settings, data, cb)
        advertiseCb = cb
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        val scanner = adapter?.bluetoothLeScanner ?: return
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SAFEZONE_SOS_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()
        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Peer SafeZone device detected. A production build would:
                //  - de-dupe by device address
                //  - notify the user with an IncomingAlert row in the local DB
                //  - surface it in the AlertScreen
                broadcastPeer(result.device.address)
            }
        }
        scanner.startScan(listOf(filter), settings, cb)
        scanCb = cb
    }

    private fun broadcastPeer(address: String) {
        val i = Intent(ACTION_PEER_DETECTED).putExtra(EXTRA_PEER_ADDRESS, address)
        sendBroadcast(i)
    }

    private fun startForegroundWithNotif() {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(this, SafeZoneApp.CH_BLE)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(getString(R.string.notif_ble_title))
            .setContentText(getString(R.string.notif_ble_text))
            .setOngoing(true)
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        advertiseCb?.let { runCatching { adapter?.bluetoothLeAdvertiser?.stopAdvertising(it) } }
        scanCb?.let { runCatching { adapter?.bluetoothLeScanner?.stopScan(it) } }
        advertiseCb = null; scanCb = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? { super.onBind(intent); return null }

    companion object {
        const val NOTIF_ID = 44
        const val ACTION_BROADCAST = "com.safezone.app.action.BLE_BROADCAST"
        const val ACTION_SCAN = "com.safezone.app.action.BLE_SCAN"
        const val ACTION_PEER_DETECTED = "com.safezone.app.action.BLE_PEER_DETECTED"
        const val EXTRA_PEER_ADDRESS = "peer_address"
        /** SafeZone SOS service UUID — kept intentionally stable so peers can filter. */
        val SAFEZONE_SOS_UUID: UUID = UUID.fromString("5A5E2A17-0000-4E50-B000-5A5E2A175A5E")

        fun scan(context: Context) {
            context.startForegroundService(
                Intent(context, BLEService::class.java).setAction(ACTION_SCAN)
            )
        }
        fun broadcast(context: Context) {
            context.startForegroundService(
                Intent(context, BLEService::class.java).setAction(ACTION_BROADCAST)
            )
        }
        fun stop(context: Context) {
            context.stopService(Intent(context, BLEService::class.java))
        }
    }
}
