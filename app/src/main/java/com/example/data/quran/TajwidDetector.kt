package com.example.data.quran

object TajwidDetector {

    private val GHUNNAH_REGEX = Regex("[\\u0646\\u0645][\\u0651][\\u064B-\\u0650\\u0670]?")
    private val QALQALAH_REGEX = Regex("[\\u0642\\u0637\\u0628\\u062C\\u062F][\\u0652\\u06E1]")
    private val MAD_REGEX = Regex("[\\u0621-\\u064A][\\u0653\\u06E4]")

    private const val NUN_TANWIN_PREFIX = "([\\u0646][\\u0652\\u06E1]?|[\\u064B\\u064C\\u064D])\\s*"
    private val IQLAB_REGEX = Regex(NUN_TANWIN_PREFIX + "[\\u0628\\u06E2]")
    private val IDGHAM_BILA_REGEX = Regex(NUN_TANWIN_PREFIX + "[\\u0644\\u0631]")
    private val IDGHAM_BIGHA_REGEX = Regex(NUN_TANWIN_PREFIX + "[\\u064A\\u0646\\u0645\\u0648]")
    private val IKHFA_REGEX = Regex(NUN_TANWIN_PREFIX + "[\\u062A\\u062B\\u062C\\u062F\\u0630\\u0632\\u0633\\u0634\\u0635\\u0636\\u0637\\u0638\\u0641\\u0642\\u0643]")

    fun detect(text: String): List<TajwidSpan> {
        if (text.isBlank()) return emptyList()
        val spans = mutableListOf<TajwidSpan>()

        try {
            GHUNNAH_REGEX.findAll(text).forEach { m ->
                spans.add(TajwidSpan(m.range.first, m.range.last + 1, TajwidType.GHUNNAH))
            }
            QALQALAH_REGEX.findAll(text).forEach { m ->
                spans.add(TajwidSpan(m.range.first, m.range.last + 1, TajwidType.QALQALAH))
            }
            MAD_REGEX.findAll(text).forEach { m ->
                spans.add(TajwidSpan(m.range.first, m.range.last + 1, TajwidType.MAD))
            }
            IQLAB_REGEX.findAll(text).forEach { m ->
                spans.add(TajwidSpan(m.range.first, m.range.last + 1, TajwidType.IQLAB))
            }
            IDGHAM_BILA_REGEX.findAll(text).forEach { m ->
                spans.add(TajwidSpan(m.range.first, m.range.last + 1, TajwidType.IDGHAM_BILAGHUNNAH))
            }
            IDGHAM_BIGHA_REGEX.findAll(text).forEach { m ->
                spans.add(TajwidSpan(m.range.first, m.range.last + 1, TajwidType.IDGHAM_BIGHUNNAH))
            }
            IKHFA_REGEX.findAll(text).forEach { m ->
                spans.add(TajwidSpan(m.range.first, m.range.last + 1, TajwidType.IKHFA))
            }
        } catch (_: Exception) {}

        return spans.sortedBy { it.startIndex }
    }
}
