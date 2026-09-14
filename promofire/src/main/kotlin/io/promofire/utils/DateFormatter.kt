package io.promofire.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Разбор и печать дат в формате API.
 *
 * `SimpleDateFormat` не потокобезопасен, а SDK допускает параллельные
 * запросы, поэтому у каждого потока свой экземпляр.
 */
internal object DateFormatter {

    private const val DATE_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private val formatter = ThreadLocal.withInitial {
        SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun format(date: Date): String = formatter.get()!!.format(date)

    fun parse(dateString: String): Date =
        formatter.get()!!.parse(dateString)
            ?: throw IllegalArgumentException(
                "Incorrect date format: $dateString. Correct date pattern: $DATE_FORMAT",
            )
}

internal fun Date.toIsoString(): String = DateFormatter.format(this)
