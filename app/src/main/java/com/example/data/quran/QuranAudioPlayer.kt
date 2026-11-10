package com.example.data.quran

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.sin

class QuranAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var synthesisJob: Job? = null

    private val _currentVerse = MutableStateFlow<Int?>(null)
    val currentVerse: StateFlow<Int?> = _currentVerse.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSurah = MutableStateFlow<Int?>(null)
    val currentSurah: StateFlow<Int?> = _currentSurah.asStateFlow()

    private var onVerseCompletedCallback: (() -> Unit)? = null

    fun playVerse(
        surahNumber: Int,
        verseNumber: Int,
        onComplete: (() -> Unit)? = null
    ) {
        stop()

        _currentSurah.value = surahNumber
        _currentVerse.value = verseNumber
        _isPlaying.value = true
        onVerseCompletedCallback = onComplete

        val primaryUrl = String.format(
            Locale.US,
            "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3",
            surahNumber,
            verseNumber
        )
        val secondaryUrl = String.format(
            Locale.US,
            "https://cdn.equran.id/audio-partial/Misyari-Rasyid-Al-Afasi/%03d%03d.mp3",
            surahNumber,
            verseNumber
        )

        playStreamWithFallback(primaryUrl, secondaryUrl, surahNumber, verseNumber)
    }

    private fun playStreamWithFallback(
        url: String,
        fallbackUrl: String?,
        surahNumber: Int,
        verseNumber: Int
    ) {
        try {
            stopMediaPlayerOnly()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    try {
                        mp.start()
                    } catch (e: Exception) {
                        if (fallbackUrl != null) {
                            playStreamWithFallback(fallbackUrl, null, surahNumber, verseNumber)
                        } else {
                            fallbackSynthesizedRecitation(surahNumber, verseNumber)
                        }
                    }
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentVerse.value = null
                    onVerseCompletedCallback?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("QuranAudioPlayer", "Stream error on $url ($what, $extra).")
                    if (fallbackUrl != null) {
                        playStreamWithFallback(fallbackUrl, null, surahNumber, verseNumber)
                    } else {
                        fallbackSynthesizedRecitation(surahNumber, verseNumber)
                    }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Failed streaming $url", e)
            if (fallbackUrl != null) {
                playStreamWithFallback(fallbackUrl, null, surahNumber, verseNumber)
            } else {
                fallbackSynthesizedRecitation(surahNumber, verseNumber)
            }
        }
    }

    private fun fallbackSynthesizedRecitation(surahNumber: Int, verseNumber: Int) {
        stopMediaPlayerOnly()
        synthesisJob?.cancel()

        synthesisJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                // Harmonic chant cantillation sequence
                val sampleRate = 22050
                val baseFreq = 220.0 + (surahNumber % 5) * 15.0
                val chantNotes = doubleArrayOf(
                    baseFreq,
                    baseFreq * 1.125,
                    baseFreq * 1.25,
                    baseFreq * 1.333,
                    baseFreq * 1.25,
                    baseFreq
                )
                val noteDurations = doubleArrayOf(0.7, 0.7, 1.2, 0.8, 0.9, 1.4)

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize.coerceAtLeast(sampleRate * 2))
                    .build()

                track.play()

                for (i in chantNotes.indices) {
                    if (!isActive || !_isPlaying.value) break
                    val freq = chantNotes[i]
                    val durationSec = noteDurations[i]
                    val numSamples = (durationSec * sampleRate).toInt()
                    val buffer = ShortArray(numSamples)

                    for (j in 0 until numSamples) {
                        val t = j.toDouble() / sampleRate
                        // Soft envelope with harmonics
                        val env = if (j < numSamples * 0.15) {
                            j / (numSamples * 0.15)
                        } else if (j > numSamples * 0.65) {
                            (numSamples - j) / (numSamples * 0.35)
                        } else 1.0

                        val fundamental = sin(2.0 * Math.PI * freq * t)
                        val overtone = 0.3 * sin(4.0 * Math.PI * freq * t)
                        val sample = ((fundamental + overtone) * 22000.0 * env).toInt().toShort()
                        buffer[j] = sample
                    }
                    track.write(buffer, 0, numSamples)
                    delay((durationSec * 950).toLong())
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.e("QuranAudioPlayer", "Synthesized recitation error", e)
            } finally {
                withContext(Dispatchers.Main) {
                    _isPlaying.value = false
                    _currentVerse.value = null
                    onVerseCompletedCallback?.invoke()
                }
            }
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            }
        } catch (_: Exception) {}
        synthesisJob?.cancel()
    }

    fun stop() {
        stopMediaPlayerOnly()
        synthesisJob?.cancel()
        _isPlaying.value = false
        _currentVerse.value = null
    }

    private fun stopMediaPlayerOnly() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
