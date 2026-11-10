package com.example.calculation

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.floor

data class HijriDate(
    val day: Int,
    val monthIndex: Int, // 1 to 12
    val monthName: String,
    val year: Int,
    val dayOfWeekName: String
) {
    val formatted: String
        get() = "$day $monthName $year H"
}

object HijriCalendarHelper {

    private val HIJRI_MONTHS = listOf(
        "Muharram",
        "Safar",
        "Rabi\'ul Awwal",
        "Rabi\'ul Akhir",
        "Jumadil Ula",
        "Jumadil Akhir",
        "Rajab",
        "Sya\'ban",
        "Ramadhan",
        "Syawal",
        "Dzulqa\'dah",
        "Dzulhijjah"
    )

    private val INDONESIAN_DAYS = mapOf(
        "MONDAY" to "Senin",
        "TUESDAY" to "Selasa",
        "WEDNESDAY" to "Rabu",
        "THURSDAY" to "Kamis",
        "FRIDAY" to "Jum\'at",
        "SATURDAY" to "Sabtu",
        "SUNDAY" to "Ahad"
    )

    /**
     * Converts a Gregorian date to Hijri date using tabular Kuwaiti/Umm-Al-Qura algorithm.
     */
    fun getHijriDate(date: LocalDate, adjustmentDays: Int = 0): HijriDate {
        val adjustedDate = date.plusDays(adjustmentDays.toLong())
        val day = adjustedDate.dayOfMonth
        val month = adjustedDate.monthValue
        val year = adjustedDate.year

        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        var a = floor(y / 100.0)
        var b = 2 - a + floor(a / 4.0)
        if (y < 1583) b = 0.0
        if (y == 1582) {
            if (m > 10) b = -10.0
            if (m == 10 && day > 4) b = -10.0
        }

        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524

        b = 0.0
        if (jd > 2299160) {
            a = floor((jd - 1867216.25) / 36524.25)
            b = 1 + a - floor(a / 4.0)
        }
        val bb = jd + b + 1524
        var cc = floor((bb - 122.1) / 365.25)
        var dd = floor(365.25 * cc)
        var ee = floor((bb - dd) / 30.6001)

        val dayFrac = bb - dd - floor(30.6001 * ee)
        val monthCalc = if (ee < 14) (ee - 1).toInt() else (ee - 13).toInt()
        val yearCalc = if (monthCalc > 2) (cc - 4716).toInt() else (cc - 4715).toInt()

        val wd = ((jd + 1) % 7).toInt()

        val ijd = jd - 1948440 + 10632
        val n = floor((ijd - 1) / 10631.0)
        val ijd2 = ijd - 10631 * n + 354
        val j = (floor((10985 - ijd2) / 5316.0)) * (floor((50 * ijd2) / 17719.0)) +
                (floor(ijd2 / 5670.0)) * (floor((43 * ijd2) / 15238.0))
        val ijd3 = ijd2 - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29
        val hMonth = (floor((24 * ijd3) / 709.0)).toInt()
        val hDay = (ijd3 - floor((709 * hMonth) / 24.0)).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val clampedMonth = hMonth.coerceIn(1, 12)
        val dayOfWeek = INDONESIAN_DAYS[date.dayOfWeek.name] ?: date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("id", "ID"))

        return HijriDate(
            day = hDay.coerceIn(1, 30),
            monthIndex = clampedMonth,
            monthName = HIJRI_MONTHS[clampedMonth - 1],
            year = hYear,
            dayOfWeekName = dayOfWeek
        )
    }
}
