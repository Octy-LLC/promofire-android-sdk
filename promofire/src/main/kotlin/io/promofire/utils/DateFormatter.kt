package io.promofire.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object DateFormatter {

    private const val DATE_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private val isoDateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun format(date: Date): String {
        return isoDateFormatter.format(date)
    }

    fun parse(dateString: String): Date {
        return isoDateFormatter.parse(dateString) ?: dateFormatError(dateString)
    }

    private fun dateFormatError(dateString: String): Nothing {
        throw IllegalArgumentException("Incorrect date format: $dateString. Correct date pattern: $DATE_FORMAT")
    }
}

internal fun Date.toIsoString(): String = DateFormatter.format(this)
