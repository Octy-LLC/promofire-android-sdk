package io.promofire.data.network.api.codes.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CodeRedeemsDto(
    val redeems: List<CodeRedeemDto>,
    val total: Int,
)
