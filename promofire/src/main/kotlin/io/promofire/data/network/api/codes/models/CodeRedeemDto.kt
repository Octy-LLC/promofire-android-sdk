package io.promofire.data.network.api.codes.models

import io.promofire.data.network.core.serializers.IsoDateSerializer
import io.promofire.models.Platform
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
internal data class CodeRedeemDto(
    val id: String,
    val redeemerId: String,
    val code: String,
    val platform: Platform?,
    val country: String?,
    val templateId: String,
    @Serializable(IsoDateSerializer::class)
    val redeemedAt: Date,
)
