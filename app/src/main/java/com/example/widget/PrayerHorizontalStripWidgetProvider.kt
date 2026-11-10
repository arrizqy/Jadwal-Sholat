package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calculation.PrayerTimesCalculator
import com.example.data.model.PrayerName
import com.example.location.LocationHelper
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PrayerHorizontalStripWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "PrayerHorizontalStrip"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, PrayerHorizontalStripWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                if (allWidgetIds != null && allWidgetIds.isNotEmpty()) {
                    for (widgetId in allWidgetIds) {
                        updateWidget(context, appWidgetManager, widgetId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating horizontal strip widgets", e)
            }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.prayer_widget_horizontal_strip_layout)

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

                val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

                val subuhStr = schedule?.subuh?.format(timeFormat) ?: "04:25"
                val dzuhurStr = schedule?.dzuhur?.format(timeFormat) ?: "11:50"
                val asharStr = schedule?.ashar?.format(timeFormat) ?: "15:10"
                val maghribStr = schedule?.maghrib?.format(timeFormat) ?: "18:02"
                val isyaStr = schedule?.isya?.format(timeFormat) ?: "19:15"

                val now = LocalTime.now()
                val next = schedule?.getNextPrayer(now)

                views.setTextViewText(R.id.strip_widget_location, location.cityName)

                val nextInfoText = if (next != null) {
                    "Menuju ${next.prayer.displayName} ${next.time.format(timeFormat)}"
                } else {
                    "Menuju Subuh $subuhStr"
                }
                views.setTextViewText(R.id.strip_widget_next_info, nextInfoText)

                views.setTextViewText(R.id.strip_time_subuh, subuhStr)
                views.setTextViewText(R.id.strip_time_dzuhur, dzuhurStr)
                views.setTextViewText(R.id.strip_time_ashar, asharStr)
                views.setTextViewText(R.id.strip_time_maghrib, maghribStr)
                views.setTextViewText(R.id.strip_time_isya, isyaStr)

                // Highlight next prayer box with pill background
                val nextPrayer = next?.prayer ?: PrayerName.SUBUH

                // Reset all to default background & text colors
                views.setInt(R.id.strip_box_subuh, "setBackgroundResource", 0)
                views.setInt(R.id.strip_box_dzuhur, "setBackgroundResource", 0)
                views.setInt(R.id.strip_box_ashar, "setBackgroundResource", 0)
                views.setInt(R.id.strip_box_maghrib, "setBackgroundResource", 0)
                views.setInt(R.id.strip_box_isya, "setBackgroundResource", 0)

                views.setTextColor(R.id.strip_label_subuh, Color.parseColor("#94A3B8"))
                views.setTextColor(R.id.strip_label_dzuhur, Color.parseColor("#94A3B8"))
                views.setTextColor(R.id.strip_label_ashar, Color.parseColor("#94A3B8"))
                views.setTextColor(R.id.strip_label_maghrib, Color.parseColor("#94A3B8"))
                views.setTextColor(R.id.strip_label_isya, Color.parseColor("#94A3B8"))

                views.setTextColor(R.id.strip_time_subuh, Color.WHITE)
                views.setTextColor(R.id.strip_time_dzuhur, Color.WHITE)
                views.setTextColor(R.id.strip_time_ashar, Color.WHITE)
                views.setTextColor(R.id.strip_time_maghrib, Color.WHITE)
                views.setTextColor(R.id.strip_time_isya, Color.WHITE)

                // Highlight active upcoming prayer
                val (activeBoxId, activeLabelId, activeTimeId) = when (nextPrayer) {
                    PrayerName.SUBUH, PrayerName.IMSAK -> Triple(R.id.strip_box_subuh, R.id.strip_label_subuh, R.id.strip_time_subuh)
                    PrayerName.DZUHUR, PrayerName.TERBIT, PrayerName.DHUHA -> Triple(R.id.strip_box_dzuhur, R.id.strip_label_dzuhur, R.id.strip_time_dzuhur)
                    PrayerName.ASHAR -> Triple(R.id.strip_box_ashar, R.id.strip_label_ashar, R.id.strip_time_ashar)
                    PrayerName.MAGHRIB -> Triple(R.id.strip_box_maghrib, R.id.strip_label_maghrib, R.id.strip_time_maghrib)
                    PrayerName.ISYA -> Triple(R.id.strip_box_isya, R.id.strip_label_isya, R.id.strip_time_isya)
                }

                views.setInt(activeBoxId, "setBackgroundResource", R.drawable.widget_pill_bg)
                views.setTextColor(activeLabelId, Color.parseColor("#FDE047"))
                views.setTextColor(activeTimeId, Color.parseColor("#FDE047"))

                // Click intent
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_strip_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update horizontal strip widget $appWidgetId", e)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }
}
