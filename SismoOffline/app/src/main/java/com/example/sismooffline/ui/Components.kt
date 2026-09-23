package com.example.sismooffline.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatusCard(title: String, status: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title)
            Text(status, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
fun ModeSelector(
    sensorMode: Boolean,
    onSensor: () -> Unit,
    onMonitor: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = sensorMode,
            onClick = onSensor,
            label = { Text("📦 Sensor") }
        )
        FilterChip(
            selected = !sensorMode,
            onClick = onMonitor,
            label = { Text("📱 Monitor") }
        )
    }
}

@Composable
fun DeviceList(
    devices: List<BluetoothDevice>,
    selectedAddress: String?,
    onSelect: (BluetoothDevice) -> Unit
) {
    if (devices.isEmpty()) {
        Text("No hay teléfonos emparejados todavía.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        devices.forEach { device ->
            OutlinedButton(
                onClick = { onSelect(device) },
                modifier = Modifier.fillMaxWidth()
            ) {
                val name = device.name ?: "Dispositivo Bluetooth"
                val selected = device.address == selectedAddress
                Text(if (selected) "✓ $name" else name)
            }
        }
    }
}

@Composable
fun SensitivityControl(value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text("Sensibilidad: ${value.toInt()}/10")
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..10f,
            steps = 8
        )
        LinearProgressIndicator(
            progress = value / 10f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
