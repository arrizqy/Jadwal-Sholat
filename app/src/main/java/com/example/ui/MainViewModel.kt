package com.example.ui

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.PrayerAlarmScheduler
import com.example.alarm.PrayerSoundPlayer
import com.example.calculation.HijriCalendarHelper
import com.example.calculation.HijriDate
import com.example.calculation.PrayerTimesCalculator
import com.example.calculation.QiblaCalculator
import com.example.data.db.AppDatabase
import com.example.data.db.BookmarkEntity
import com.example.data.db.LastReadEntity
import com.example.data.model.*
import com.example.data.quran.QuranAudioPlayer
import com.example.data.quran.QuranRepository
import com.example.data.quran.Surah
import com.example.data.quran.Verse
import com.example.location.CityPreset
import com.example.location.LocationHelper
import com.example.sensor.CompassSensorManager
import com.example.widget.PrayerAppWidgetProvider
import com.example.widget.PrayerCompactWidgetProvider
import com.example.widget.PrayerHorizontalStripWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class AppNavTab(val title: String) {
    JADWAL("Jadwal"),
    KIBLAT("Kiblat"),
    QURAN("Al-Qur'an"),
    DOA("Doa & Dzikir"),
    SETTINGS("Pengaturan")
}

data class MainUiState(
    val currentTab: AppNavTab = AppNavTab.JADWAL,
    val userLocation: UserLocation = UserLocation(),
    val isDetectingLocation: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val hijriDate: HijriDate = HijriCalendarHelper.getHijriDate(LocalDate.now()),
    val todaySchedule: DayPrayerSchedule? = null,
    val countdown: NextPrayerCountdown? = null,
    val qiblaBearing: Double = 0.0,
    val distanceToKaabaKm: Double = 0.0,
    val compassAzimuth: Float = 0f,
    val isFacingQibla: Boolean = false,
    val isSensorAvailable: Boolean = true,
    val isPlayingAdhanPreview: Boolean = false,
    val selectedAdzanVoice: AdzanVoice = AdzanVoice.MAKKAH,
    // Quran state
    val selectedSurah: Surah? = null,
    val currentVerses: List<Verse> = emptyList(),
    val isTajwidEnabled: Boolean = true,
    val quranFontSizeSp: Float = 24f,
    val quranSearchQuery: String = "",
    val playingVerseNumber: Int? = null,
    val isQuranPlaying: Boolean = false,
    // Tasbih state
    val tasbihCount: Int = 0,
    val tasbihTarget: Int = 33,
    // Settings
    val calculationMethod: CalculationMethod = CalculationMethod.KEMENAG,
    val ihtiyatMinutes: Int = 2
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val locationHelper = LocationHelper(context)
    private val compassSensorManager = CompassSensorManager(context)
    private val alarmScheduler = PrayerAlarmScheduler(context)
    private val database = AppDatabase.getDatabase(context)
    private val bookmarkDao = database.bookmarkDao()
    private val lastReadDao = database.lastReadDao()
    private val quranAudioPlayer = QuranAudioPlayer(context)

    private val _uiState = MutableStateFlow(
        MainUiState(
            selectedAdzanVoice = PrayerSoundPlayer.getSelectedAdzanVoice(context)
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val allBookmarks: StateFlow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastRead: StateFlow<LastReadEntity?> = lastReadDao.getLastRead()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var countdownJob: Job? = null
    private var compassJob: Job? = null

    init {
        QuranRepository.init(context)
        // Load initial location
        val savedLoc = locationHelper.getSavedLocation()
        _uiState.update { it.copy(userLocation = savedLoc) }
        recalculateSchedule()
        startCountdownTicker()
        observeCompass()
        observeQuranAudio()
        alarmScheduler.rescheduleAllAlarms()
        updateWidgets()
    }

    private fun updateWidgets() {
        PrayerAppWidgetProvider.updateAllWidgets(context)
        PrayerCompactWidgetProvider.updateAllWidgets(context)
        PrayerHorizontalStripWidgetProvider.updateAllWidgets(context)
    }

    private fun observeQuranAudio() {
        viewModelScope.launch {
            quranAudioPlayer.currentVerse.collect { verseNum ->
                _uiState.update { it.copy(playingVerseNumber = verseNum) }
            }
        }
        viewModelScope.launch {
            quranAudioPlayer.isPlaying.collect { isPlaying ->
                _uiState.update { it.copy(isQuranPlaying = isPlaying) }
            }
        }
    }

    fun selectTab(tab: AppNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun recalculateSchedule(date: LocalDate = _uiState.value.selectedDate) {
        val loc = _uiState.value.userLocation
        val method = _uiState.value.calculationMethod
        val ihtiyat = _uiState.value.ihtiyatMinutes

        val schedule = PrayerTimesCalculator.calculate(
            date = date,
            latitude = loc.latitude,
            longitude = loc.longitude,
            method = method,
            ihtiyatMinutes = ihtiyat
        )
        val hijri = HijriCalendarHelper.getHijriDate(date)
        val bearing = QiblaCalculator.calculateQiblaBearing(loc.latitude, loc.longitude)
        val distance = QiblaCalculator.calculateDistanceToKaaba(loc.latitude, loc.longitude)

        _uiState.update {
            it.copy(
                selectedDate = date,
                todaySchedule = schedule,
                hijriDate = hijri,
                qiblaBearing = bearing,
                distanceToKaabaKm = distance
            )
        }
        updateCountdown()
        updateWidgets()
    }

    fun requestAutoLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingLocation = true) }
            val detected = locationHelper.fetchCurrentLocation()
            if (detected != null) {
                _uiState.update { it.copy(userLocation = detected, isDetectingLocation = false) }
                recalculateSchedule()
                alarmScheduler.rescheduleAllAlarms()
                updateWidgets()
            } else {
                _uiState.update { it.copy(isDetectingLocation = false) }
            }
        }
    }

    fun selectCity(preset: CityPreset) {
        val userLoc = UserLocation(
            latitude = preset.latitude,
            longitude = preset.longitude,
            cityName = preset.name,
            regionName = "${preset.province}, Indonesia",
            isAutoDetected = false
        )
        locationHelper.saveLocation(userLoc)
        _uiState.update { it.copy(userLocation = userLoc) }
        recalculateSchedule()
        alarmScheduler.rescheduleAllAlarms()
        updateWidgets()
    }

    private fun startCountdownTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                updateCountdown()
                delay(1000)
            }
        }
    }

    private fun updateCountdown() {
        val schedule = _uiState.value.todaySchedule ?: return
        val now = LocalTime.now()
        val next = schedule.getNextPrayer(now) ?: return

        val secondsUntil = if (next.time.isAfter(now)) {
            ChronoUnit.SECONDS.between(now, next.time)
        } else {
            val untilMidnight = ChronoUnit.SECONDS.between(now, LocalTime.MAX)
            val fromMidnight = ChronoUnit.SECONDS.between(LocalTime.MIN, next.time)
            untilMidnight + fromMidnight
        }

        val hours = secondsUntil / 3600
        val minutes = (secondsUntil % 3600) / 60
        val seconds = secondsUntil % 60

        val windowSec = 14400f
        val progress = 1f - (secondsUntil.toFloat() / windowSec).coerceIn(0f, 1f)

        _uiState.update {
            it.copy(
                countdown = NextPrayerCountdown(
                    nextPrayer = next.prayer,
                    time = next.time,
                    hoursRemaining = hours,
                    minutesRemaining = minutes,
                    secondsRemaining = seconds,
                    progress = progress
                )
            )
        }
    }

    private fun observeCompass() {
        compassJob?.cancel()
        compassJob = viewModelScope.launch {
            compassSensorManager.getCompassFlow().collect { state ->
                val qibla = _uiState.value.qiblaBearing
                val isFacing = QiblaCalculator.isFacingQibla(state.azimuth, qibla)

                if (isFacing) {
                    compassSensorManager.vibrateOnceOnQiblaFound()
                } else {
                    compassSensorManager.resetQiblaVibrationLock()
                }

                _uiState.update {
                    it.copy(
                        compassAzimuth = state.azimuth,
                        isFacingQibla = isFacing,
                        isSensorAvailable = state.isSensorAvailable
                    )
                }
            }
        }
    }

    // Alarm controls
    fun isPrayerAlarmEnabled(prayer: PrayerName): Boolean = alarmScheduler.isAlarmEnabled(prayer)

    fun togglePrayerAlarm(prayer: PrayerName) {
        val current = alarmScheduler.isAlarmEnabled(prayer)
        alarmScheduler.setAlarmEnabled(prayer, !current)
        _uiState.update { it.copy() }
    }

    fun getPrayerAlarmSound(prayer: PrayerName): AlarmSoundType = alarmScheduler.getAlarmSound(prayer)

    fun setPrayerAlarmSound(prayer: PrayerName, sound: AlarmSoundType) {
        alarmScheduler.setAlarmSound(prayer, sound)
        _uiState.update { it.copy() }
    }

    fun selectAdzanVoice(voice: AdzanVoice) {
        PrayerSoundPlayer.setSelectedAdzanVoice(context, voice)
        _uiState.update { it.copy(selectedAdzanVoice = voice) }
    }

    fun toggleAdhanPreview(voice: AdzanVoice? = null) {
        val targetVoice = voice ?: _uiState.value.selectedAdzanVoice
        if (_uiState.value.isPlayingAdhanPreview && _uiState.value.selectedAdzanVoice == targetVoice) {
            PrayerSoundPlayer.stopPlayback()
            _uiState.update { it.copy(isPlayingAdhanPreview = false) }
        } else {
            PrayerSoundPlayer.stopPlayback()
            PrayerSoundPlayer.setSelectedAdzanVoice(context, targetVoice)
            _uiState.update { it.copy(selectedAdzanVoice = targetVoice, isPlayingAdhanPreview = true) }
            PrayerSoundPlayer.playTestAdhan(context, targetVoice) {
                _uiState.update { it.copy(isPlayingAdhanPreview = false) }
            }
        }
    }

    // Quran actions
    fun openSurah(surah: Surah) {
        viewModelScope.launch(Dispatchers.IO) {
            val verses = QuranRepository.getVersesForSurah(surah.number, context)
            _uiState.update { it.copy(selectedSurah = surah, currentVerses = verses) }
            saveLastRead(surah.number, surah.name, 1)
        }
    }

    fun closeSurahDetail() {
        quranAudioPlayer.stop()
        _uiState.update {
            it.copy(
                selectedSurah = null,
                currentVerses = emptyList(),
                playingVerseNumber = null,
                isQuranPlaying = false
            )
        }
    }

    fun playVerseAudio(verseNumber: Int) {
        val surah = _uiState.value.selectedSurah ?: return
        val verses = _uiState.value.currentVerses

        if (_uiState.value.playingVerseNumber == verseNumber && _uiState.value.isQuranPlaying) {
            quranAudioPlayer.pause()
        } else {
            quranAudioPlayer.playVerse(surah.number, verseNumber) {
                // Auto advance to next verse in the surah
                val nextVerse = verseNumber + 1
                if (verses.any { it.verseNumber == nextVerse }) {
                    playVerseAudio(nextVerse)
                }
            }
        }
    }

    fun pauseVerseAudio() {
        quranAudioPlayer.pause()
    }

    fun stopVerseAudio() {
        quranAudioPlayer.stop()
    }

    fun setQuranSearch(query: String) {
        _uiState.update { it.copy(quranSearchQuery = query) }
    }

    fun toggleTajwid() {
        _uiState.update { it.copy(isTajwidEnabled = !it.isTajwidEnabled) }
    }

    fun setQuranFontSize(sizeSp: Float) {
        _uiState.update { it.copy(quranFontSizeSp = sizeSp.coerceIn(18f, 36f)) }
    }

    fun toggleBookmark(verse: Verse, surahName: String) {
        viewModelScope.launch {
            val isBookmarked = allBookmarks.value.any { it.surahNumber == verse.surahNumber && it.verseNumber == verse.verseNumber }
            if (isBookmarked) {
                bookmarkDao.deleteBookmark(verse.surahNumber, verse.verseNumber)
            } else {
                bookmarkDao.insertBookmark(
                    BookmarkEntity(
                        surahNumber = verse.surahNumber,
                        surahName = surahName,
                        verseNumber = verse.verseNumber,
                        arabicText = verse.arabicText,
                        translationText = verse.translation
                    )
                )
            }
        }
    }

    fun saveLastRead(surahNumber: Int, surahName: String, verseNumber: Int) {
        viewModelScope.launch {
            lastReadDao.setLastRead(
                LastReadEntity(
                    id = 1,
                    surahNumber = surahNumber,
                    surahName = surahName,
                    verseNumber = verseNumber
                )
            )
        }
    }

    // Tasbih actions
    fun incrementTasbih() {
        val current = _uiState.value.tasbihCount + 1
        val target = _uiState.value.tasbihTarget
        _uiState.update { it.copy(tasbihCount = current) }
        vibrateLight()
        if (target > 0 && current % target == 0) {
            vibrateTargetReached()
        }
    }

    fun resetTasbih() {
        _uiState.update { it.copy(tasbihCount = 0) }
        vibrateLight()
    }

    fun setTasbihTarget(target: Int) {
        _uiState.update { it.copy(tasbihTarget = target) }
    }

    private fun vibrateLight() {
        try {
            val vibrator = getVibrator()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateTargetReached() {
        try {
            val vibrator = getVibrator()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(250)
            }
        } catch (_: Exception) {}
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Application.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Application.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Settings actions
    fun setCalculationMethod(method: CalculationMethod) {
        _uiState.update { it.copy(calculationMethod = method) }
        recalculateSchedule()
        alarmScheduler.rescheduleAllAlarms()
    }

    fun setIhtiyatMinutes(minutes: Int) {
        _uiState.update { it.copy(ihtiyatMinutes = minutes.coerceIn(0, 10)) }
        recalculateSchedule()
        alarmScheduler.rescheduleAllAlarms()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        compassJob?.cancel()
        PrayerSoundPlayer.stopPlayback()
        quranAudioPlayer.release()
    }
}
