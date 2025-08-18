package io.promofire.data.network.api.customers.models

import io.promofire.models.Platform
import kotlinx.serialization.Serializable

@Serializable
internal data class CreatePresetRequestDto(
    val platform: Platform,
)
