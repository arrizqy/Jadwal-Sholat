package com.example.data.doa

data class DoaItem(
    val id: String,
    val title: String,
    val category: String, // "Dzikir Sholat", "Pagi & Petang", "Doa Harian"
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val sourceOrCount: String = "1x"
)

object DoaRepository {

    val ALL_DOA: List<DoaItem> = listOf(
        DoaItem(
            id = "dzikir_istighfar",
            title = "Istighfar Setelah Sholat",
            category = "Dzikir Sholat",
            arabic = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullāhal-'aẓīm, allażī lā ilāha illā huwal-ḥayyul-qayyūmu wa atūbu ilaih.",
            translation = "Aku memohon ampunan kepada Allah Yang Maha Agung, tidak ada Tuhan selain Dia, Yang Maha Hidup lagi terus menerus mengurus makhluk-Nya, dan aku bertobat kepada-Nya.",
            sourceOrCount = "3x"
        ),
        DoaItem(
            id = "dzikir_tahlil",
            title = "Tahlil & Doa Keselamatan",
            category = "Dzikir Sholat",
            arabic = "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transliteration = "Allāhumma antas-salām, wa minkas-salām, tabārakta yā żal-jalāli wal-ikrām.",
            translation = "Ya Allah, Engkaulah As-Salam (Yang Maha Menyelamatkan), dari-Mu keselamatan, Mahasuci Engkau wahai Tuhan Pemilik Keagungan dan Kemuliaan.",
            sourceOrCount = "HR. Muslim"
        ),
        DoaItem(
            id = "tasbih_sholat",
            title = "Tasbih, Tahmid, Takbir",
            category = "Dzikir Sholat",
            arabic = "سُبْحَانَ اللَّهِ (٣٣) ، الْحَمْدُ لِلَّهِ (٣٣) ، اللَّهُ أَكْبَرُ (٣٣)",
            transliteration = "Subḥānallāh (33x), Al-ḥamdulillāh (33x), Allāhu akbar (33x).",
            translation = "Mahasuci Allah (33x), Segala puji bagi Allah (33x), Allah Mahabesar (33x).",
            sourceOrCount = "33x Masing-masing"
        ),
        DoaItem(
            id = "ayat_kursi",
            title = "Ayat Al-Kursi",
            category = "Dzikir Sholat",
            arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allāhu lā ilāha illā huwal-ḥayyul-qayyūm, lā ta'khużuhū sinatuw wa lā na'ūm, lahū mā fis-samāwāti wa mā fil-arḍ, man żal-lażī yasyfa'u 'indahū illā bi'iżnih, ya'lamu mā baina aidīhim wa mā khalfahum, wa lā yuḥīṭūna bisyai'im min 'ilmihī illā bimā syā', wasi'a kursiyyuhus-samāwāti wal-arḍ, wa lā ya'ūduhū ḥifẓuhumā, wa huwal-'aliyyul-'aẓīm.",
            translation = "Allah, tidak ada tuhan selain Dia. Yang Mahahidup, Yang terus-menerus mengurus (makhluk-Nya), tidak mengantuk dan tidak tidur. Milik-Nya apa yang ada di langit dan apa yang ada di bumi...",
            sourceOrCount = "QS. Al-Baqarah: 255"
        ),
        DoaItem(
            id = "dzikir_pagi_sayyidul_istighfar",
            title = "Sayyidul Istighfar (Penghulu Istighfar)",
            category = "Pagi & Petang",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allāhumma anta rabbī lā ilāha illā anta khalaqtanī wa anā 'abduka wa anā 'alā 'ahdika wa wa'dika mastaṭa'tu, a'ūżu bika min syarri mā ṣana'tu, abū'u laka bini'matika 'alayya wa abū'u biżambī fagfir lī fa'innahū lā yagfiruż-żunūba illā anta.",
            translation = "Ya Allah, Engkau adalah Tuhanku, tidak ada tuhan selain Engkau. Engkau yang menciptakan aku dan aku adalah hamba-Mu...",
            sourceOrCount = "Dibaca 1x (Pagi & Sore)"
        ),
        DoaItem(
            id = "dzikir_bismillahil_ladzi",
            title = "Doa Perlindungan Pagi & Sore",
            category = "Pagi & Petang",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillāhil-lażī lā yaḍurru ma'asmihī syai'un fil-arḍi wa lā fis-samā'i wa huwas-samī'ul-'alīm.",
            translation = "Dengan nama Allah yang bila bersama nama-Nya tidak ada sesuatupun yang dapat mendatangkan bahaya di bumi maupun di langit, dan Dia Maha Mendengar lagi Maha Mengetahui.",
            sourceOrCount = "3x Pagi & Petang"
        ),
        DoaItem(
            id = "doa_kedua_orang_tua",
            title = "Doa Untuk Kedua Orang Tua",
            category = "Doa Harian",
            arabic = "رَبِّ اغْفِرْ لِي وَلِوَالِدَيَّ وَارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliteration = "Rabbigfir lī wa liwālidayya war-ḥamhumā kamā rabbayānī ṣagīrā.",
            translation = "Wahai Tuhanku, ampunilah aku dan kedua orang tuaku, dan sayangilah keduanya sebagaimana mereka telah mendidik aku di waktu kecil.",
            sourceOrCount = "QS. Al-Isra': 24"
        ),
        DoaItem(
            id = "doa_sapujagat",
            title = "Doa Kebaikan Dunia & Akhirat",
            category = "Doa Harian",
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliteration = "Rabbanā ātinā fid-dunyā ḥasanataw wa fil-ākhirati ḥasanataw wa qinā 'ażāban-nār.",
            translation = "Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat, dan lindungilah kami dari siksa neraka.",
            sourceOrCount = "QS. Al-Baqarah: 201"
        ),
        DoaItem(
            id = "doa_masuk_masjid",
            title = "Doa Masuk Masjid",
            category = "Doa Harian",
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allāhummaftaḥ lī abwāba raḥmatik.",
            translation = "Ya Allah, bukakanlah untukku pintu-pintu rahmat-Mu.",
            sourceOrCount = "HR. Muslim"
        ),
        DoaItem(
            id = "doa_keluar_masjid",
            title = "Doa Keluar Masjid",
            category = "Doa Harian",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Allāhumma innī as'aluka min faḍlik.",
            translation = "Ya Allah, sesungguhnya aku memohon karunia dari-Mu.",
            sourceOrCount = "HR. Muslim"
        )
    )
}
