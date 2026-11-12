package com.noorazan.app.util

import android.hardware.GeomagneticField
import kotlin.math.*

object PrayerCalc {
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262

    /**
     * Calculates the True North Qibla bearing in degrees (0..360)
     */
    fun calculateTrueQiblaBearing(latitude: Double, longitude: Double): Float {
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val kaabaLngRad = Math.toRadians(KAABA_LNG)
        val userLatRad = Math.toRadians(latitude)
        val userLngRad = Math.toRadians(longitude)

        val dLng = kaabaLngRad - userLngRad
        val y = sin(dLng) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(dLng)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360f) % 360f
        return bearing
    }

    /**
     * Returns magnetic declination in degrees to convert Magnetic North to True North
     */
    fun getMagneticDeclination(latitude: Double, longitude: Double, altitude: Double = 0.0): Float {
        val geoField = GeomagneticField(
            latitude.toFloat(),
            longitude.toFloat(),
            altitude.toFloat(),
            System.currentTimeMillis()
        )
        return geoField.declination
    }
}
