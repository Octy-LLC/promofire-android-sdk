package io.promofire.data.mappers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

private val EmptyJsonObject = JsonObject(emptyMap())

internal fun String?.toJsonElement(): JsonElement = this?.let { Json.parseToJsonElement(it) } ?: EmptyJsonObject
