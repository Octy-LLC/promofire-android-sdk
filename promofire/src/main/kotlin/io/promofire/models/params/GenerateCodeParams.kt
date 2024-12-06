package io.promofire.models.params

public data class GenerateCodeParams(
    val value: String,
    val templateId: String,
    val payload: String? = null,
)
