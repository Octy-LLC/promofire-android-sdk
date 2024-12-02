package io.promofire.data.network.api.codes.models

import kotlinx.serialization.Serializable

@Serializable
internal data class RedeemsDto(
    val redeems: List<RedeemDto>,
    val total: Int,
)
