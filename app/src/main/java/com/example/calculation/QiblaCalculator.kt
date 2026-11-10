package com.example.calculation

import kotlin.math.*

object QiblaCalculator {

    // Holy Kaaba Coordinates (Mecca, Saudi Arabia)
    const val KAABA_LATITUDE = 21.4224779
    const val KAABA_LONGITUDE = 39.826216

    /**
     * Calculates the true forward azimuth (bearing) from user location to the Kaaba in degrees (0..360).
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Double {
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - userLng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)

        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return qibla
    }

    /**
     * Calculates great-circle distance from user coordinates to Kaaba in kilometers.
     */
    fun calculateDistanceToKaaba(userLat: Double, userLng: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(KAABA_LATITUDE - userLat)
        val dLng = Math.toRadians(KAABA_LONGITUDE - userLng)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LATITUDE)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Returns true if user's heading is pointing directly to Qibla within tolerance (e.g. 3.5 degrees).
     */
    fun isFacingQibla(currentHeading: Float, qiblaBearing: Double, toleranceDegrees: Double = 3.5): Boolean {
        var diff = abs(currentHeading - qiblaBearing)
        if (diff > 180) diff = 360 - diff
        return diff <= toleranceDegrees
    }

    fun getDirectionLabel(bearing: Double): String {
        val directions = arrayOf("U", "TL", "T", "TG", "S", "BD", "B", "BL")
        val index = ((bearing + 22.5) / 45.0).toInt() % 8
        return directions[index]
    }
}
