package com.example.data.quran

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class TajwidType(
    val title: String,
    val description: String,
    val example: String,
    val color: Color
) {
    GHUNNAH(
        title = "Ghunnah (Dengung)",
        description = "Mendengungkan suara selama 2 harakat pada huruf Nun (نّ) atau Mim (مّ) yang bertasydid.",
        example = "إِنَّ ، عَمَّ",
        color = TajwidGhunnah
    ),
    IKHFA(
        title = "Ikhfa Haqiqi (Samar)",
        description = "Menyamarkan bunyi Nun Sukun (نْ) atau Tanwin di antara Izhar dan Idgham disertai dengung bila bertemu 15 huruf Ikhfa.",
        example = "مِنْ قَبْلِ ، أَنْزَلَ",
        color = TajwidIkhfa
    ),
    IDGHAM_BIGHUNNAH(
        title = "Idgham Bighunnah (Melebur Dengung)",
        description = "Memasukkan bunyi Nun Sukun / Tanwin ke salah satu huruf (ي ، ن ، م ، و) dengan mendengung.",
        example = "مَنْ يَقُولُ ، مِمَّنْ",
        color = TajwidIdgham
    ),
    IDGHAM_BILAGHUNNAH(
        title = "Idgham Bilaghunnah (Melebur Tanpa Dengung)",
        description = "Memasukkan bunyi Nun Sukun / Tanwin ke huruf Lam (ل) atau Ra (ر) tanpa dengung.",
        example = "مِنْ لَدُنْهُ ، غَفُورٌ رَحِيمٌ",
        color = TajwidIdgham
    ),
    IQLAB(
        title = "Iqlab (Menukar)",
        description = "Mengganti bacaan Nun Sukun / Tanwin menjadi huruf Mim samar dengan dengung saat bertemu Ba (ب).",
        example = "مِنْ بَعْدِ ، سَمِيعٌ بَصِيرٌ",
        color = TajwidIqlab
    ),
    QALQALAH(
        title = "Qalqalah (Memantul)",
        description = "Memantulkan bunyi huruf (ق ، ط ، ب ، ج ، د) ketika berharakat sukun atau berada di akhir waqaf.",
        example = "أَحَدٌ ، الفَلَقِ ، الحَبْلِ",
        color = TajwidQalqalah
    ),
    MAD(
        title = "Mad Wajib / Jaiz (Panjang)",
        description = "Memanjangkan bacaan 4 hingga 6 harakat karena adanya huruf mad bertemu hamzah atau sukun.",
        example = "جَآءَ ، بِمَآ أُنْزِلَ ، السَّمَاءِ",
        color = TajwidMad
    )
}

data class TajwidSpan(
    val startIndex: Int,
    val endIndex: Int,
    val rule: TajwidType
)

data class Verse(
    val surahNumber: Int,
    val verseNumber: Int,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val tajwidSpans: List<TajwidSpan> = emptyList()
)

data class Surah(
    val number: Int,
    val name: String,
    val arabicName: String,
    val translation: String,
    val revelationType: String, // "Makkiyah" or "Madaniyah"
    val totalVerses: Int
)
