package com.example.sismooffline.core

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

/**
 * Detector experimental para la demostración escolar.
 * Mide movimiento del teléfono con el acelerómetro. No predice terremotos.
 */
class EarthquakeDetector(
    private val sensorManager: SensorManager,
    private val onDetection: (movement: Float) -> Unit
) : SensorEventListener {

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val gravity = FloatArray(3)
    private var gravityInitialized = false
    private var threshold = 2.2f
    private var hitCount = 0
    private var windowStart = 0L
    private var cooldownUntil = 0L

    fun setSensitivity(sensitivity: Float) {
        // 1 = menos sensible, 10 = más sensible.
        threshold = 4.2f - ((sensitivity - 1f) / 9f) * 2.8f
    }

    fun start(): Boolean {
        val sensor = accelerometer ?: return false
        hitCount = 0
        windowStart = 0L
        cooldownUntil = 0L
        gravityInitialized = false

        return sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        gravityInitialized = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        if (!gravityInitialized) {
            gravity[0] = event.values[0]
            gravity[1] = event.values[1]
            gravity[2] = event.values[2]
            gravityInitialized = true
            return
        }

        val alpha = 0.80f
        gravity[0] = alpha * gravity[0] + (1f - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1f - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1f - alpha) * event.values[2]

        val x = event.values[0] - gravity[0]
        val y = event.values[1] - gravity[1]
        val z = event.values[2] - gravity[2]
        val movement = sqrt(x * x + y * y + z * z)

        val now = SystemClock.elapsedRealtime()
        if (movement < threshold || now < cooldownUntil) return

        if (windowStart == 0L || now - windowStart > 900L) {
            windowStart = now
            hitCount = 0
        }

        hitCount++

        // Exigir tres muestras cercanas reduce bastante los disparos aislados.
        if (hitCount >= 3) {
            cooldownUntil = now + 7_000L
            hitCount = 0
            windowStart = 0L
            onDetection(movement)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
