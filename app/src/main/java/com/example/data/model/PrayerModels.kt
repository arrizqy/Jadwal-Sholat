package com.example.data.model

import java.time.LocalDate
import java.time.LocalTime

enum class PrayerName(
    val id: String,
    val displayName: String,
    val arabicName: String
) {
    IMSAK("imsak", "Imsak", "الإمساك"),
    SUBUH("subuh", "Subuh", "الفجر"),
    TERBIT("terbit", "Terbit", "الشروق"),
    DHUHA("dhuha", "Dhuha", "الضحى"),
    DZUHUR("dzuhur", "Dzuhur", "الظهر"),
    ASHAR("ashar", "Ashar", "العصر"),
    MAGHRIB("maghrib", "Maghrib", "المغرب"),
    ISYA("isya", "Isya", "العشاء");

    val isFardh: Boolean
        get() = this in listOf(SUBUH, DZUHUR, ASHAR, MAGHRIB, ISYA)
}

data class PrayerTimeItem(
    val prayer: PrayerName,
    val time: LocalTime,
    val isNext: Boolean = false,
    val isPast: Boolean = false
)

data class DayPrayerSchedule(
    val date: LocalDate,
    val imsak: LocalTime,
    val subuh: LocalTime,
    val terbit: LocalTime,
    val dhuha: LocalTime,
    val dzuhur: LocalTime,
    val ashar: LocalTime,
    val maghrib: LocalTime,
    val isya: LocalTime
) {
    fun toList(): List<PrayerTimeItem> {
        val now = LocalTime.now()
        val allPrayers = listOf(
            PrayerTimeItem(PrayerName.IMSAK, imsak),
            PrayerTimeItem(PrayerName.SUBUH, subuh),
            PrayerTimeItem(PrayerName.TERBIT, terbit),
            PrayerTimeItem(PrayerName.DHUHA, dhuha),
            PrayerTimeItem(PrayerName.DZUHUR, dzuhur),
            PrayerTimeItem(PrayerName.ASHAR, ashar),
            PrayerTimeItem(PrayerName.MAGHRIB, maghrib),
            PrayerTimeItem(PrayerName.ISYA, isya)
        )
        val next = getNextPrayer(now)
        return allPrayers.map { item ->
            item.copy(
                isNext = item.prayer == next?.prayer,
                isPast = item.time.isBefore(now)
            )
        }
    }

    fun getNextPrayer(currentTime: LocalTime = LocalTime.now()): PrayerTimeItem? {
        val mainPrayers = listOf(
            PrayerTimeItem(PrayerName.SUBUH, subuh),
            PrayerTimeItem(PrayerName.DZUHUR, dzuhur),
            PrayerTimeItem(PrayerName.ASHAR, ashar),
            PrayerTimeItem(PrayerName.MAGHRIB, maghrib),
            PrayerTimeItem(PrayerName.ISYA, isya)
        )
        return mainPrayers.firstOrNull { it.time.isAfter(currentTime) }
            ?: mainPrayers.first() // After Isya, next is Subuh tomorrow
    }
}

data class NextPrayerCountdown(
    val nextPrayer: PrayerName,
    val time: LocalTime,
    val hoursRemaining: Long,
    val minutesRemaining: Long,
    val secondsRemaining: Long,
    val progress: Float // 0f to 1f between previous and next prayer
)

data class UserLocation(
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val cityName: String = "Jakarta Pusat",
    val regionName: String = "DKI Jakarta, Indonesia",
    val isAutoDetected: Boolean = false
)

enum class CalculationMethod(val displayName: String, val subuhAngle: Double, val isyaAngle: Double) {
    KEMENAG("Kemenag RI (Standar Indonesia)", 20.0, 18.0),
    MWL("Muslim World League", 18.0, 17.0),
    EGYPT("Egyptian General Authority", 19.5, 17.5),
    UMM_AL_QURA("Umm Al-Qura (Makkah)", 18.5, 19.0)
}

enum class AlarmSoundType(val displayName: String) {
    AZAN("Azan Lengkap"),
    BEEP("Nada Pengingat"),
    VIBRATE("Getar Saja"),
    SILENT("Mati (Senyap)")
}

enum class AdzanVoice(
    val id: String,
    val displayName: String,
    val description: String
) {
    MAKKAH("makkah", "Adzan Makkah (Masjidil Haram)", "Suara muadzin asli Masjidil Haram Makkah, Maqam Hijaz syahdu"),
    MADINAH("madinah", "Adzan Madinah (Masjid Nabawi)", "Suara muadzin asli Masjid Nabawi Madinah, Maqam Rast agung"),
    EGYPT("egypt", "Adzan Kairo / Mesir", "Suara muadzin asli Kairo Mesir, Maqam Bayati merdu"),
    QUDS("quds", "Adzan Al-Quds / Syam", "Suara muadzin asli lantunan Maqam Sikah syahdu"),
    SOFT_CHIME("soft_chime", "Nada Harmonis Lembut", "Alunan melodi lembut pengingat waktu sholat"),
    BEEP("beep", "Bip Notifikasi Singkat", "Nada bip ringkas dua ketukan")
}
