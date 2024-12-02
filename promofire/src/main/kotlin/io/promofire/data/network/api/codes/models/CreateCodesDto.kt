package io.promofire.data.network.api.codes.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class CreateCodesDto(
    val templateId: String,
    val payload: JsonElement,
    val count: Int,
)
