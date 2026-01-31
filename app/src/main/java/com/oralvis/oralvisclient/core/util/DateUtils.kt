package com.oralvis.oralvisclient.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Central date formatting for API: backend expects YYYY-MM-DD for query/body dates.
 */
object DateUtils {

    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /** Format for API (query params, body). */
    fun toApiDate(date: Date): String = apiDateFormatter.format(date)

    /** Parse API date string (YYYY-MM-DD) to Date. */
    fun fromApiDate(dateString: String): Date? = runCatching {
        apiDateFormatter.parse(dateString)
    }.getOrNull()

    /** Keywords used by slot API: today, tomorrow, dayafter. */
    const val DATE_TODAY = "today"
    const val DATE_TOMORROW = "tomorrow"
    const val DATE_DAY_AFTER = "dayafter"
}
