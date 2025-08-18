package io.promofire.models.params

public data class UpdateCodeParams(
    val isActive: Boolean,
    val payload: String? = null,
)
