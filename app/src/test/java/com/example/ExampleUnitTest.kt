package com.example

import com.example.calculation.HijriCalendarHelper
import com.example.calculation.PrayerTimesCalculator
import com.example.calculation.QiblaCalculator
import com.example.data.quran.QuranRepository
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun testPrayerTimesCalculation() {
        val date = LocalDate.of(2026, 11, 8)
        val jakartaLat = -6.2088
        val jakartaLng = 106.8456

        val schedule = PrayerTimesCalculator.calculate(date, jakartaLat, jakartaLng)

        assertNotNull(schedule)
        // Subuh should be earlier than Terbit
        assertTrue(schedule.subuh.isBefore(schedule.terbit))
        // Terbit earlier than Dzuhur
        assertTrue(schedule.terbit.isBefore(schedule.dzuhur))
        // Dzuhur earlier than Ashar
        assertTrue(schedule.dzuhur.isBefore(schedule.ashar))
        // Ashar earlier than Maghrib
        assertTrue(schedule.ashar.isBefore(schedule.maghrib))
        // Maghrib earlier than Isya
        assertTrue(schedule.maghrib.isBefore(schedule.isya))
    }

    @Test
    fun testQiblaCalculation() {
        val jakartaLat = -6.2088
        val jakartaLng = 106.8456

        val bearing = QiblaCalculator.calculateQiblaBearing(jakartaLat, jakartaLng)
        // From Jakarta, Qibla is roughly 295°
        assertTrue("Bearing should be ~295 degrees (between 290 and 300)", bearing in 290.0..300.0)

        val distance = QiblaCalculator.calculateDistanceToKaaba(jakartaLat, jakartaLng)
        // Jakarta to Mecca is ~7,900 km
        assertTrue("Distance should be ~7,900 km", distance in 7500.0..8500.0)

        // Direction label should be Barat Laut (BL)
        val label = QiblaCalculator.getDirectionLabel(bearing)
        assertEquals("BL", label)
    }

    @Test
    fun testHijriCalendar() {
        val date = LocalDate.of(2026, 11, 8)
        val hijri = HijriCalendarHelper.getHijriDate(date)

        assertNotNull(hijri)
        assertTrue(hijri.day in 1..30)
        assertTrue(hijri.monthIndex in 1..12)
        assertTrue(hijri.year > 1440)
        assertFalse(hijri.monthName.isBlank())
    }

    @Test
    fun testQuranRepository() {
        val allSurahs = QuranRepository.ALL_SURAHS
        assertEquals(114, allSurahs.size)

        val alFatihah = QuranRepository.getSurah(1)
        assertNotNull(alFatihah)
        assertEquals("Al-Fatihah", alFatihah?.name)
        assertEquals(7, alFatihah?.totalVerses)

        val verses = QuranRepository.getVersesForSurah(1)
        assertEquals(7, verses.size)
        // Check Tajwid spans present
        assertTrue(verses.any { it.tajwidSpans.isNotEmpty() })
    }

    @Test
    fun testTajwidDetector() {
        val spans = com.example.data.quran.TajwidDetector.detect("مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ")
        assertTrue(spans.isNotEmpty())
    }
}
