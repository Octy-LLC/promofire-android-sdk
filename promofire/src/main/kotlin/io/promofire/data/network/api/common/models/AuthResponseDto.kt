package io.promofire.data.network.api.common.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AuthResponseDto(
    val accessToken: String,
)
