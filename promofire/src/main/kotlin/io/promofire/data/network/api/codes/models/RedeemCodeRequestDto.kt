package io.promofire.data.network.api.codes.models

import io.promofire.models.Platform
import kotlinx.serialization.Serializable

@Serializable
internal data class RedeemCodeRequestDto(
    val codeValue: String,
    val platform: Platform,
)
