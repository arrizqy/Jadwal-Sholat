package com.example.calculation

import com.example.data.model.CalculationMethod
import com.example.data.model.DayPrayerSchedule
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.*

object PrayerTimesCalculator {

    /**
     * Calculates astronomical prayer times for a given date, geographic location,
     * and calculation method (defaults to Kemenag RI with standard +2 mins ihtiyat).
     */
    fun calculate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timezoneOffsetHours: Double = getEstimatedTimezoneOffset(longitude),
        method: CalculationMethod = CalculationMethod.KEMENAG,
        ihtiyatMinutes: Int = 2
    ): DayPrayerSchedule {
        val julianDate = toJulianDate(date.year, date.monthValue, date.dayOfMonth)
        val d = julianDate - 2451545.0

        // Mean solar anomaly and longitude
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        // Earth's obliquity
        val e = 23.439 - 0.00000036 * d
        val sinDec = sin(Math.toRadians(e)) * sin(Math.toRadians(l))
        val dec = Math.toDegrees(asin(sinDec)) // Solar declination

        // Right ascension & Equation of time
        val ra = fixAngle(Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))) / 15.0
        val eqT = (q / 15.0) - ra

        // Transit time (Solar noon / Dzuhur)
        val noon = 12.0 + timezoneOffsetHours - (longitude / 15.0) - eqT

        // Sun elevation calculations
        // Sunrise/Sunset (standard atmospheric refraction 0.833)
        val sunriseHourAngle = calculateHourAngle(-0.8333, latitude, dec)

        // Subuh (Fajr) based on method angle
        val subuhHourAngle = calculateHourAngle(-method.subuhAngle, latitude, dec)

        // Isya based on method angle
        val isyaHourAngle = calculateHourAngle(-method.isyaAngle, latitude, dec)

        // Ashar (Shafi'i shadow factor 1: angle = arccot(1 + tan|lat - dec|))
        val asharAngleRad = atan(1.0 / (1.0 + tan(Math.toRadians(abs(latitude - dec)))))
        val asharHourAngle = calculateHourAngle(Math.toDegrees(asharAngleRad), latitude, dec)

        // Convert hours to LocalTime with ihtiyat safety adjustment
        val dzuhurTime = toLocalTime(noon, ihtiyatMinutes)
        val terbitTime = toLocalTime(noon - sunriseHourAngle, 0)
        val maghribTime = toLocalTime(noon + sunriseHourAngle, ihtiyatMinutes)
        val subuhTime = toLocalTime(noon - subuhHourAngle, ihtiyatMinutes)
        val isyaTime = toLocalTime(noon + isyaHourAngle, ihtiyatMinutes)
        val asharTime = toLocalTime(noon + asharHourAngle, ihtiyatMinutes)

        // Imsak is 10 minutes before Subuh (Indonesian tradition)
        val imsakTime = subuhTime.minusMinutes(10)

        // Dhuha is 20 minutes after Terbit
        val dhuhaTime = terbitTime.plusMinutes(20)

        return DayPrayerSchedule(
            date = date,
            imsak = imsakTime,
            subuh = subuhTime,
            terbit = terbitTime,
            dhuha = dhuhaTime,
            dzuhur = dzuhurTime,
            ashar = asharTime,
            maghrib = maghribTime,
            isya = isyaTime
        )
    }

    private fun calculateHourAngle(altitude: Double, latitude: Double, declination: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val altRad = Math.toRadians(altitude)

        val cosH = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH)) / 15.0
    }

    private fun toLocalTime(fractionalHours: Double, addMinutes: Int): LocalTime {
        var totalMinutes = (fractionalHours * 60.0).roundToInt() + addMinutes
        while (totalMinutes < 0) totalMinutes += 1440
        while (totalMinutes >= 1440) totalMinutes -= 1440

        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return LocalTime.of(hours, minutes)
    }

    private fun toJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    fun getEstimatedTimezoneOffset(longitude: Double): Double {
        // Indonesia timezone estimation
        return when {
            longitude < 110.0 -> 7.0   // WIB (UTC+7)
            longitude < 125.0 -> 8.0   // WITA (UTC+8)
            longitude <= 141.0 -> 9.0  // WIT (UTC+9)
            else -> (longitude / 15.0).roundToInt().toDouble()
        }
    }
}
