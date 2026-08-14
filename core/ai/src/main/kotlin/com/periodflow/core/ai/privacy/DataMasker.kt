package com.periodflow.core.ai.privacy

/**
 * Redacts identifiable data before it leaves the device.
 *
 * Zero-hallucination privacy protocol:
 * - Absolute dates become relative offsets ("day 3", "yesterday").
 * - Numeric weights/heights are bucketed ("~65kg" → "in your normal weight band").
 * - Free-form notes and questions are scrubbed for phone numbers, emails,
 *   national ids, and multi-digit numbers that could re-identify.
 *
 * The masker is deterministic and side-effect free so it can be unit tested and
 * layered in front of any provider (Gemini today, on-device fast/slow model tomorrow).
 */
object DataMasker {

    private val emailRegex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    // 7+ consecutive digits or grouped digit blocks that look like phone / national id.
    private val longNumberRegex = Regex("""(?:\+?\d[\d\s\-]{6,}\d)""")
    private val isoDateRegex = Regex("""\b\d{4}-\d{2}-\d{2}\b""")
    private val slashDateRegex = Regex("""\b\d{1,2}/\d{1,2}/\d{2,4}\b""")
    private val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)

    /** Redact free-text user input before sending to any LLM. */
    fun redactUserText(text: String): String {
        if (text.isBlank()) return text
        return text
            .replace(emailRegex, "[email]")
            .replace(urlRegex, "[url]")
            .replace(isoDateRegex, "[date]")
            .replace(slashDateRegex, "[date]")
            .replace(longNumberRegex, "[number]")
    }

    /** Bucket a weight into a fuzzy band. Returns null if input is null. */
    fun bucketWeight(kg: Float?): String? = kg?.let {
        when {
            it < 45 -> "low weight band"
            it < 60 -> "lower-normal weight band"
            it < 75 -> "normal weight band"
            it < 90 -> "upper-normal weight band"
            else -> "higher weight band"
        }
    }

    /** Bucket a height into a fuzzy band. Returns null if input is null. */
    fun bucketHeight(cm: Float?): String? = cm?.let {
        when {
            it < 150 -> "shorter stature"
            it < 165 -> "shorter-average stature"
            it < 175 -> "average stature"
            else -> "taller stature"
        }
    }

    /** Convert an absolute cycle-day index into a phase-relative descriptor. */
    fun relativeCycleDay(dayInCycle: Int, cycleLength: Int): String {
        if (cycleLength <= 0) return "unknown day"
        val ratio = dayInCycle.toFloat() / cycleLength
        return when {
            ratio < 0.15f -> "early cycle"
            ratio < 0.4f -> "follicular window"
            ratio < 0.6f -> "around ovulation"
            ratio < 0.85f -> "luteal window"
            else -> "late luteal / pre-menstrual"
        }
    }
}
