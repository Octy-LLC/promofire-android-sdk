package io.promofire.models.params

public data class GenerateCodesParams(
    val templateId: String,
    val count: Int,
    val payload: String? = null,
)
