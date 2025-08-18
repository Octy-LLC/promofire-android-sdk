package io.promofire.data.network.api.codes.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CodesDto(
    val codes: List<CodeDto>,
    val total: Int,
)
