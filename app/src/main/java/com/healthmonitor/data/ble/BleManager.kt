package com.healthmonitor.data.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin
import kotlin.random.Random

// Standard BLE UUIDs for heart rate / health devices
private val HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
private val HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
private val OXYGEN_SERVICE = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")
private val OXYGEN_CHARACTERISTIC = UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb")
private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

data class BleHealthData(
    val heartRate: Int,
    val steps: Int,
    val oxygenLevel: Float,
    val deviceAddress: String = "MOCK",
    val rssi: Int = -60
)

sealed class BleState {
    object Idle : BleState()
    object Scanning : BleState()
    data class Connected(val deviceName: String, val address: String) : BleState()
    data class Disconnected(val reason: String) : BleState()
    data class Error(val message: String) : BleState()
}

@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "BleManager"
    private var bluetoothGatt: BluetoothGatt? = null
    private var stepCounter = 0

    /**
     * Check whether BLE is available and enabled.
     */
    fun isBleAvailable(): Boolean {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return btManager.adapter?.isEnabled == true
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun arePermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Produces a continuous stream of health readings.
     * In MOCK mode it generates realistic simulated data.
     * In REAL mode it scans for and connects to the first health sensor found.
     */
    fun observeHealthData(useMock: Boolean = true): Flow<BleHealthData> =
        if (useMock) mockDataFlow() else realBleFlow()

    // ─── Mock data generator ─────────────────────────────────────────────────

    private fun mockDataFlow(): Flow<BleHealthData> = flow {
        var tick = 0
        while (true) {
            tick++
            // Simulate realistic HR with slow sinusoidal drift + noise
            val baseHr = 72 + (sin(tick * 0.05) * 12).toInt()
            val heartRate = (baseHr + Random.nextInt(-3, 4)).coerceIn(40, 180)

            // Steps increment realistically during day hours
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            if (hour in 7..22 && Random.nextFloat() < 0.7f) {
                stepCounter += Random.nextInt(0, 8)
            }

            // SpO2 stays tightly around 97-99%, occasional small dip
            val oxygen = (97f + Random.nextFloat() * 2f - Random.nextFloat() * 1.5f)
                .coerceIn(88f, 100f)

            emit(BleHealthData(
                heartRate = heartRate,
                steps = stepCounter,
                oxygenLevel = oxygen
            ))

            delay(3000L)  // emit every 3 seconds
        }
    }

    // ─── Real BLE implementation ─────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun realBleFlow(): Flow<BleHealthData> = callbackFlow {
        if (!arePermissionsGranted()) {
            close(SecurityException("Missing Bluetooth permissions"))
            return@callbackFlow
        }

        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = btManager.adapter?.bluetoothLeScanner

        if (scanner == null) {
            close(IllegalStateException("Bluetooth not available"))
            return@callbackFlow
        }

        var latestHeartRate = 0
        var latestOxygen = 0f
        var localSteps = stepCounter

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to ${gatt.device.address}")
                    gatt.discoverServices()
                } else {
                    Log.d(TAG, "Disconnected: status=$status")
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                // Enable heart rate notifications
                gatt.getService(HEART_RATE_SERVICE)
                    ?.getCharacteristic(HEART_RATE_MEASUREMENT)
                    ?.let { characteristic ->
                        gatt.setCharacteristicNotification(characteristic, true)
                        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            @Suppress("DEPRECATION")
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            gatt.writeDescriptor(descriptor)
                        }
                    }
            }

            @Suppress("DEPRECATION")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                when (characteristic.uuid) {
                    HEART_RATE_MEASUREMENT -> {
                        val flag = characteristic.properties
                        latestHeartRate = if (flag and 0x01 == 0) {
                            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1) ?: 0
                        } else {
                            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1) ?: 0
                        }
                    }
                    OXYGEN_CHARACTERISTIC -> {
                        latestOxygen = (characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8, 1
                        ) ?: 97).toFloat()
                    }
                }
                if (latestHeartRate > 0) {
                    trySend(BleHealthData(
                        heartRate = latestHeartRate,
                        steps = localSteps,
                        oxygenLevel = latestOxygen.takeIf { it > 0f } ?: 97f,
                        deviceAddress = gatt.device.address
                    ))
                }
            }
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                scanner.stopScan(this)
                bluetoothGatt = result.device.connectGatt(context, false, gattCallback)
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "BLE scan failed: $errorCode")
                close(Exception("BLE scan failed: $errorCode"))
            }
        }

        val filters = listOf(ScanFilter.Builder().build())
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(filters, settings, scanCallback)
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }

        awaitClose {
            try {
                scanner.stopScan(scanCallback)
            } catch (e: SecurityException) {
                // Ignore during close
            }
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
