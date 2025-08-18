package io.promofire.data.network.api.codes.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class UpdateCodeDto(
    val active: Boolean,
    val payload: JsonElement,
)
