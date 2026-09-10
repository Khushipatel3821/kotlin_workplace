package com.fitpulse.app.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.fitpulse.app.domain.model.MotionSample
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class SensorMotionDataSource(private val context: Context) {

    private val sensorManager: SensorManager? by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }

    private val accelerometer: Sensor? by lazy {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private val stepDetector: Sensor? by lazy {
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    private val stepCounter: Sensor? by lazy {
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    val isStepSensorAvailable: Boolean
        get() = stepDetector != null || stepCounter != null

    /**
     * Emits continuous 3-axis motion samples for real-time rep counting and cadence tracking.
     */
    fun observeMotionSamples(samplingRateDelay: Int = SensorManager.SENSOR_DELAY_GAME): Flow<MotionSample> = callbackFlow {
        val manager = sensorManager
        val sensor = accelerometer

        if (manager == null || sensor == null) {
            close()
            return@callbackFlow
        }

        var gravX = 0f
        var gravY = 9.8f
        var gravZ = 0f
        val alpha = 0.8f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // Approximate gravity
                    gravX = alpha * gravX + (1f - alpha) * x
                    gravY = alpha * gravY + (1f - alpha) * y
                    gravZ = alpha * gravZ + (1f - alpha) * z

                    val linX = x - gravX
                    val linY = y - gravY
                    val linZ = z - gravZ

                    val magnitude = sqrt(linX * linX + linY * linY + linZ * linZ)
                    val timestampMs = System.currentTimeMillis()

                    val sample = MotionSample(
                        timestampMs = timestampMs,
                        rawX = x,
                        rawY = y,
                        rawZ = z,
                        filteredMagnitude = magnitude,
                        verticalAcceleration = linY
                    )

                    trySend(sample)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        manager.registerListener(listener, sensor, samplingRateDelay)

        awaitClose {
            manager.unregisterListener(listener)
        }
    }

    /**
     * Emits step events from hardware step detector or step counter.
     */
    fun observeStepEvents(): Flow<Int> = callbackFlow {
        val manager = sensorManager
        val sensor = stepDetector ?: stepCounter

        if (manager == null || sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return
                if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                    trySend(1) // Step detected
                } else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    trySend(event.values[0].toInt())
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            manager.unregisterListener(listener)
        }
    }
}
