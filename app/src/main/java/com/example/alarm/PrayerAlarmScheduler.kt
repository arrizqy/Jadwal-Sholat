package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.example.calculation.PrayerTimesCalculator
import com.example.data.model.AlarmSoundType
import com.example.data.model.PrayerName
import com.example.location.LocationHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PrayerAlarmScheduler(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun isAlarmEnabled(prayer: PrayerName): Boolean {
        return prefs.getBoolean("alarm_${prayer.id}_enabled", true)
    }

    fun setAlarmEnabled(prayer: PrayerName, enabled: Boolean) {
        prefs.edit().putBoolean("alarm_${prayer.id}_enabled", enabled).apply()
        rescheduleAllAlarms()
    }

    fun getAlarmSound(prayer: PrayerName): AlarmSoundType {
        val name = prefs.getString("alarm_${prayer.id}_sound", AlarmSoundType.AZAN.name) ?: AlarmSoundType.AZAN.name
        return try {
            AlarmSoundType.valueOf(name)
        } catch (_: Exception) {
            AlarmSoundType.AZAN
        }
    }

    fun setAlarmSound(prayer: PrayerName, soundType: AlarmSoundType) {
        prefs.edit().putString("alarm_${prayer.id}_sound", soundType.name).apply()
        rescheduleAllAlarms()
    }

    fun rescheduleAllAlarms() {
        val location = LocationHelper(context).getSavedLocation()
        val today = LocalDate.now()
        val scheduleToday = PrayerTimesCalculator.calculate(today, location.latitude, location.longitude)
        val scheduleTomorrow = PrayerTimesCalculator.calculate(today.plusDays(1), location.latitude, location.longitude)

        val prayerTimes = listOf(
            PrayerName.SUBUH to Pair(scheduleToday.subuh, scheduleTomorrow.subuh),
            PrayerName.DZUHUR to Pair(scheduleToday.dzuhur, scheduleTomorrow.dzuhur),
            PrayerName.ASHAR to Pair(scheduleToday.ashar, scheduleTomorrow.ashar),
            PrayerName.MAGHRIB to Pair(scheduleToday.maghrib, scheduleTomorrow.maghrib),
            PrayerName.ISYA to Pair(scheduleToday.isya, scheduleTomorrow.isya)
        )

        val now = LocalTime.now()

        for ((prayer, times) in prayerTimes) {
            val enabled = isAlarmEnabled(prayer)
            val soundType = getAlarmSound(prayer)
            val requestCode = prayer.ordinal

            if (!enabled || soundType == AlarmSoundType.SILENT) {
                cancelAlarm(requestCode)
                continue
            }

            val targetDateTime: LocalDateTime = if (times.first.isAfter(now)) {
                LocalDateTime.of(today, times.first)
            } else {
                LocalDateTime.of(today.plusDays(1), times.second)
            }

            val triggerEpochMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            scheduleAlarm(prayer, times.first.format(DateTimeFormatter.ofPattern("HH:mm")), soundType, triggerEpochMillis, requestCode)
        }
    }

    private fun scheduleAlarm(
        prayer: PrayerName,
        timeFormatted: String,
        soundType: AlarmSoundType,
        triggerAtMillis: Long,
        requestCode: Int
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayer.displayName)
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, timeFormatted)
            putExtra(PrayerAlarmReceiver.EXTRA_SOUND_TYPE, soundType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // If exact alarm permission is restricted on Android 13+, fallback to standard inexact alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
