package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

data class CompassState(
    val azimuth: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isSensorAvailable: Boolean = true
)

class CompassSensorManager(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var hasVibratedForCurrentAlignment = false

    fun getCompassFlow(): Flow<CompassState> = callbackFlow {
        if (sensorManager == null) {
            trySend(CompassState(isSensorAvailable = false))
            close()
            return@callbackFlow
        }

        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var lastAzimuth = 0f
        var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val lastAccelerometer = FloatArray(3)
        val lastMagnetometer = FloatArray(3)
        var lastAccelerometerSet = false
        var lastMagnetometerSet = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var azimuthDeg = 0f
                var updated = false

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    currentAccuracy = event.accuracy
                    updated = true
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                    lastAccelerometerSet = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                    lastMagnetometerSet = true
                    currentAccuracy = event.accuracy
                }

                if (!updated && lastAccelerometerSet && lastMagnetometerSet) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        updated = true
                    }
                }

                if (updated) {
                    azimuthDeg = (azimuthDeg + 360f) % 360f

                    // Smooth jitter with shortest angular distance
                    var diff = azimuthDeg - lastAzimuth
                    while (diff < -180f) diff += 360f
                    while (diff > 180f) diff -= 360f
                    val smoothed = (lastAzimuth + diff * 0.25f + 360f) % 360f
                    lastAzimuth = smoothed

                    trySend(
                        CompassState(
                            azimuth = smoothed,
                            accuracy = currentAccuracy,
                            isSensorAvailable = true
                        )
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                currentAccuracy = accuracy
            }
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        } else {
            trySend(CompassState(isSensorAvailable = false))
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun vibrateOnceOnQiblaFound() {
        if (!hasVibratedForCurrentAlignment) {
            hasVibratedForCurrentAlignment = true
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(70)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun resetQiblaVibrationLock() {
        hasVibratedForCurrentAlignment = false
    }
}
