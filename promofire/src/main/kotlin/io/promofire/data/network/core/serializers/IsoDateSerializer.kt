package io.promofire.data.network.core.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal class IsoDateSerializer : KSerializer<Date> {

    private val dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private fun newDateFormat(): DateFormat {
        return SimpleDateFormat(dateFormat, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        try {
            val decodedString = decoder.decodeString()
            return newDateFormat().parse(decodedString) ?: dateFormatError(decodedString)
        } catch (e: ParseException) {
            throw SerializationException(e)
        }
    }

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(newDateFormat().format(value))
    }

    private fun dateFormatError(
        decodedString: String,
    ): Nothing {
        throw IllegalArgumentException("Incorrect date format: $decodedString. Correct date pattern: $dateFormat")
    }
}
