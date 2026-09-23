package com.example.sismooffline.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    sensorMode: Boolean,
    onSensorMode: () -> Unit,
    onMonitorMode: () -> Unit,
    status: String,
    devices: List<BluetoothDevice>,
    selectedDevice: BluetoothDevice?,
    onSelectDevice: (BluetoothDevice) -> Unit,
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    monitoring: Boolean,
    onStartSensor: () -> Unit,
    onConnectMonitor: () -> Unit,
    onSimulate: () -> Unit,
    onRefreshDevices: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onRequestBluetoothPermission: () -> Unit,
    bluetoothReady: Boolean,
    bluetoothAvailable: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🌎 Sismo Offline", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Detector experimental de movimiento con acelerómetro + Bluetooth",
            style = MaterialTheme.typography.bodyMedium
        )

        ModeSelector(sensorMode, onSensorMode, onMonitorMode)
        StatusCard(
            title = if (sensorMode) "Estado del sensor" else "Estado del enlace",
            status = status
        )

        HorizontalDivider()

        if (!bluetoothAvailable) {
            Text("Este teléfono no tiene Bluetooth disponible.")
        } else if (!bluetoothReady) {
            ActionButton(
                text = "🔐 Dar permiso de Dispositivos cercanos",
                onClick = onRequestBluetoothPermission
            )
            Text(
                "Android 12/13 necesita este permiso para comunicarse con el teléfono emparejado.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (sensorMode) {
            Text("Teléfono dentro de la caja", style = MaterialTheme.typography.titleMedium)
            Text(
                "Coloca el teléfono sujeto dentro de la caja. El detector busca varias sacudidas próximas para evitar activaciones por un golpe aislado.",
                style = MaterialTheme.typography.bodyMedium
            )
            SensitivityControl(sensitivity, onSensitivityChange)
            ActionButton(
                text = if (monitoring) "🟢 Detector activo" else "▶ Iniciar detector",
                enabled = !monitoring && bluetoothAvailable,
                onClick = onStartSensor
            )
        } else {
            Text("Teléfono que mostrará la alerta", style = MaterialTheme.typography.titleMedium)
            Text(
                "Primero empareja ambos teléfonos desde Ajustes > Bluetooth. Después selecciona aquí el teléfono sensor.",
                style = MaterialTheme.typography.bodyMedium
            )
            ActionButton("⚙ Abrir ajustes Bluetooth", onClick = onOpenBluetoothSettings)
            ActionButton("↻ Actualizar teléfonos", onClick = onRefreshDevices)
            DeviceList(
                devices = devices,
                selectedAddress = selectedDevice?.address,
                onSelect = onSelectDevice
            )
            ActionButton(
                text = if (monitoring) "🟢 Monitor conectado" else "🔗 Conectar con sensor",
                enabled = selectedDevice != null && !monitoring && bluetoothReady,
                onClick = onConnectMonitor
            )
        }

        Spacer(Modifier.height(2.dp))
        HorizontalDivider()
        Text("Prueba de exposición", style = MaterialTheme.typography.titleMedium)
        ActionButton("⚡ Simular temblor", onClick = onSimulate)
        Text(
            "Usa este botón para comprobar el enlace Bluetooth sin depender de la sacudida física de la caja.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
