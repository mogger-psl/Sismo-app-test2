package com.example.sismooffline.core

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

class BluetoothManager(
    private val adapter: BluetoothAdapter?
) {

    companion object {
        private const val SERVICE_NAME = "SismoOffline"
        private const val PREFIX = "SISMO|"
        private val SERVICE_UUID: UUID =
            UUID.fromString("4d0d0f71-2fa3-4c5c-8d7a-2f4a68df7b12")
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var serverSocket: BluetoothServerSocket? = null
    private var socket: BluetoothSocket? = null
    private var writer: BufferedWriter? = null
    @Volatile private var listening = false

    fun isAvailable(): Boolean = adapter != null

    @SuppressLint("MissingPermission")
    fun isEnabled(): Boolean = adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun pairedDevices(): List<BluetoothDevice> {
        return adapter?.bondedDevices
            ?.toList()
            ?.sortedBy { it.name ?: it.address }
            ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    fun startServer(
        onMessage: (String) -> Unit,
        onStatus: (String) -> Unit
    ) {
        if (adapter == null) {
            postStatus(onStatus, "Bluetooth no disponible en este teléfono")
            return
        }
        if (!adapter.isEnabled) {
            postStatus(onStatus, "Bluetooth está apagado. Ábrelo en ajustes.")
            return
        }
        if (listening) return

        listening = true
        postStatus(onStatus, "🟡 Sensor listo. Esperando al monitor…")

        Thread {
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(
                    SERVICE_NAME,
                    SERVICE_UUID
                )

                while (listening) {
                    val incoming = serverSocket?.accept() ?: break
                    attachSocket(incoming, onMessage, onStatus)
                }
            } catch (e: Exception) {
                if (listening) {
                    postStatus(onStatus, "Error del servidor Bluetooth: ${e.message ?: "desconocido"}")
                }
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun connect(
        device: BluetoothDevice,
        onMessage: (String) -> Unit,
        onStatus: (String) -> Unit
    ) {
        if (adapter == null) {
            postStatus(onStatus, "Bluetooth no disponible en este teléfono")
            return
        }
        if (!adapter.isEnabled) {
            postStatus(onStatus, "Bluetooth está apagado. Ábrelo en ajustes.")
            return
        }

        Thread {
            try {
                closeSocketOnly()
                postStatus(onStatus, "🔗 Conectando con ${device.name ?: "el sensor"}…")
                val newSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                socket = newSocket
                newSocket.connect()
                attachSocket(newSocket, onMessage, onStatus)
                send("HELLO")
            } catch (e: Exception) {
                closeSocketOnly()
                postStatus(onStatus, "No se pudo conectar. Comprueba que ambos estén emparejados.")
            }
        }.start()
    }

    @Synchronized
    @SuppressLint("MissingPermission")
    fun send(message: String): Boolean {
        val currentWriter = writer ?: return false
        return try {
            currentWriter.write(PREFIX + message)
            currentWriter.newLine()
            currentWriter.flush()
            true
        } catch (_: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun attachSocket(
        newSocket: BluetoothSocket,
        onMessage: (String) -> Unit,
        onStatus: (String) -> Unit
    ) {
        try {
            socket = newSocket
            writer = BufferedWriter(OutputStreamWriter(newSocket.outputStream, Charsets.UTF_8))
            postStatus(onStatus, "🟢 Bluetooth conectado")

            Thread {
                try {
                    val reader = BufferedReader(
                        InputStreamReader(newSocket.inputStream, Charsets.UTF_8)
                    )
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.startsWith(PREFIX)) {
                            onMessage(line.removePrefix(PREFIX))
                        }
                    }
                } catch (_: Exception) {
                    if (listening || socket != null) {
                        postStatus(onStatus, "🟠 Bluetooth desconectado")
                    }
                }
            }.start()
        } catch (e: Exception) {
            postStatus(onStatus, "Error de Bluetooth: ${e.message ?: "desconocido"}")
        }
    }

    fun close() {
        listening = false
        try { serverSocket?.close() } catch (_: Exception) { }
        closeSocketOnly()
        serverSocket = null
    }

    private fun closeSocketOnly() {
        try { socket?.close() } catch (_: Exception) { }
        socket = null
        writer = null
    }

    private fun postStatus(callback: (String) -> Unit, message: String) {
        mainHandler.post { callback(message) }
    }
}
