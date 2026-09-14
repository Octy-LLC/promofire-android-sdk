package io.promofire.models.params

public data class UpdateCodeParams(
    /** Включить или отключить код. */
    val active: Boolean? = null,
    /** Работает только если у шаблона `hasMutablePayload`. */
    val payload: Map<String, String>? = null,
)
