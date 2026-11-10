package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calculation.HijriCalendarHelper
import com.example.calculation.PrayerTimesCalculator
import com.example.location.LocationHelper
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PrayerCompactWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "PrayerCompactWidget"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, PrayerCompactWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                if (allWidgetIds != null && allWidgetIds.isNotEmpty()) {
                    for (widgetId in allWidgetIds) {
                        updateWidget(context, appWidgetManager, widgetId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateAllWidgets", e)
            }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.prayer_widget_compact_layout)

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
                val now = LocalTime.now()
                val next = schedule?.getNextPrayer(now)
                val nextPrayerText = if (next != null) {
                    "${next.prayer.displayName} ${next.time.format(timeFormat)}"
                } else {
                    "Subuh 04:25"
                }

                views.setTextViewText(R.id.compact_widget_location, location.cityName)
                val dateText = if (hijriDate != null) {
                    "${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} H"
                } else {
                    "Jadwal Sholat"
                }
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

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update compact widget id $appWidgetId", e)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }
}
