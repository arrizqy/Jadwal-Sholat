package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "prayer_reminder_channel_v2"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_SOUND_TYPE = "extra_sound_type"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Sholat"
        val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
        val soundType = intent.getStringExtra(EXTRA_SOUND_TYPE) ?: "AZAN"

        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Waktu $prayerName Telah Tiba ($prayerTime)")
            .setContentText("Mari tunaikan sholat $prayerName tepat waktu. Hayya 'alas-shalah!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundType != "SILENT") {
            builder.setSound(notificationSoundUri)
            builder.setVibrate(longArrayOf(0, 500, 250, 500))
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = prayerName.hashCode()
        notificationManager.notify(notificationId, builder.build())

        if (soundType == "AZAN") {
            PrayerSoundPlayer.playTestAdhan(context)
        } else if (soundType == "BEEP") {
            PrayerSoundPlayer.playBeep(context)
        } else if (soundType == "VIBRATE") {
            PrayerSoundPlayer.vibrateDevice(context)
        }

        // Reschedule upcoming alarms
        PrayerAlarmScheduler(context).rescheduleAllAlarms()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pengingat Waktu Sholat"
            val descriptionText = "Notifikasi pengingat azan dan waktu sholat 5 waktu"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
