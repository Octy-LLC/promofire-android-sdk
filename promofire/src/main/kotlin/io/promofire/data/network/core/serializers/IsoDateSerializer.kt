package io.promofire.data.network.core.serializers

import io.promofire.utils.DateFormatter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.ParseException
import java.util.Date

internal class IsoDateSerializer : KSerializer<Date> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        try {
            val decodedString = decoder.decodeString()
            return DateFormatter.parse(decodedString)
        } catch (e: ParseException) {
            throw SerializationException(e)
        } catch (e: IllegalArgumentException) {
            throw SerializationException(e)
        }
    }

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(DateFormatter.format(value))
    }
}
