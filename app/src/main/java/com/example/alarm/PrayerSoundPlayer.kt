package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R
import com.example.data.model.AdzanVoice
import kotlinx.coroutines.*
import kotlin.math.sin

object PrayerSoundPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var playingJob: Job? = null
    private var isPlayingPreview = false

    fun getSelectedAdzanVoice(context: Context): AdzanVoice {
        val prefs = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
        val name = prefs.getString("selected_adzan_voice", AdzanVoice.MAKKAH.name) ?: AdzanVoice.MAKKAH.name
        return try {
            AdzanVoice.valueOf(name)
        } catch (_: Exception) {
            AdzanVoice.MAKKAH
        }
    }

    fun setSelectedAdzanVoice(context: Context, voice: AdzanVoice) {
        val prefs = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_adzan_voice", voice.name).apply()
    }

    /**
     * Plays authentic human voice Adhan recording (copyright-free authentic muazzin vocal recording)
     * for the selected style.
     */
    fun playTestAdhan(
        context: Context,
        voice: AdzanVoice = getSelectedAdzanVoice(context),
        onComplete: () -> Unit = {}
    ) {
        stopPlayback()
        isPlayingPreview = true

        val rawRes = when (voice) {
            AdzanVoice.MAKKAH -> R.raw.adhan_makkah
            AdzanVoice.MADINAH -> R.raw.adhan_madinah
            AdzanVoice.EGYPT -> R.raw.adhan_egypt
            AdzanVoice.QUDS -> R.raw.adhan_makkah
            AdzanVoice.SOFT_CHIME, AdzanVoice.BEEP -> null
        }

        if (rawRes != null) {
            try {
                val player = MediaPlayer.create(context.applicationContext, rawRes)
                if (player != null) {
                    mediaPlayer = player
                    player.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    player.setOnCompletionListener {
                        stopPlayback()
                        onComplete()
                    }
                    player.setOnErrorListener { _, _, _ ->
                        stopPlayback()
                        onComplete()
                        true
                    }
                    player.start()
                    return
                }
            } catch (e: Exception) {
                // Fallback to chime or notification if MediaPlayer fails
            }
        }

        // Chime / beep or fallback
        playToneOrChime(context, voice, onComplete)
    }

    fun playBeep(context: Context) {
        stopPlayback()
        playToneOrChime(context, AdzanVoice.BEEP) {}
    }

    private fun playToneOrChime(
        context: Context,
        voice: AdzanVoice,
        onComplete: () -> Unit
    ) {
        val scope = CoroutineScope(Dispatchers.Default)
        playingJob = scope.launch {
            try {
                if (voice == AdzanVoice.BEEP) {
                    val sampleRate = 22050
                    val minBufferSize = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    val audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBufferSize.coerceAtLeast(sampleRate))
                        .build()

                    audioTrack.play()
                    val freqs = doubleArrayOf(880.0, 0.0, 1174.66)
                    val durs = doubleArrayOf(0.2, 0.08, 0.3)
                    for (i in freqs.indices) {
                        if (!isActive || !isPlayingPreview) break
                        val freq = freqs[i]
                        val durationSec = durs[i]
                        val numSamples = (durationSec * sampleRate).toInt()
                        val buffer = ShortArray(numSamples)
                        if (freq > 0) {
                            for (j in 0 until numSamples) {
                                val t = j.toDouble() / sampleRate
                                val sample = (sin(2.0 * Math.PI * freq * t) * 20000.0).toInt().toShort()
                                buffer[j] = sample
                            }
                        }
                        audioTrack.write(buffer, 0, numSamples)
                    }
                    audioTrack.stop()
                    audioTrack.release()
                } else {
                    // Soft chime or notification ringtone
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, uri)
                    ringtone?.play()
                }
            } catch (_: Exception) {
            } finally {
                isPlayingPreview = false
                launch(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    fun stopPlayback() {
        isPlayingPreview = false
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        playingJob?.cancel()
        playingJob = null
    }

    fun vibrateDevice(context: Context, durationMillis: Long = 800) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMillis)
            }
        } catch (_: Exception) {}
    }
}
