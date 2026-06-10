package io.qpointz.mill.ai.valuemap

import java.time.Duration

/**
 * Parses facet `data.refreshInterval` strings (ISO-8601 or simple `15m`-style units).
 */
object MillDurations {

    /**
     * @return parsed duration, or `null` if blank / unparseable
     */
    fun parseLenient(text: String): Duration? {
        val t = text.trim()
        if (t.isEmpty()) {
            return null
        }
        return try {
            Duration.parse(t)
        } catch (_: Exception) {
            looseParse(t)
        }
    }

    private fun looseParse(t: String): Duration? {
        val m = Regex("""^(\d+)\s*([a-z]+)$""", RegexOption.IGNORE_CASE).matchEntire(t) ?: return null
        val amount = m.groupValues[1].toLongOrNull() ?: return null
        return when (m.groupValues[2].lowercase()) {
            "ms" -> Duration.ofMillis(amount)
            "s", "sec", "secs", "second", "seconds" -> Duration.ofSeconds(amount)
            "m", "mi", "min", "mins", "minute", "minutes" -> Duration.ofMinutes(amount)
            "h", "hr", "hrs", "hour", "hours" -> Duration.ofHours(amount)
            "d", "day", "days" -> Duration.ofDays(amount)
            else -> null
        }
    }
}
