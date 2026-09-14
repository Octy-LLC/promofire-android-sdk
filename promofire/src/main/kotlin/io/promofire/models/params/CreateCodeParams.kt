package io.promofire.models.params

public data class CreateCodeParams(
    /** Значение кода. Должно быть уникальным в пределах тенанта. */
    val value: String,
    val templateId: String,
    val payload: Map<String, String> = emptyMap(),
)
