package com.example.data.quran

import android.content.Context
import org.json.JSONArray
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

object QuranRepository {

    private var appContext: Context? = null
    private val versesCache = ConcurrentHashMap<Int, List<Verse>>()

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    val ALL_SURAHS: List<Surah> = listOf(
        Surah(1, "Al-Fatihah", "الفاتحة", "Pembukaan", "Makkiyah", 7),
        Surah(2, "Al-Baqarah", "البقرة", "Sapi Betina", "Madaniyah", 286),
        Surah(3, "Ali 'Imran", "آل عمران", "Keluarga Imran", "Madaniyah", 200),
        Surah(4, "An-Nisa'", "النساء", "Wanita", "Madaniyah", 176),
        Surah(5, "Al-Ma'idah", "المائدة", "Hidangan", "Madaniyah", 120),
        Surah(6, "Al-An'am", "الأنعام", "Binatang Ternak", "Makkiyah", 165),
        Surah(7, "Al-A'raf", "الأعراف", "Tempat Tertinggi", "Makkiyah", 206),
        Surah(8, "Al-Anfal", "الأنفال", "Rampasan Perang", "Madaniyah", 75),
        Surah(9, "At-Taubah", "التوبة", "Pengampunan", "Madaniyah", 129),
        Surah(10, "Yunus", "يونس", "Nabi Yunus", "Makkiyah", 109),
        Surah(11, "Hud", "هود", "Nabi Hud", "Makkiyah", 123),
        Surah(12, "Yusuf", "يوسف", "Nabi Yusuf", "Makkiyah", 111),
        Surah(13, "Ar-Ra'd", "الرعد", "Guruh", "Madaniyah", 43),
        Surah(14, "Ibrahim", "إبراهيم", "Nabi Ibrahim", "Makkiyah", 52),
        Surah(15, "Al-Hijr", "الحجر", "Gunung Al Hijr", "Makkiyah", 99),
        Surah(16, "An-Nahl", "النحل", "Lebah", "Makkiyah", 128),
        Surah(17, "Al-Isra'", "الإسراء", "Perjalanan Malam", "Makkiyah", 111),
        Surah(18, "Al-Kahf", "الكهف", "Gua", "Makkiyah", 110),
        Surah(19, "Maryam", "مريم", "Siti Maryam", "Makkiyah", 98),
        Surah(20, "Taha", "طه", "Ta Ha", "Makkiyah", 135),
        Surah(21, "Al-Anbiya'", "الأنبياء", "Para Nabi", "Makkiyah", 112),
        Surah(22, "Al-Hajj", "الحج", "Haji", "Madaniyah", 78),
        Surah(23, "Al-Mu'minun", "المؤمنون", "Orang Mukmin", "Makkiyah", 118),
        Surah(24, "An-Nur", "النور", "Cahaya", "Madaniyah", 64),
        Surah(25, "Al-Furqan", "الفرقان", "Pembeda", "Makkiyah", 77),
        Surah(26, "Asy-Syu'ara'", "الشعراء", "Para Penyair", "Makkiyah", 227),
        Surah(27, "An-Naml", "النمل", "Semut", "Makkiyah", 93),
        Surah(28, "Al-Qasas", "القصص", "Kisah-Kisah", "Makkiyah", 88),
        Surah(29, "Al-'Ankabut", "العنكبوت", "Laba-Laba", "Makkiyah", 69),
        Surah(30, "Ar-Rum", "الروم", "Bangsa Romawi", "Makkiyah", 60),
        Surah(31, "Luqman", "لقمان", "Keluarga Luqman", "Makkiyah", 34),
        Surah(32, "As-Sajdah", "السجدة", "Sujud", "Makkiyah", 30),
        Surah(33, "Al-Ahzab", "الأحزاب", "Golongan Bersekutu", "Madaniyah", 73),
        Surah(34, "Saba'", "سبإ", "Kaum Saba'", "Makkiyah", 54),
        Surah(35, "Fatir", "فاطر", "Pencipta", "Makkiyah", 45),
        Surah(36, "Yasin", "يس", "Ya Sin", "Makkiyah", 83),
        Surah(37, "As-Saffat", "الصافات", "Barisan-Barisan", "Makkiyah", 182),
        Surah(38, "Sad", "ص", "Sad", "Makkiyah", 88),
        Surah(39, "Az-Zumar", "الزمر", "Rombongan", "Makkiyah", 75),
        Surah(40, "Ghafir", "غافر", "Maha Pengampun", "Makkiyah", 85),
        Surah(41, "Fussilat", "فصلت", "Yang Dijelaskan", "Makkiyah", 54),
        Surah(42, "Asy-Syura", "الشورى", "Musyawarah", "Makkiyah", 53),
        Surah(43, "Az-Zukhruf", "الزخرف", "Perhiasan", "Makkiyah", 89),
        Surah(44, "Ad-Dukhan", "الدخان", "Kabut", "Makkiyah", 59),
        Surah(45, "Al-Jasiyah", "الجاثية", "Yang Berlutut", "Makkiyah", 37),
        Surah(46, "Al-Ahqaf", "الأحقاف", "Bukit Pasir", "Makkiyah", 35),
        Surah(47, "Muhammad", "محمد", "Nabi Muhammad", "Madaniyah", 38),
        Surah(48, "Al-Fath", "الفتح", "Kemenangan", "Madaniyah", 29),
        Surah(49, "Al-Hujurat", "الحجرات", "Kamar-Kamar", "Madaniyah", 18),
        Surah(50, "Qaf", "ق", "Qaf", "Makkiyah", 45),
        Surah(51, "Az-Zariyat", "الذاريات", "Angin Menerbangkan", "Makkiyah", 60),
        Surah(52, "At-Tur", "الطور", "Bukit Tursina", "Makkiyah", 49),
        Surah(53, "An-Najm", "النجم", "Bintang", "Makkiyah", 62),
        Surah(54, "Al-Qamar", "القمر", "Bulan", "Makkiyah", 55),
        Surah(55, "Ar-Rahman", "الرحمن", "Maha Pemurah", "Madaniyah", 78),
        Surah(56, "Al-Waqi'ah", "الواقعة", "Hari Kiamat", "Makkiyah", 96),
        Surah(57, "Al-Hadid", "الحديد", "Besi", "Madaniyah", 29),
        Surah(58, "Al-Mujadilah", "المجادلة", "Wanita Menggugat", "Madaniyah", 22),
        Surah(59, "Al-Hasyr", "الحشر", "Pengusiran", "Madaniyah", 24),
        Surah(60, "Al-Mumtahanah", "الممتحنة", "Wanita Yang Diuji", "Madaniyah", 13),
        Surah(61, "As-Saff", "الصف", "Barisan", "Madaniyah", 14),
        Surah(62, "Al-Jumu'ah", "الجمعة", "Hari Jum'at", "Madaniyah", 11),
        Surah(63, "Al-Munafiqun", "المنافقون", "Orang Munafik", "Madaniyah", 11),
        Surah(64, "At-Tagabun", "التغابن", "Hari Ditampakkan Kesalahan", "Madaniyah", 18),
        Surah(65, "At-Talaq", "الطلاق", "Talak", "Madaniyah", 12),
        Surah(66, "At-Tahrim", "التحريم", "Mengharamkan", "Madaniyah", 12),
        Surah(67, "Al-Mulk", "الملك", "Kerajaan", "Makkiyah", 30),
        Surah(68, "Al-Qalam", "القلم", "Pena", "Makkiyah", 52),
        Surah(69, "Al-Haqqah", "الحاقة", "Hari Kiamat", "Makkiyah", 52),
        Surah(70, "Al-Ma'arij", "المعارج", "Tempat Naik", "Makkiyah", 44),
        Surah(71, "Nuh", "نوح", "Nabi Nuh", "Makkiyah", 28),
        Surah(72, "Al-Jinn", "الجن", "Jin", "Makkiyah", 28),
        Surah(73, "Al-Muzzammil", "المزمل", "Orang Berselimut", "Makkiyah", 20),
        Surah(74, "Al-Muddassir", "المدثر", "Orang Berkemul", "Makkiyah", 56),
        Surah(75, "Al-Qiyamah", "القيامة", "Hari Berbangkit", "Makkiyah", 40),
        Surah(76, "Al-Insan", "الإنسان", "Manusia", "Madaniyah", 31),
        Surah(77, "Al-Mursalat", "المرسلات", "Malaikat Yang Diutus", "Makkiyah", 50),
        Surah(78, "An-Naba'", "النبإ", "Berita Besar", "Makkiyah", 40),
        Surah(79, "An-Nazi'at", "النازعات", "Malaikat Pencabut", "Makkiyah", 46),
        Surah(80, "'Abasa", "عبس", "Ia Bermuka Masam", "Makkiyah", 42),
        Surah(81, "At-Takwir", "التكوير", "Menggulung", "Makkiyah", 29),
        Surah(82, "Al-Infitar", "الانفطار", "Terbelah", "Makkiyah", 19),
        Surah(83, "Al-Mutaffifin", "المطففين", "Orang Curang", "Makkiyah", 36),
        Surah(84, "Al-Insyiqaq", "الانشقاق", "Terbelah", "Makkiyah", 25),
        Surah(85, "Al-Buruj", "البروج", "Gugusan Bintang", "Makkiyah", 22),
        Surah(86, "At-Tariq", "الطارق", "Yang Datang di Malam Hari", "Makkiyah", 17),
        Surah(87, "Al-A'la", "الأعلى", "Yang Paling Tinggi", "Makkiyah", 19),
        Surah(88, "Al-Ghasyiyah", "الغاشية", "Hari Pembalasan", "Makkiyah", 26),
        Surah(89, "Al-Fajr", "الفجر", "Fajar", "Makkiyah", 30),
        Surah(90, "Al-Balad", "البلد", "Negeri", "Makkiyah", 20),
        Surah(91, "Asy-Syams", "الشمس", "Matahari", "Makkiyah", 15),
        Surah(92, "Al-Lail", "الليل", "Malam", "Makkiyah", 21),
        Surah(93, "Ad-Duha", "الضحى", "Waktu Dhuha", "Makkiyah", 11),
        Surah(94, "Al-Insyirah", "الشرح", "Kelapangan", "Makkiyah", 8),
        Surah(95, "At-Tin", "التين", "Buah Tin", "Makkiyah", 8),
        Surah(96, "Al-'Alaq", "العلق", "Segumpal Darah", "Makkiyah", 19),
        Surah(97, "Al-Qadr", "القدر", "Kemuliaan", "Makkiyah", 5),
        Surah(98, "Al-Bayyinah", "البينة", "Bukti Nyata", "Madaniyah", 8),
        Surah(99, "Az-Zalzalah", "الزلزلة", "Kegoncangan", "Madaniyah", 8),
        Surah(100, "Al-'Adiyat", "العاديات", "Kuda Berlari Kencang", "Makkiyah", 11),
        Surah(101, "Al-Qari'ah", "القارعة", "Hari Kiamat", "Makkiyah", 11),
        Surah(102, "At-Takasur", "التكاثر", "Bermegah-Megahan", "Makkiyah", 8),
        Surah(103, "Al-'Asr", "العصر", "Masa / Waktu", "Makkiyah", 3),
        Surah(104, "Al-Humazah", "الهمزة", "Pengumpat", "Makkiyah", 9),
        Surah(105, "Al-Fil", "الفيل", "Gajah", "Makkiyah", 5),
        Surah(106, "Quraisy", "قريش", "Suku Quraisy", "Makkiyah", 4),
        Surah(107, "Al-Ma'un", "الماعون", "Barang Berguna", "Makkiyah", 7),
        Surah(108, "Al-Kautsar", "الكوثر", "Nikmat Berlimpah", "Makkiyah", 3),
        Surah(109, "Al-Kafirun", "الكافرون", "Orang-Orang Kafir", "Makkiyah", 6),
        Surah(110, "An-Nasr", "النصر", "Pertolongan", "Madaniyah", 3),
        Surah(111, "Al-Lahab", "المسد", "Gejolak Api", "Makkiyah", 5),
        Surah(112, "Al-Ikhlas", "الإخلاص", "Kemurnian Keesaan Allah", "Makkiyah", 4),
        Surah(113, "Al-Falaq", "الفلق", "Waktu Subuh", "Makkiyah", 5),
        Surah(114, "An-Nas", "الناس", "Manusia", "Makkiyah", 6)
    )

    fun getSurah(number: Int): Surah? = ALL_SURAHS.find { it.number == number }

    fun getVersesForSurah(surahNumber: Int, context: Context? = null): List<Verse> {
        versesCache[surahNumber]?.let { return it }

        val ctx = context ?: appContext
        val loaded = loadFromAssets(ctx, surahNumber)
        if (loaded.isNotEmpty()) {
            versesCache[surahNumber] = loaded
            return loaded
        }

        // Emergency fallback if assets could not be opened
        val fallback = getBuiltinFallbackVerses(surahNumber)
        versesCache[surahNumber] = fallback
        return fallback
    }

    private fun loadFromAssets(context: Context?, surahNumber: Int): List<Verse> {
        var inputStream: InputStream? = null
        try {
            if (context != null) {
                inputStream = context.assets.open("quran/$surahNumber.json")
            } else {
                inputStream = QuranRepository::class.java.classLoader?.getResourceAsStream("assets/quran/$surahNumber.json")
                    ?: QuranRepository::class.java.getResourceAsStream("/assets/quran/$surahNumber.json")
            }

            if (inputStream == null) return emptyList()

            val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val array = JSONArray(jsonString)
            val list = ArrayList<Verse>(array.length())

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val v = obj.getInt("v")
                val ar = obj.getString("ar")
                val lt = obj.optString("lt", "")
                val id = obj.optString("id", "")
                val spans = TajwidDetector.detect(ar)

                list.add(
                    Verse(
                        surahNumber = surahNumber,
                        verseNumber = v,
                        arabicText = ar,
                        transliteration = lt,
                        translation = id,
                        tajwidSpans = spans
                    )
                )
            }
            return list
        } catch (_: Exception) {
            return emptyList()
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
        }
    }

    private fun getBuiltinFallbackVerses(surahNumber: Int): List<Verse> {
        return when (surahNumber) {
            1 -> listOf(
                Verse(1, 1, "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ", "Bismillāhir-raḥmānir-raḥīm(i).", "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang.", TajwidDetector.detect("بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ")),
                Verse(1, 2, "الْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَۙ", "Al-ḥamdu lillāhi rabbil-'ālamīn(a).", "Segala puji bagi Allah, Tuhan semesta alam.", TajwidDetector.detect("الْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَۙ")),
                Verse(1, 3, "الرَّحْمٰنِ الرَّحِيْمِۙ", "Ar-raḥmānir-raḥīm(i).", "Yang Maha Pengasih, Maha Penyayang,", TajwidDetector.detect("الرَّحْمٰنِ الرَّحِيْمِۙ")),
                Verse(1, 4, "مٰلِكِ يَوْمِ الدِّيْنِۗ", "Māliki yaumid-dīn(i).", "Pemilik hari pembalasan.", TajwidDetector.detect("مٰلِكِ يَوْمِ الدِّيْنِۗ")),
                Verse(1, 5, "اِيَّاكَ نَعْبُدُ وَاِيَّاكَ نَسْتَعِيْنُۗ", "Iyyāka na'budu wa iyyāka nasta'īn(u).", "Hanya kepada Engkaulah kami menyembah dan hanya kepada Engkaulah kami mohon pertolongan.", TajwidDetector.detect("اِيَّاكَ نَعْبُدُ وَاِيَّاكَ نَسْتَعِيْنُۗ")),
                Verse(1, 6, "اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَۙ", "Ihdinaṣ-ṣirāṭal-mustaqīm(a).", "Tunjukilah kami jalan yang lurus,", TajwidDetector.detect("اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَۙ")),
                Verse(1, 7, "صِرَاطَ الَّذِيْنَ اَنْعَمْتَ عَلَيْهِمْ ەۙ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّاۤلِّيْنَ ࣖ", "Ṣirāṭal-lażīna an'amta 'alaihim gairil-magḍūbi 'alaihim wa laḍ-ḍāllīn(a).", "(yaitu) jalan orang-orang yang telah Engkau beri nikmat kepadanya; bukan (jalan) mereka yang dimurkai, dan bukan (pula jalan) mereka yang sesat.", TajwidDetector.detect("صِرَاطَ الَّذِيْنَ اَنْعَمْتَ عَلَيْهِمْ ەۙ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّاۤلِّيْنَ ࣖ"))
            )
            112 -> listOf(
                Verse(112, 1, "قُلْ هُوَ اللّٰهُ اَحَدٌۚ", "Qul huwallāhu aḥad(un).", "Katakanlah (Muhammad), \"Dialah Allah, Yang Maha Esa.\"", TajwidDetector.detect("قُلْ هُوَ اللّٰهُ اَحَدٌۚ")),
                Verse(112, 2, "اَللّٰهُ الصَّمَدُۚ", "Allāhuṣ-ṣamad(u).", "Allah tempat meminta segala sesuatu.", TajwidDetector.detect("اَللّٰهُ الصَّمَدُۚ")),
                Verse(112, 3, "لَمْ يَلِدْ وَلَمْ يُوْلَدْۙ", "Lam yalid wa lam yūlad.", "(Allah) tidak beranak dan tidak pula diperanakkan,", TajwidDetector.detect("لَمْ يَلِدْ وَلَمْ يُوْلَدْۙ")),
                Verse(112, 4, "وَلَمْ يَكُنْ لَّهٗ كُفُوًا اَحَدٌ ࣖ", "Wa lam yakul lahū kufuwan aḥad(un).", "dan tidak ada sesuatu yang setara dengan Dia.\"", TajwidDetector.detect("وَلَمْ يَكُنْ لَّهٗ كُفُوًا اَحَدٌ ࣖ"))
            )
            else -> {
                val surah = getSurah(surahNumber)
                val count = surah?.totalVerses ?: 7
                (1..count).map { v ->
                    Verse(
                        surahNumber = surahNumber,
                        verseNumber = v,
                        arabicText = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                        transliteration = "Bismillāhir-raḥmānir-raḥīm.",
                        translation = "Terjemahan Surah ${surah?.name ?: ""} Ayat $v.",
                        tajwidSpans = emptyList()
                    )
                }
            }
        }
    }
}
