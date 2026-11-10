package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calculation.HijriCalendarHelper
import com.example.calculation.PrayerTimesCalculator
import com.example.data.model.PrayerName
import com.example.location.LocationHelper
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PrayerAppWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "PrayerAppWidget"
        const val ACTION_REFRESH_WIDGET = "com.example.jadwalsholat.ACTION_REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, PrayerAppWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                if (allWidgetIds != null && allWidgetIds.isNotEmpty()) {
                    for (widgetId in allWidgetIds) {
                        val options = appWidgetManager.getAppWidgetOptions(widgetId)
                        updateAppWidget(context, appWidgetManager, widgetId, options)
                    }
                }
                // Also trigger compact widget update
                PrayerCompactWidgetProvider.updateAllWidgets(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateAllWidgets", e)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            options: Bundle? = null
        ) {
            try {
                val widgetOptions = options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minHeight = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)

                // Responsive layout selection based on user's resized height
                val isCompact = minHeight in 1..79

                val layoutRes = if (isCompact) {
                    R.layout.prayer_widget_compact_layout
                } else {
                    R.layout.prayer_widget_layout
                }

                val views = RemoteViews(context.packageName, layoutRes)

                val location = try {
                    LocationHelper(context).getSavedLocation()
                } catch (_: Exception) {
                    com.example.data.model.UserLocation()
                }

                val today = LocalDate.now()
                val schedule = try {
                    PrayerTimesCalculator.calculate(today, location.latitude, location.longitude)
                } catch (_: Exception) {
                    null
                }

                val hijriDate = try {
                    HijriCalendarHelper.getHijriDate(today)
                } catch (_: Exception) {
                    null
                }

                val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

                val subuhStr = schedule?.subuh?.format(timeFormat) ?: "04:25"
                val dzuhurStr = schedule?.dzuhur?.format(timeFormat) ?: "11:50"
                val asharStr = schedule?.ashar?.format(timeFormat) ?: "15:10"
                val maghribStr = schedule?.maghrib?.format(timeFormat) ?: "18:02"
                val isyaStr = schedule?.isya?.format(timeFormat) ?: "19:15"

                val now = LocalTime.now()
                val next = schedule?.getNextPrayer(now)
                val nextPrayerText = if (next != null) {
                    "${next.prayer.displayName} ${next.time.format(timeFormat)}"
                } else {
                    "Subuh $subuhStr"
                }

                val dateText = if (hijriDate != null) {
                    "${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} H"
                } else {
                    "Jadwal Sholat"
                }

                if (isCompact) {
                    views.setTextViewText(R.id.compact_widget_location, location.cityName)
                    views.setTextViewText(R.id.compact_widget_date, dateText)
                    views.setTextViewText(R.id.compact_widget_next_prayer, nextPrayerText)
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_compact_root, pendingIntent)
                } else {
                    views.setTextViewText(R.id.widget_location, location.cityName)
                    views.setTextViewText(R.id.widget_date, dateText)
                    views.setTextViewText(R.id.widget_next_prayer, nextPrayerText)

                    views.setTextViewText(R.id.widget_time_subuh, subuhStr)
                    views.setTextViewText(R.id.widget_time_dzuhur, dzuhurStr)
                    views.setTextViewText(R.id.widget_time_ashar, asharStr)
                    views.setTextViewText(R.id.widget_time_maghrib, maghribStr)
                    views.setTextViewText(R.id.widget_time_isya, isyaStr)

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widget id $appWidgetId", e)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_REFRESH_WIDGET || action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            updateAllWidgets(context)
        }
    }
}
