package io.promofire.models.params

public data class CreateCodeParams(
    val value: String,
    val templateId: String,
    val payload: String? = null,
)
