package io.promofire.data.network.api.auth.requests

import kotlinx.serialization.Serializable

@Serializable
internal data class SdkAuthRequestDto(
    val tenant: String,
    val secret: String,
)
