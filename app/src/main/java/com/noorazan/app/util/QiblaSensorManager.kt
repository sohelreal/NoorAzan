package com.noorazan.app.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class QiblaSensorManager(
    context: Context,
    private val onHeadingUpdate: (trueAzimuth: Float, needsCalibration: Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var smoothedAzimuth = 0f
    private var declination = 0f
    private var needsCalibration = false

    fun setDeclination(declinationDegrees: Float) {
        this.declination = declinationDegrees
    }

    fun start() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.85f
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
            }
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientation)
            var magneticAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            magneticAzimuth = (magneticAzimuth + 360f) % 360f

            // Apply Magnetic Declination to obtain True North
            val trueAzimuth = (magneticAzimuth + declination + 360f) % 360f

            // Smooth circular angle transitions
            smoothedAzimuth = lowPassAngle(trueAzimuth, smoothedAzimuth)
            onHeadingUpdate(smoothedAzimuth, needsCalibration)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            needsCalibration = (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                    accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        }
    }

    private fun lowPassAngle(current: Float, last: Float): Float {
        var diff = current - last
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        return (last + diff * 0.15f + 360f) % 360f
    }
}
