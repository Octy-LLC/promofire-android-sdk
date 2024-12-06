package io.promofire.data.network.api.codes.models

import io.promofire.data.network.core.serializers.IsoDateSerializer
import io.promofire.models.Code
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.Date

@Serializable
internal data class CodeDto(
    val value: String,
    val status: Code.Status,
    val templateId: String,
    @Serializable(IsoDateSerializer::class)
    val createdAt: Date,
    @Serializable(IsoDateSerializer::class)
    val updatedAt: Date,
    val expiresAt: Int,
    val ownerId: String,
    val payload: JsonElement,
    val amount: String,
)
