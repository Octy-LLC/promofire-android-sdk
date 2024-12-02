package io.promofire.data.network.api.customers.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CreatePresetRequestDto(
    val platform: String,
)
