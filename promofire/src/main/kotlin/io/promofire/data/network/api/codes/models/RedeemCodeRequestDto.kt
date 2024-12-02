package io.promofire.data.network.api.codes.models

import kotlinx.serialization.Serializable

@Serializable
internal data class RedeemCodeRequestDto(
    val codeValue: String,
    val platform: String,
)
