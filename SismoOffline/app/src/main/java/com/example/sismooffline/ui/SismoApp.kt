package com.example.sismooffline.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.sismooffline.core.BluetoothManager
import com.example.sismooffline.core.EarthquakeDetector

@Composable
fun SismoApp(
    onRequestBluetoothPermission: () -> Unit,
    onOpenBluetoothSettings: () -> Unit
) {
    val context = LocalContext.current
    val bluetoothAdapter = remember { BluetoothAdapter.getDefaultAdapter() }
    val bluetoothManager = remember { BluetoothManager(bluetoothAdapter) }
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    var alertMovement by remember { mutableStateOf<Float?>(null) }
    var sensorMode by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("Listo") }
    var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var sensitivity by remember { mutableStateOf(7f) }
    var monitoring by remember { mutableStateOf(false) }

    var bluetoothReady by remember {
        mutableStateOf(hasBluetoothPermission(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                bluetoothReady = hasBluetoothPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val detector = remember {
        EarthquakeDetector(sensorManager) { movement ->
            bluetoothManager.send("EARTHQUAKE|$movement")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            detector.stop()
            bluetoothManager.close()
        }
    }

    LaunchedEffect(sensorMode, bluetoothReady) {
        if (!sensorMode && bluetoothReady) {
            devices = try {
                bluetoothManager.pairedDevices()
            } catch (_: SecurityException) {
                emptyList()
            }
        }
    }

    if (alertMovement != null) {
        AlertScreen(
            movement = alertMovement ?: 0f,
            onDismiss = { alertMovement = null }
        )
        return
    }

    MainScreen(
        sensorMode = sensorMode,
        onSensorMode = {
            detector.stop()
            bluetoothManager.close()
            monitoring = false
            sensorMode = true
            status = "Modo sensor"
        },
        onMonitorMode = {
            detector.stop()
            bluetoothManager.close()
            monitoring = false
            sensorMode = false
            status = if (bluetoothReady) "Modo monitor" else "Falta permiso de Bluetooth"
            if (bluetoothReady) {
                devices = try {
                    bluetoothManager.pairedDevices()
                } catch (_: SecurityException) {
                    emptyList()
                }
            }
        },
        status = status,
        devices = devices,
        selectedDevice = selectedDevice,
        onSelectDevice = { selectedDevice = it },
        sensitivity = sensitivity,
        onSensitivityChange = {
            sensitivity = it
            detector.setSensitivity(it)
        },
        monitoring = monitoring,
        onStartSensor = {
            if (!bluetoothReady) {
                status = "Necesitas permitir Dispositivos cercanos."
                onRequestBluetoothPermission()
            } else if (!bluetoothManager.isEnabled()) {
                status = "Bluetooth está apagado. Ábrelo en ajustes."
                onOpenBluetoothSettings()
            } else {
                detector.setSensitivity(sensitivity)
                bluetoothManager.startServer(
                    onMessage = { message -> handleMessage(message) { alertMovement = it } },
                    onStatus = { status = it }
                )

                val started = detector.start()
                if (started) {
                    monitoring = true
                    status = "🟢 Detector activo; esperando movimiento"
                } else {
                    detector.stop()
                    bluetoothManager.close()
                    status = "Este teléfono no tiene acelerómetro compatible."
                }
            }
        },
        onConnectMonitor = {
            if (!bluetoothReady) {
                status = "Necesitas permitir Dispositivos cercanos."
                onRequestBluetoothPermission()
            } else if (!bluetoothManager.isEnabled()) {
                status = "Bluetooth está apagado. Ábrelo en ajustes."
                onOpenBluetoothSettings()
            } else {
                selectedDevice?.let { device ->
                    monitoring = true
                    status = "Conectando…"
                    bluetoothManager.connect(
                        device = device,
                        onMessage = { message -> handleMessage(message) { alertMovement = it } },
                        onStatus = { status = it }
                    )
                }
            }
        },
        onSimulate = {
            val sent = bluetoothManager.send("EARTHQUAKE|3.50")
            status = if (sent) {
                "⚡ Evento de prueba enviado"
            } else {
                "⚠️ No hay enlace Bluetooth activo todavía"
            }
        },
        onRefreshDevices = {
            if (!bluetoothReady) {
                status = "Necesitas permitir Dispositivos cercanos."
                onRequestBluetoothPermission()
            } else {
                devices = try {
                    bluetoothManager.pairedDevices()
                } catch (_: SecurityException) {
                    emptyList()
                }
                status = "Lista actualizada"
            }
        },
        onOpenBluetoothSettings = onOpenBluetoothSettings,
        onRequestBluetoothPermission = onRequestBluetoothPermission,
        bluetoothReady = bluetoothReady,
        bluetoothAvailable = bluetoothManager.isAvailable()
    )
}

private fun handleMessage(message: String, onEarthquake: (Float) -> Unit) {
    if (message.startsWith("EARTHQUAKE|")) {
        val movement = message.substringAfter("EARTHQUAKE|").toFloatOrNull() ?: 0f
        onEarthquake(movement)
    }
}

private fun hasBluetoothPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
}
