package io.promofire.models.params

public data class CreateCodesParams(
    val templateId: String,
    val count: Int,
    val payload: String? = null,
)
