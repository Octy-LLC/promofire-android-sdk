package io.promofire.models.params

public data class CreateCodesParams(
    val templateId: String,
    /** Сколько кодов выпустить. Значения генерирует сервер. */
    val count: Int,
    val payload: Map<String, String> = emptyMap(),
)
