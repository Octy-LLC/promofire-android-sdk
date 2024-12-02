package io.promofire.data.network.core.exceptions

import io.promofire.data.network.core.serializers.StringListSerializer
import kotlinx.serialization.Serializable

@Serializable
public class PromofireNetworkException internal constructor(
    @Serializable(StringListSerializer::class)
    public val messages: List<String>,
    public val error: String?,
    public val statusCode: Int,
) : Exception() {

    override val message: String
        get() = messages.firstOrNull() ?: messages.joinToString(separator = " | ") { it }
}
