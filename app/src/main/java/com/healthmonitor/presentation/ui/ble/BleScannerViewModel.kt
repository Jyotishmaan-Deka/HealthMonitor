package com.healthmonitor.presentation.ui.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannedDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val isHealthDevice: Boolean
)

data class BleScanUiState(
    val isScanning: Boolean = false,
    val devices: List<ScannedDevice> = emptyList(),
    val selectedAddress: String? = null,
    val permissionDenied: Boolean = false
)

// Known health service UUIDs (partial list)
private val HEALTH_SERVICE_UUIDS = setOf(
    "0000180d",  // Heart Rate
    "00001822",  // Pulse Oximeter
    "0000181c",  // User Data
    "00001809"   // Health Thermometer
)

@HiltViewModel
class BleScannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(BleScanUiState())
    val state: StateFlow<BleScanUiState> = _state.asStateFlow()

    private var scanJob: Job? = null
    private val foundDevices = mutableMapOf<String, ScannedDevice>()

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

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!arePermissionsGranted()) {
            _state.update { it.copy(permissionDenied = true) }
            return
        }

        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = btManager.adapter?.bluetoothLeScanner ?: run {
            // This can happen if Bluetooth is turned off
            _state.update { it.copy(isScanning = false) }
            return
        }

        foundDevices.clear()
        _state.update { it.copy(isScanning = true, devices = emptyList(), permissionDenied = false) }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = result.device.name ?: "Unknown device"
                val serviceUuids = result.scanRecord?.serviceUuids
                    ?.map { it.uuid.toString().take(8).lowercase() }
                    ?: emptyList()
                val isHealth = serviceUuids.any { it in HEALTH_SERVICE_UUIDS }

                val device = ScannedDevice(
                    address = result.device.address,
                    name = name,
                    rssi = result.rssi,
                    isHealthDevice = isHealth
                )
                foundDevices[device.address] = device

                _state.update { s ->
                    s.copy(
                        devices = foundDevices.values
                            .sortedWith(compareByDescending<ScannedDevice> { it.isHealthDevice }
                                .thenByDescending { it.rssi })
                    )
                }
            }

            override fun onScanFailed(errorCode: Int) {
                _state.update { it.copy(isScanning = false) }
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(null, settings, callback)

            scanJob = viewModelScope.launch {
                delay(10_000L)  // auto-stop after 10s
                scanner.stopScan(callback)
                _state.update { it.copy(isScanning = false) }
            }
        } catch (e: SecurityException) {
            _state.update { it.copy(isScanning = false, permissionDenied = true) }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _state.update { it.copy(isScanning = false) }
    }

    fun selectDevice(address: String) {
        _state.update { it.copy(selectedAddress = address) }
    }

    override fun onCleared() {
        stopScan()
    }
}
